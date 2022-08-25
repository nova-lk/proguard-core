/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
  * Copyright (c) 2002-2020 Guardsquare NV
 */
package proguard.io;

import proguard.classfile.ClassConstants;
import proguard.classfile.ClassPool;
import proguard.util.ExtensionMatcher;
import proguard.util.FileNameParser;
import proguard.util.ListFunctionParser;
import proguard.util.ListParser;
import proguard.util.SingleFunctionParser;
import proguard.util.StringFunction;
import proguard.util.StringMatcher;
import proguard.util.WildcardManager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class can create DataEntryWriter instances based on class paths. The
 * writers will wrap the output in the proper apks, jars, aars, wars, ears,
 * and zips.
 *
 * @author Eric Lafortune
 */
public class DataEntryWriterFactory
{
    private static final int    PAGE_ALIGNMENT         = 4096;
    private static final byte[] JMOD_HEADER            = new byte[] { 'J', 'M', 1, 0 };
    private static final String JMOD_CLASS_FILE_PREFIX = "classes/";
    private static final String WAR_CLASS_FILE_PREFIX  = "classes/";

    private static final boolean DISABLE_ZIP64_SUPPORT = System.getProperty("disable.zip64.support") != null;

    private /*static*/ final String[][] JMOD_PREFIXES = new String[][]
    {
        { "**.class", JMOD_CLASS_FILE_PREFIX }
    };

    private /*static*/ final String[][] WAR_PREFIXES = new String[][]
    {
        { "**.class", WAR_CLASS_FILE_PREFIX }
    };

    private final ClassPool                     programClassPool;
    private final boolean                       dalvik;

    private final DexDataEntryWriterFactory     dexDataEntryWriterFactory;

    private final StringMatcher                 uncompressedFilter;

    private final int                           uncompressedAlignment;

    private final boolean                       pageAlignNativeLibs;

    private final int                           modificationTime;

    private final Map<File,DataEntryWriter> jarWriterCache = new HashMap<>();


    /**
     * Creates a new DataEntryWriterFactory.
     *
     * @param programClassPool                the program class pool to process.
     * @param dalvik                          specifies whether to write
     *                                        dalvik files or apk files.
     * @param dexDataEntryWriterFactory       a factory for creating data entry
     *                                        writers for (multi-)dex files.
     * @param uncompressedFilter              an optional filter for files that
     *                                        should not be compressed.
     * @param uncompressedAlignment           the desired alignment for the data
     *                                        of uncompressed entries.
     * @param pageAlignNativeLibs             specifies whether to align native
     *                                        libraries at page boundaries.
     * @param modificationTime                the modification date and time of
     *                                        the zip entries, in DOS
     *                                        format.
     */
    public DataEntryWriterFactory(ClassPool                     programClassPool,
                                  boolean                       dalvik,
                                  DexDataEntryWriterFactory     dexDataEntryWriterFactory,
                                  StringMatcher                 uncompressedFilter,
                                  int                           uncompressedAlignment,
                                  boolean                       pageAlignNativeLibs,
                                  int                           modificationTime)
    {
        this.programClassPool                = programClassPool;
        this.dalvik                          = dalvik;
        this.dexDataEntryWriterFactory       = dexDataEntryWriterFactory;
        this.uncompressedFilter              = uncompressedFilter;
        this.uncompressedAlignment           = uncompressedAlignment;
        this.pageAlignNativeLibs             = pageAlignNativeLibs;
        this.modificationTime                = modificationTime;
    }


