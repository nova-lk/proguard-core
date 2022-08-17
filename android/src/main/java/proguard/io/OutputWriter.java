package proguard.io;

import proguard.classfile.ClassPool;
import proguard.classfile.Clazz;
import proguard.classfile.visitor.ClassVisitor;
import proguard.dexfile.writer.ClassPath;
import proguard.dexfile.writer.ClassPathEntry;
import proguard.dexfile.writer.DataEntryWriterFactory;
import proguard.dexfile.writer.DexDataEntryWriterFactory;
import proguard.util.StringMatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Date;
import java.util.List;

public class OutputWriter {

    private boolean android;
    private final ClassPath programJars;
    private final ClassPath libraryJars;

    public OutputWriter(ClassPath programJars, ClassPath libraryJars) {
        this.programJars = programJars;
        this.libraryJars = libraryJars;
    }

    public void execute(ClassPool programClassPool, ClassPool libraryClassPool) throws IOException {

        DexClassReader extraDexClassReader = new DexClassReader(false, new ClassVisitor() {
            @Override
            public void visitAnyClass(Clazz clazz) {

            }
        });

        // Convert the current time into DOS date and time.
        Date currentDate = new Date();
        int modificationTime =
                (currentDate.getYear()  - 80) << 25 |
                        (currentDate.getMonth() + 1 ) << 21 |
                        currentDate.getDate()        << 16 |
                        currentDate.getHours()       << 11 |
                        currentDate.getMinutes()     << 5  |
                        currentDate.getSeconds()     >> 1;

        // Get the private key from the key store.
        KeyStore.PrivateKeyEntry[] privateKeyEntries = retrievePrivateKeys(); // always give null

        // Construct a filter for files that shouldn't be compressed.
        StringMatcher uncompressedFilter = null;
//                configuration.dontCompress == null ? null :
//                        new ListParser(new FileNameParser()).parse(configuration.dontCompress);

        DexDataEntryWriterFactory dexDataEntryWriterFactory = new DexDataEntryWriterFactory(programClassPool, false, null);

        // Create a main data entry writer factory for all nested archives.
        DataEntryWriterFactory dataEntryWriterFactory =
                new DataEntryWriterFactory(
                        programClassPool,
                        true,
                        dexDataEntryWriterFactory,
                        uncompressedFilter,
                        1,
                        false,
                        modificationTime,
                        false,
                        privateKeyEntries,
                        null);

        DataEntryWriter extraDataEntryWriter = null;

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
                            programClassPool,
                            extraDataEntryWriter != null ?
                                    // The extraDataEntryWriter must be remain open
                                    // until all outputs have been written.
                                    new NonClosingDataEntryWriter(extraDataEntryWriter) :
                                    // no extraDataEntryWriter supplied
                                    null,
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

    private KeyStore.PrivateKeyEntry[] retrievePrivateKeys()
            throws IOException
    {
        // Check the signing variables.
        List<File>   keyStoreFiles     = null;
        List<String> keyStorePasswords = null;
        List<String> keyAliases        = null;
        List<String> keyPasswords      = null;

        return null;

    }

    /**
     * Transfers the specified input jars to the specified output jars.
     */
    private void writeOutput(DataEntryWriterFactory dataEntryWriterFactory,
                             ClassPool              programClassPool,
                             DataEntryWriter        extraDataEntryWriter,
                             ClassPath            classPath,
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
                            toOutputIndex,
                            null);

            DataEntryWriter resourceWriter = writer;


            // By default, just copy resource files into the above writers.
            DataEntryReader resourceCopier =
                    new DataEntryCopier(resourceWriter);

            // We're now switching to the reader side, operating on the
            // contents possibly parsed from the input streams.
            DataEntryReader resourceRewriter = resourceCopier;

            // Write any kept directories.
            DataEntryReader reader =
                    writeDirectories(
//                            configuration,
                            programClassPool,
                            resourceCopier,
                            resourceRewriter);

            // Write classes.
            DataEntryReader classReader = new ClassFilter(new IdleRewriter(writer), reader);

            // Write classes attached as extra data entries.
            DataEntryReader extraClassReader = extraDataEntryWriter != null ?
                    new ClassFilter(new IdleRewriter(extraDataEntryWriter), reader) :
                    classReader;

            // Go over the specified input entries and write their processed
            // versions.
            new InputReader(programJars, libraryJars, android).readInput("  Copying resources from program ",
                    programJars,
                    fromInputIndex,
                    fromOutputIndex,
                    reader);

            // Close all output entries.
            writer.close();
        }
        catch (IOException ex)
        {
            String message = "Can't write [" + programJars.get(fromOutputIndex).toString() + "] (" + ex.getMessage() + ")";
            throw new IOException(message, ex);
        }
    }

    private DirectoryFilter writeDirectories(
//                                                Configuration   configuration,
                                             ClassPool       programClassPool,
                                             DataEntryReader directoryCopier,
                                             DataEntryReader fileCopier)
    {
        DataEntryReader directoryRewriter = null;

        // Wrap the directory copier with a filter and a data entry renamer.
//        if (configuration.keepDirectories != null)
//        {
//            StringFunction packagePrefixFunction =
//                    new MapStringFunction(createPackagePrefixMap(programClassPool));
//
//            directoryRewriter =
//                    new NameFilteredDataEntryReader(configuration.keepDirectories,
//                            new RenamedDataEntryReader(packagePrefixFunction,
//                                    directoryCopier,
//                                    directoryCopier));
//        }

        // Filter on directories and files.
        return new DirectoryFilter(directoryRewriter, fileCopier);
    }

}
