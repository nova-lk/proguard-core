/*
 * ProGuardCORE -- library to process Java bytecode.
 *
 * Copyright (c) 2002-2022 Guardsquare NV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package proguard.io;

import proguard.classfile.ClassPool;
import proguard.classfile.visitor.ClassNameFilter;
import proguard.classfile.visitor.ClassPoolFiller;
import proguard.dexfile.reader.Smali2DexReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InputReader {

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
    private List<String>    dontCompress;

    /**
     * Specifies whether the code should be targeted at the Android platform.
     */
    private boolean android = true;

    private boolean dalvik = true;

    /**
     * Specifies whether to skip non-public library classes while reading
     * library jars.
     */
    private boolean skipNonPublicLibraryClasses = false;

    /**
     * Specifies whether to skip non-public library class members while reading
     * library classes.
     */
    private boolean skipNonPublicLibraryClassMembers = true;


    public InputReader(ClassPath programJars, ClassPath libraryJars)
    {
        this.programJars = programJars;
        this.libraryJars = libraryJars;
    }

    /**
     * Fills the program class pool, library class pool, and resource file
     * pool by reading files based on the current configuration.
     */
    public void execute(ClassPool programClassPool, ClassPool libraryClassPool) throws IOException {

        ClassPoolFiller programClassPoolFiller = new ClassPoolFiller(programClassPool);
        ClassPoolFiller libraryClassPoolFiller = new ClassPoolFiller(libraryClassPool);

        // Create a reader to fill the program class pool (while checking for
        // duplicates).
        DataEntryReader classReader =
                new ClassReader(false,
                        skipNonPublicLibraryClasses,
                        skipNonPublicLibraryClassMembers,
                        false,
                        null,
                        new ClassNameFilter(
                                "**",
                                programClassPoolFiller
                        )
                );

        // Dex reader
        DataEntryReader dexReader =
                new NameFilteredDataEntryReader(
                        "classes*.dex",
                        new DexClassReader(
                                true,
                                programClassPoolFiller
                        )
                );

        // Read smali files
        dexReader =
                new NameFilteredDataEntryReader(
                        "**.smali",
                        new Smali2DexReader(dexReader),
                        dexReader
                );

        // Detect compression of the inputs.
        determineCompressionMethod(programJars);

        // Read the program class files and resource files and put them in the
        // program class pool and resource file pool.
        readInput("Reading program ",
                programJars,
                new ClassFilter(classReader, dexReader));

        // Read the library class files, if any.
        if (libraryJars != null)
        {
            // Read the library class files and put then in the library class
            // pool.
            readInput("Reading library ",
                    libraryJars,
                    new ClassFilter(
                            new ClassReader(
                                    true,
                                    skipNonPublicLibraryClasses,
                                    skipNonPublicLibraryClassMembers,
                                    true,
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
                    new DataEntryReaderFactory(android)
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


    protected void determineCompressionMethod(ClassPath classPath)
    {
        for (int index = 0; index < classPath.size(); index++)
        {
            ClassPathEntry entry = classPath.get(index);
            if (!entry.isOutput())
            {
                determineCompressionMethod(entry);
            }
        }
    }


    private void determineCompressionMethod(ClassPathEntry entry) {
        File file = entry.getFile();
        if (file == null || file.isDirectory())
        {
            return;
        }

        String regexDexClasses = "classes*.dex";

        try (ZipFile zip = new ZipFile(file))
        {
            Enumeration<? extends ZipEntry> entries = zip.entries();

            Set<String> storedEntries = new TreeSet<>();

            if (dontCompress != null)
            {
                storedEntries.addAll(dontCompress);
            }

            while (entries.hasMoreElements())
            {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.getMethod() == ZipEntry.DEFLATED)
                {
                    continue;
                }

                String name = zipEntry.getName();

                // Special case for classes.dex: If we end up creating another dex file, we want all dex files compression to be consistent.
                if (name.matches("classes\\d*.dex"))
                {
                    storedEntries.add(regexDexClasses);
                }
                else
                {
                    storedEntries.add(name);
                }
            }

            if (!storedEntries.isEmpty())
            {
                dontCompress = new ArrayList<>(storedEntries);
            }
        }
        catch (Exception e)
        {
            System.out.println("Could not determine compression method for entries of: " + file.getAbsolutePath() + ". You may need to add -dontcompress rules manually for this file. " + e);
        }
    }

    public List<String> getDontCompressList(){
        return dontCompress;
    }

}