    /**
     * Creates a DataEntryWriter that can write to the given class path entries.
     *
     * @param classPath            the output class path.
     * @param fromIndex            the start index in the class path.
     * @param toIndex              the end index in the class path.
     * @return a DataEntryWriter for writing to the given class path entries.
     */
    public DataEntryWriter createDataEntryWriter(ClassPath classPath,
                                                 int             fromIndex,
                                                 int             toIndex)
    {
        DataEntryWriter writer = null;

        // Create a chain of writers, one for each class path entry.
        for (int index = toIndex - 1; index >= fromIndex; index--)
        {
            ClassPathEntry entry = classPath.get(index);

            // We're allowing the same output file to be specified multiple
            // times in the class path. We only close cached jar writers
            // for this entry if its file doesn't occur again later on.
            boolean closeCachedJarWriter =
                    outputFileOccurs(entry, classPath, index + 1, classPath.size());

            writer = createClassPathEntryWriter(entry, closeCachedJarWriter);
        }

        return writer;
    }


    private boolean outputFileOccurs(ClassPathEntry entry,
                                     ClassPath      classPath,
                                     int            startIndex,
                                     int            endIndex)
    {
        File file = entry.getFile();

        for (int index = startIndex; index < endIndex; index++)
        {
            ClassPathEntry classPathEntry = classPath.get(index);
            if (classPathEntry.isOutput() &&
                classPathEntry.getFile().equals(file))
            {
                return false;
            }
        }

        return true;
    }


