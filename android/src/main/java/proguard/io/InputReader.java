package proguard.io;

import proguard.classfile.ClassPool;
import proguard.classfile.visitor.ClassNameFilter;
import proguard.classfile.visitor.ClassPoolFiller;
import proguard.dexfile.writer.ClassPath;
import proguard.dexfile.writer.ClassPathEntry;
import proguard.dexfile.writer.DataEntryReaderFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class InputReader {

    private boolean android;
    private final List<Path> programJars;
    private final List<Path> libraryJars;


    public InputReader (List<Path> programJars, List<Path> libraryJars, boolean android)
    {
        this.android = android;
        this.programJars = programJars;
        this.libraryJars = libraryJars;
    }

    public void execute(ClassPool programClassPool, ClassPool libraryClassPool) throws IOException {

        ClassPoolFiller programClassPoolFiller = new ClassPoolFiller(programClassPool);
        ClassPoolFiller libraryClassPoolFiller = new ClassPoolFiller(libraryClassPool);

        DataEntryReader classReader =
                new ClassReader(false, false, false, false, null,
                        new ClassNameFilter("**",
                                programClassPoolFiller));

        DataEntryReader dexReader = new NameFilteredDataEntryReader(
                "classes*.dex",
                new DexClassReader(
                        true,
                        programClassPoolFiller
                )
        );

        dexReader = new NameFilteredDataEntryReader(
                "**.smali",
                new Smali2DexReader(dexReader),
                dexReader
        );

//        for (Path p : programJars) {
//            File file = new File(p.toUri());
//
//            ClassPathEntry classPathEntry = new ClassPathEntry(file, false);
//
//            readInput("Reading program ",
//                    classPathEntry,
//                    new ClassFilter(classReader, dexReader), true);
//        }

        for (Path p : programJars) {

            Path classPathEntry = p;

            readInput("Reading program ",
                    classPathEntry,
                    new ClassFilter(classReader, dexReader), true);
        }


//        for (Path p : libraryJars) {
//            File file = new File(p.toUri());
//
//            ClassPathEntry classPathEntry = new ClassPathEntry(file, false);
//
//            // Library
//        readInput("Reading library ",
//                classPathEntry,
//                new ClassFilter(new ClassReader(true, false, false, false, null,
//                        new ClassNameFilter("**",
//                                libraryClassPoolFiller)),
//                        new NameFilteredDataEntryReader(
//                                "classes*.dex",
//                                new DexClassReader(
//                                        true,
//                                        libraryClassPoolFiller
//                                ),
//                                classReader
//                        )), true);
//        }


    }

    /**
     * Reads all input entries from the given section of the given class path.
     */
    public void readInput(String          messagePrefix,
                          List<Path> programJars,
                          int             fromIndex,
                          int             toIndex,
                          DataEntryReader reader)
            throws IOException
    {

        boolean output = true;

        for (int index = fromIndex; index < toIndex; index++)
        {
            Path entry = programJars.get(index);
            if (!output)
            {
                readInput(messagePrefix, entry, reader, true);
            }
        }
    }

    /**
     * Reads the given input class path entry.
     */
    private void readInput(String messagePrefix,
                           Path classPathEntry,
                           DataEntryReader dataEntryReader,
                           boolean android)
            throws IOException
    {
        try
        {
            // Create a reader that can unwrap jars, wars, ears, jmods and zips.
            DataEntryReader reader =
                    new DataEntryReaderFactory(android)
                            .createDataEntryReader(messagePrefix,
                                    classPathEntry,
                                    dataEntryReader);

            // Create the data entry source.
            DataEntrySource source =
                    new DirectorySource(classPathEntry.toFile());

            // Pump the data entries into the reader.
            source.pumpDataEntries(reader);
        }
        catch (IOException ex)
        {
            throw new IOException("Can't read [" + classPathEntry + "] (" + ex.getMessage() + ")", ex);
        }
    }

}
