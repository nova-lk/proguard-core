package proguard.io;

import proguard.classfile.ClassPool;
import proguard.classfile.Clazz;
import proguard.classfile.visitor.ClassPoolFiller;
import proguard.util.FileNameParser;
import proguard.util.ListParser;
import proguard.util.StringMatcher;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class OutputWriter {

    /**
     * A list of input and output entries (jars, wars, ears, jmods, zips, and directories).
     */
    private final ClassPath programJars;

    /**
     * A list of library entries (jars, wars, ears, jmods, zips, and directories).
     */
    private final ClassPath libraryJars;

    /**
     * A list of String instances specifying a filter for files that should
     * not be compressed in output jars.
     */
    private final List<String> dontCompress;

    /**
     * Specifies whether the code should be targeted at the Android platform.
     */
    private boolean android = true;

    private boolean dalvik = true;


    public OutputWriter(ClassPath programJars, ClassPath libraryJars, List<String> dontCompress) {
        this.programJars = programJars;
        this.libraryJars = libraryJars;
        this.dontCompress = dontCompress;
    }

    public void execute(ClassPool programClassPool, ClassPool libraryClassPool) throws IOException {

        // Construct a filter for files that shouldn't be compressed.
        StringMatcher uncompressedFilter =
                dontCompress == null ? null :
                        new ListParser(new FileNameParser()).parse(dontCompress);

        // Convert the current time into DOS date and time.
        Date currentDate = new Date();
        int modificationTime =
                (currentDate.getYear()  - 80) << 25 |
                        (currentDate.getMonth() + 1 ) << 21 |
                        currentDate.getDate()        << 16 |
                        currentDate.getHours()       << 11 |
                        currentDate.getMinutes()     << 5  |
                        currentDate.getSeconds()     >> 1;

        // Create a data entry writer factory for dex files.
        DexDataEntryWriterFactory dexDataEntryWriterFactory =
                dalvik ?
                        new DexDataEntryWriterFactory(
                                programClassPool,
                                libraryJars,
                                false,
                                1,
                                null) :
                        null;


        // Create a main data entry writer factory for all nested archives.
        DataEntryWriterFactory dataEntryWriterFactory =
                new DataEntryWriterFactory(
                        programClassPool,
                        dalvik,
                        dexDataEntryWriterFactory,
                        uncompressedFilter,
                        1,
                        android,
                        modificationTime);

        int firstInputIndex = 0;
        int lastInputIndex  = 0;

        // Go over all program class path entries.
        for (int index = 0; index < programJars.size(); index++)
        {
            // Is it an input entry?
            ClassPathEntry entry = programJars.get(index);
            if (!entry.isOutput())
            {
                // It's an input entry. Remember the highest index.
                lastInputIndex = index;
            }
            else
            {
                // It's an output entry. Is it the last one in a
                // series of output entries?
                int nextIndex = index + 1;
                if (nextIndex == programJars.size() || !programJars.get(nextIndex).isOutput())
                {
                    // Write the processed input entries to the output entries.
                    writeOutput(dataEntryWriterFactory,
                            programJars,
                            firstInputIndex,
                            lastInputIndex + 1,
                            nextIndex);

                    // Start with the next series of input entries.
                    firstInputIndex = nextIndex;
                }
            }
        }
    }


    /**
     * Transfers the specified input jars to the specified output jars.
     */
    private void writeOutput(DataEntryWriterFactory dataEntryWriterFactory,
                             ClassPath              classPath,
                             int                    fromInputIndex,
                             int                    fromOutputIndex,
                             int                    toOutputIndex)
    throws IOException
    {
        // Debugging tip: your can wrap data entry writers and readers with
        //     new DebugDataEntryWriter("...", ....)
        //     new DebugDataEntryReader("...", ....)

        try
        {
            // Construct the writer that can write apks, jars, wars, ears, zips,
            // and directories, cascading over the specified output entries.
            DataEntryWriter writer =
                    dataEntryWriterFactory.createDataEntryWriter(classPath,
                                                                 fromOutputIndex,
                                                                 toOutputIndex);

            // By default, just copy resource files into the above writers.
            DataEntryReader resourceCopier = new DataEntryCopier(writer);

            // Write classes.
            DataEntryReader classReader = new ClassFilter(new IdleRewriter(writer), resourceCopier);

            DataEntryReader reader =
                    new NameFilteredDataEntryReader(
                            "classes*.dex",
                            dataEntry -> {
                                ClassPool classPool = new ClassPool();
                                new DexClassReader(false, new ClassPoolFiller(classPool)).read(dataEntry);
                                for (Clazz programClass : classPool.classes())
                                {
                                    classReader.read(new RenamedDataEntry(dataEntry, programClass.getName() + ".class"));
                                }
                            },
                            classReader
                    );



            // Go over the specified input entries and write their processed
            // versions.
            new InputReader(programJars, libraryJars).readInput("  Copying resources from program ",
                    classPath,
                    fromInputIndex,
                    fromOutputIndex,
                    reader);

            // Close all output entries.
            writer.close();
        }
        catch (IOException ex)
        {
            String message = "Can't write [" + classPath.get(fromOutputIndex).getName() + "] (" + ex.getMessage() + ")";
            throw new IOException(message, ex);
        }
    }
}