    /**
     * Creates a DataEntryWriter that can write to the given class path entry,
     * or delegate to another DataEntryWriter if its filters don't match.
     */
    private DataEntryWriter createClassPathEntryWriter(ClassPathEntry  classPathEntry,
                                                       boolean         closeCachedJarWriter)
    {
        File file = classPathEntry.getFile();

        boolean isDex  = classPathEntry.isDex();
        boolean isApk  = classPathEntry.isApk();
        boolean isAab  = classPathEntry.isAab();
        boolean isJar  = classPathEntry.isJar();
        boolean isAar  = classPathEntry.isAar();
        boolean isWar  = classPathEntry.isWar();
        boolean isEar  = classPathEntry.isEar();
        boolean isJmod = classPathEntry.isJmod();
        boolean isZip  = classPathEntry.isZip();

        boolean isFile = isDex || isApk || isAab || isJar || isAar || isWar || isEar || isJmod || isZip;

        List<String> filter     = DataEntryReaderFactory.getFilterExcludingVersionedClasses(classPathEntry);
        List<String> apkFilter  = classPathEntry.getApkFilter();
        List<String> aabFilter  = classPathEntry.getAabFilter();
        List<String> jarFilter  = classPathEntry.getJarFilter();
        List<String> aarFilter  = classPathEntry.getAarFilter();
        List<String> warFilter  = classPathEntry.getWarFilter();
        List<String> earFilter  = classPathEntry.getEarFilter();
        List<String> jmodFilter = classPathEntry.getJmodFilter();
        List<String> zipFilter  = classPathEntry.getZipFilter();

        System.out.println(
                "Preparing output " +
                        (isDex  ? "dex"  :
                        isApk  ? "apk"  :
                        isAab  ? "aab"  :
                        isJar  ? "jar"  :
                        isAar  ? "aar"  :
                        isWar  ? "war"  :
                        isEar  ? "ear"  :
                        isJmod ? "jmod" :
                        isZip  ? "zip"  :
                                 "directory") +
                       " [" + classPathEntry.getName() + "]" +
                       (filter     != null ||
                        apkFilter  != null ||
                        aabFilter  != null ||
                        jarFilter  != null ||
                        aarFilter  != null ||
                        warFilter  != null ||
                        earFilter  != null ||
                        jmodFilter != null ||
                        zipFilter  != null ? " (filtered)" : "")
        );

        // Create the writer for the main file or directory.
        DataEntryWriter writer = isFile ?
            new FixedFileWriter(file) :
            new DirectoryWriter(file);

        if (isDex)
        {
            // A dex file can't contain resource files.
            writer =
                new FilteredDataEntryWriter(
                new DataEntryNameFilter(
                new ExtensionMatcher(AndroidConstants.DEX_FILE_EXTENSION)),
                    writer);
        }
        else
        {
            // If the output is an archive, we'll flatten (unpack the contents of)
            // higher level input archives, e.g. when writing into a jar file, we
            // flatten zip files.
            boolean flattenApks  = false;
            boolean flattenAabs  = flattenApks  || isApk;
            boolean flattenJars  = flattenAabs  || isAab;
            boolean flattenAars  = flattenJars  || isJar;
            boolean flattenWars  = flattenAars  || isAar;
            boolean flattenEars  = flattenWars  || isWar;
            boolean flattenJmods = flattenEars  || isEar;
            boolean flattenZips  = flattenJmods || isJmod;

            // Set up the filtered jar writers.
            writer = wrapInJarWriter(file, writer, closeCachedJarWriter, flattenZips,  isZip, false, ".zip",  zipFilter,  null,        false, null);
            writer = wrapInJarWriter(file, writer, closeCachedJarWriter, flattenJmods, isJmod, false, ".jmod", jmodFilter, JMOD_HEADER, false, JMOD_PREFIXES);
            writer = wrapInJarWriter(file, writer, closeCachedJarWriter, flattenEars,  isEar, false, ".ear",  earFilter,  null,        false, null);
            writer = wrapInJarWriter(file, writer, closeCachedJarWriter, flattenWars,  isWar, false, ".war",  warFilter,  null,        false, WAR_PREFIXES);
            writer = wrapInJarWriter(file, writer, closeCachedJarWriter, flattenAars,  isAar, false, ".aar",  aarFilter,  null,        false, null);

            if (isAar)
            {

                writer =
                    new FilteredDataEntryWriter(new DataEntryNameFilter(new ExtensionMatcher(".jar")),
                                                new RenamedDataEntryWriter(string -> {
                                                    String fileName = string.substring(string.lastIndexOf('/') + 1);
                                                    if (fileName.equals("classes.jar"))
                                                    {
                                                        return fileName;
                                                    }
                                                    else
                                                    {
                                                        return "libs/" + fileName;
                                                    }
                                                }, writer),
                                                writer);

            }

            writer = wrapInJarWriter(file, writer, closeCachedJarWriter, flattenJars,  isJar, false,  ".jar",  jarFilter,  null, false, null);

            // Either we create an aab or apk; they can not be nested.
            writer = isAab ?
                wrapInJarWriter(file, writer, closeCachedJarWriter, flattenAabs, isAab,  true, ".aab", aabFilter, null, false, null) :
                wrapInJarWriter(file, writer, closeCachedJarWriter, flattenApks, isApk, false, ".apk", apkFilter, null, pageAlignNativeLibs, null);

            // Create a writer for plain class files. Don't close the enclosed
            // writer through it, but let it be closed later on.
            DataEntryWriter classWriter =
                new ClassDataEntryWriter(programClassPool,
                new NonClosingDataEntryWriter(writer));

            // Add a renaming filter, if specified.
            if (filter != null)
            {
                WildcardManager wildcardManager = new WildcardManager();

                StringFunction fileNameFunction =
                    new ListFunctionParser(
                    new SingleFunctionParser(
                    new FileNameParser(wildcardManager), wildcardManager)).parse(filter);

                // Slight asymmetry: we filter plain class files beforehand,
                // but we filter and rename dex files and resource files after
                // creating and renaming them in the feature structure.
                // We therefore don't filter class files that go into dex
                // files.
                classWriter = new RenamedDataEntryWriter(fileNameFunction, classWriter);
                writer      = new RenamedDataEntryWriter(fileNameFunction, writer);
            }

            writer =
                // Filter on class files.
                new NameFilteredDataEntryWriter(
                new ExtensionMatcher(ClassConstants.CLASS_FILE_EXTENSION),

                    // Collect and write out dex files or class files.
                    // The dex files may still be renamed afterwards.
                    dalvik ?
                        dexDataEntryWriterFactory.wrapInDexWriter(writer) :
                        classWriter,
                        writer);
        }
        return writer;
    }


