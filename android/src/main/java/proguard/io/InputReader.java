package proguard.io;

import kotlinx.metadata.KmClassifier;
import proguard.classfile.ClassPool;
import proguard.classfile.visitor.ClassNameFilter;
import proguard.classfile.visitor.ClassPoolFiller;
import proguard.dexfile.writer.ClassPath;
import proguard.dexfile.writer.ClassPathEntry;
import proguard.dexfile.writer.Configuration;
import proguard.dexfile.writer.DataEntryReaderFactory;

import java.io.IOException;

public class InputReader {

    private final Configuration configuration;

    public InputReader (Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void execute(ClassPool programClassPool, ClassPool libraryClassPool) throws IOException {

        ClassPoolFiller programClassPoolFiller = new ClassPoolFiller(programClassPool);
        ClassPoolFiller libraryClassPoolFiller = new ClassPoolFiller(libraryClassPool);

        DataEntryReader classReader =
                new ClassReader(false,
                        false,
                        false,
                        false,
                        null,
                        new ClassNameFilter(
                                "**",
                                programClassPoolFiller
                        )
                );

        DataEntryReader dexReader =
                new NameFilteredDataEntryReader(
                        "classes*.dex",
                        new DexClassReader(
                                true,
                                programClassPoolFiller
                        )
                );

        dexReader =
                new NameFilteredDataEntryReader(
                        "**.smali",
                        new Smali2DexReader(dexReader),
                        dexReader
                );

        readInput("Reading program ",
                configuration.programJars,
                new ClassFilter(classReader, dexReader));

        if (configuration.libraryJars != null)
        {
            readInput("Reading library ",
                    configuration.libraryJars,
                    new ClassFilter(
                            new ClassReader(
                                    true,
                                    false,
                                    false,
                                    false,
                                    null,
                                    new ClassNameFilter(
                                            "**",
                                            libraryClassPoolFiller
                                    )

                            )
                    ));
        }

    }

    /**
     * Reads all input entries from the given class path.
     */
    private void readInput(String          messagePrefix,
                           ClassPath       classPath,
                           DataEntryReader reader)
    throws IOException
    {
        readInput(messagePrefix,
                classPath,
                0,
                classPath.size(),
                reader);
    }

    /**
     * Reads all input entries from the given section of the given class path.
     */
    public void readInput(String          messagePrefix,
                          ClassPath       classPath,
                          int             fromIndex,
                          int             toIndex,
                          DataEntryReader reader)
    throws IOException
    {
        for (int index = fromIndex; index < toIndex; index++)
        {
            ClassPathEntry entry = classPath.get(index);
            if (!entry.isOutput())
            {
                readInput(messagePrefix, entry, reader);
            }
        }
    }

    /**
     * Reads the given input class path entry.
     */
    private void readInput(String messagePrefix,
                           ClassPathEntry classPathEntry,
                           DataEntryReader dataEntryReader)
    throws IOException
    {
        try
        {
            // Create a reader that can unwrap jars, wars, ears, jmods and zips.
            DataEntryReader reader =
                    new DataEntryReaderFactory(configuration.android)
                            .createDataEntryReader(messagePrefix,
                                                   classPathEntry,
                                                   dataEntryReader);

            // Create the data entry source.
            DataEntrySource source =
                    new DirectorySource(classPathEntry.getFile());

            // Pump the data entries into the reader.
            source.pumpDataEntries(reader);
        }
        catch (IOException ex)
        {
            throw new IOException("Can't read [" + classPathEntry + "] (" + ex.getMessage() + ")", ex);
        }
    }

}