    /**
     * Wraps the given DataEntryWriter in a ZipWriter, filtering and signing
     * if necessary.
     */
    private DataEntryWriter wrapInJarWriter(File            file,
                                            DataEntryWriter writer,
                                            boolean         closeCachedJarWriter,
                                            boolean         flatten,
                                            boolean         isJar,
                                            boolean         isAab,
                                            String          jarFilterExtension,
                                            List<String>    jarFilter,
                                            byte[]          jarHeader,
                                            boolean         pageAlignNativeLibs,
                                            String[][]      prefixes)
    {
        StringMatcher pageAlignmentFilter = pageAlignNativeLibs ?
            new FileNameParser().parse("lib/*/*.so") :
            null;

        // Flatten jars or zip them up.
        DataEntryWriter zipWriter;
        if (flatten)
        {
            // Unpack the jar.
            zipWriter = new ParentDataEntryWriter(writer);
        }

        // Do we have a cached writer?
        else if (!isJar || (zipWriter = jarWriterCache.get(file)) == null)
        {
            // Sign the jar.
            zipWriter =
                    wrapInSignedJarWriter(writer,
                                          isAab,
                                          jarHeader,
                                          pageAlignmentFilter);

            // Add a prefix to specified files inside the jar.
            if (prefixes != null)
            {
                DataEntryWriter prefixlessJarWriter = zipWriter;

                for (int index = prefixes.length - 1; index >= 0; index--)
                {
                    String prefixFileNameFilter = prefixes[index][0];
                    String prefix               = prefixes[index][1];

                    zipWriter =
                        new FilteredDataEntryWriter(
                            new DataEntryNameFilter(
                                new ListParser(new FileNameParser()).parse(prefixFileNameFilter)),
                            new PrefixAddingDataEntryWriter(prefix,
                                                            prefixlessJarWriter),
                            zipWriter);
                }
            }

            // Is it an outermost archive?
            if (isJar)
            {
                // Cache the jar writer so it can be reused.
                jarWriterCache.put(file, zipWriter);
            }
        }

        // Only close an outermost archive if specified.
        // It may be used later on.
        if (isJar && !closeCachedJarWriter)
        {
            zipWriter = new NonClosingDataEntryWriter(zipWriter);
        }

        // Either zip up the jar or delegate to the original writer.
        return
            // Is the data entry part of the specified type of jar?
            new FilteredDataEntryWriter(
            new DataEntryParentFilter(
            new DataEntryNameFilter(
            new ExtensionMatcher(jarFilterExtension))),

                // The parent of the data entry is a jar.
                // Write the data entry to the jar.
                // Apply the jar filter, if specified, to the parent.
                jarFilter != null ?
                    new FilteredDataEntryWriter(
                    new DataEntryParentFilter(
                    new DataEntryNameFilter(
                    new ListParser(new FileNameParser()).parse(jarFilter))),
                    zipWriter) :
                    zipWriter,

                // The parent of the data entry is not a jar.
                // Write the entry to a jar anyway if the output is a jar.
                // Otherwise just delegate to the original writer.
                isJar ?
                        zipWriter :
                        writer
            );
    }


    /**
     * Wraps the given DataEntryWriter in a ZipWriter, signing if necessary.
     */
    private DataEntryWriter wrapInSignedJarWriter(DataEntryWriter writer,
                                                  boolean         isAab,
                                                  byte[]          jarHeader,
                                                  StringMatcher   pageAlignmentFilter)
    {
        // Pack the zip.
        DataEntryWriter zipWriter =
                new ZipWriter(uncompressedFilter,
                              uncompressedAlignment,
                              !DISABLE_ZIP64_SUPPORT && isAab,
                              pageAlignmentFilter,
                              PAGE_ALIGNMENT,
                              modificationTime,
                              jarHeader,
                              writer);

        return zipWriter;
    }


}
