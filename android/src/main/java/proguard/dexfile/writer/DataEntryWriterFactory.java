/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
  * Copyright (c) 2002-2020 Guardsquare NV
 */
package proguard.dexfile.writer;

import proguard.classfile.ClassConstants;
import proguard.classfile.ClassPool;
import proguard.io.CascadingDataEntryWriter;
import proguard.io.ClassDataEntryWriter;
import proguard.io.DataEntryNameFilter;
import proguard.io.DataEntryParentFilter;
import proguard.io.DataEntryWriter;
import proguard.io.DirectoryWriter;
import proguard.io.FilteredDataEntryWriter;
import proguard.io.FixedFileWriter;
import proguard.io.JarWriter;
import proguard.io.NameFilteredDataEntryWriter;
import proguard.io.NonClosingDataEntryWriter;
import proguard.io.ParentDataEntryWriter;
import proguard.io.PrefixAddingDataEntryWriter;
import proguard.io.RenamedDataEntryWriter;
import proguard.io.ZipWriter;
import proguard.util.CollectionMatcher;
import proguard.util.ConstantStringFunction;
import proguard.util.ExtensionMatcher;
import proguard.util.FileNameParser;
import proguard.util.ListFunctionParser;
import proguard.util.ListParser;
import proguard.util.SingleFunctionParser;
import proguard.util.StringFunction;
import proguard.util.StringMatcher;
import proguard.util.WildcardManager;

import java.io.File;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * The APK-relative path of the control manifest file.
     *
     * Related constants can be found in:
     * <ul>
     * <li>{@code FileChecker}, as a constant of the same name.
     * <li>{@code dexguard-project.pro} of the runtime module,
     *     as an exclusion filter for encrypted strings.
     * </ul>
     */
    public static final String CONTROL_MANIFEST_FILE_NAME = "assets/MANIFEST.MF";

    /**
     * The list of digest algorithms that must be used in the control manifest.
     * We need to create digests for different algorithms as the hashing
     * algorithm used when re-signing the apk might differ from the one used
     * during the build. Use a set of commonly used hashing algorithms.
     */
    private static final String[] MANDATORY_DIGEST_ALGORITHMS = { "SHA1", "SHA-256" };


    private final ClassPool                     programClassPool;
    private final boolean                       dalvik;
//    private final int                           minSdkVersion;
//    private final int                           maxSdkVersion;
//    private final FeatureDataEntryWriterFactory featureDataEntryWriterFactory;
    private final DexDataEntryWriterFactory     dexDataEntryWriterFactory;
//    private final StringMatcher                 splitDimensionFilter;
    private final StringMatcher                 uncompressedFilter;
//    private final List<String>                  uncompressedGlobs;
    private final int                           uncompressedAlignment;
    private final boolean                       pageAlignNativeLibs;
//    private final boolean                       bundleUncompressNativeLibraries;
//    private final boolean                       bundleUncompressDexFiles;
    private final int                           modificationTime;
    private final boolean                       obfuscate;
    private final KeyStore.PrivateKeyEntry[]    privateKeyEntries;
//    private final File                          certificateLineage;
//    private final StringMatcher                 apkSignatureSchemeFilter;
    private final Set<String>                   checkedFileNames;

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
//     * @param uncompressedGlobs               an optional list of glob filters for
//     *                                        things that should not be compressed.
//     * @param bundleUncompressNativeLibraries Specifies in the BundleConfig file of an App Bundle to uncompress
//     *                                        native libraries on Android M devices and up.
//     * @param bundleUncompressDexFiles        Specifies in the BundleConfig file of an App Bundle to uncompress
//     *                                        dex files on Android P devices and up.
     * @param uncompressedAlignment           the desired alignment for the data
     *                                        of uncompressed entries.
     * @param pageAlignNativeLibs             specifies whether to align native
     *                                        libraries at page boundaries.
     * @param modificationTime                the modification date and time of
     *                                        the zip entries, in DOS
     *                                        format.
     * @param privateKeyEntries               optional private keys to sign jars.
//     * @param apkSignatureSchemeFilter        an optional filter for enabling
//     *                                        signature schemes ("v1', "v2",
//     *                                        "v3").
     * @param checkedFileNames                an optional set of file names
     *                                        to be included in a control
     *                                        manifest, for file tamper
     *                                        detection.
     */
    public DataEntryWriterFactory(ClassPool                     programClassPool,
                                  boolean                       dalvik,
                                  DexDataEntryWriterFactory     dexDataEntryWriterFactory,
                                  StringMatcher                 uncompressedFilter,
//                                  List<String>                  uncompressedGlobs,
//                                  boolean                       bundleUncompressNativeLibraries,
//                                  boolean                       bundleUncompressDexFiles,
                                  int                           uncompressedAlignment,
                                  boolean                       pageAlignNativeLibs,
                                  int                           modificationTime,
                                  boolean                       obfuscate,
                                  KeyStore.PrivateKeyEntry[]    privateKeyEntries,
//                                  File                          certificateLineage,
//                                  StringMatcher                 apkSignatureSchemeFilter,
                                  Set<String>                   checkedFileNames)
    {
        this.programClassPool                = programClassPool;
        this.dalvik                          = dalvik;
        this.dexDataEntryWriterFactory       = dexDataEntryWriterFactory;
        this.uncompressedFilter              = uncompressedFilter;
//        this.uncompressedGlobs               = uncompressedGlobs;
//        this.bundleUncompressNativeLibraries = bundleUncompressNativeLibraries;
//        this.bundleUncompressDexFiles        = bundleUncompressDexFiles;
        this.uncompressedAlignment           = uncompressedAlignment;
        this.pageAlignNativeLibs             = pageAlignNativeLibs;
        this.modificationTime                = modificationTime;
        this.obfuscate                       = obfuscate;
        this.privateKeyEntries               = privateKeyEntries;
//        this.certificateLineage              = certificateLineage;
//        this.apkSignatureSchemeFilter        = apkSignatureSchemeFilter;
        this.checkedFileNames                = checkedFileNames;
    }


    /**
     * Creates a DataEntryWriter that can write to the given class path entries.
     *
//     * @param classPath            the output class path.
     * @param fromIndex            the start index in the class path.
     * @param toIndex              the end index in the class path.
     * @param extraDataEntryWriter a writer to which extra injected files can be written.
     * @return a DataEntryWriter for writing to the given class path entries.
     */
    public DataEntryWriter createDataEntryWriter(ClassPath classPath,
                                                 int             fromIndex,
                                                 int             toIndex,
                                                 DataEntryWriter extraDataEntryWriter)
    {
        DataEntryWriter writer = null;

        // Create a chain of writers, one for each class path entry.
        for (int index = toIndex - 1; index >= fromIndex; index--)
        {
            ClassPathEntry entry = classPath.get(index);

            // We're allowing the same output file to be specified multiple
            // times in the class path. We only add a control manifest for
            // the input of the first occurrence.
            boolean addCheckingJarWriter =
                !outputFileOccurs(entry,
                                  classPath,
                                  0,
                                  index);

            // We're allowing the same output file to be specified multiple
            // times in the class path. We only close cached jar writers
            // for this entry if its file doesn't occur again later on.
            boolean closeCachedJarWriter =
                !outputFileOccurs(entry,
                        classPath,
                                  index + 1,
                        classPath.size());

            writer = createClassPathEntryWriter(entry,
                                                writer,
                                                extraDataEntryWriter,
                                                addCheckingJarWriter,
                                                closeCachedJarWriter);
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
                return true;
            }
        }

        return false;
    }


    /**
     * Creates a DataEntryWriter that can write to the given class path entry,
     * or delegate to another DataEntryWriter if its filters don't match.
     */
    private DataEntryWriter createClassPathEntryWriter(ClassPathEntry  classPathEntry,
                                                       DataEntryWriter alternativeWriter,
                                                       DataEntryWriter extraDataEntryWriter,
                                                       boolean         addCheckingJarWriter,
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

        List filter     = DataEntryReaderFactory.getFilterExcludingVersionedClasses(classPathEntry);
        List apkFilter  = classPathEntry.getApkFilter();
        List aabFilter  = classPathEntry.getAabFilter();
        List jarFilter  = classPathEntry.getJarFilter();
        List aarFilter  = classPathEntry.getAarFilter();
        List warFilter  = classPathEntry.getWarFilter();
        List earFilter  = classPathEntry.getEarFilter();
        List jmodFilter = classPathEntry.getJmodFilter();
        List zipFilter  = classPathEntry.getZipFilter();

        System.out.println("Preparing " +
                           (isDex || privateKeyEntries == null ? "" : "signed ") +
                           "output " +
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
                            zipFilter  != null ? " (filtered)" : ""));

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
            writer = wrapInJarWriter(file, writer, extraDataEntryWriter, closeCachedJarWriter, flattenZips,  isZip, ".zip",  zipFilter,  null,        false, null);
            writer = wrapInJarWriter(file, writer, extraDataEntryWriter, closeCachedJarWriter, flattenJmods, isJmod,  ".jmod", jmodFilter, JMOD_HEADER, false, JMOD_PREFIXES);
            writer = wrapInJarWriter(file, writer, extraDataEntryWriter, closeCachedJarWriter, flattenEars,  isEar,  ".ear",  earFilter,  null,        false, null);
            writer = wrapInJarWriter(file, writer, extraDataEntryWriter, closeCachedJarWriter, flattenWars,  isWar,   ".war",  warFilter,  null,        false, WAR_PREFIXES);
            writer = wrapInJarWriter(file, writer, extraDataEntryWriter, closeCachedJarWriter, flattenAars,  isAar,   ".aar",  aarFilter,  null,        false, null);

            if (isAar)
            {
                // If we're writing an AAR, all input jars need to
                // be merged into a final classes.jar file or need to be put in the lib folder.
                if (obfuscate)
                {

                    writer =
                        new FilteredDataEntryWriter(new DataEntryNameFilter(new ExtensionMatcher(".jar")),
                                                    new RenamedDataEntryWriter(
                                                        new ConstantStringFunction("classes.jar"), writer),
                                                    writer);
                }
                else
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
            }

            writer = wrapInJarWriter(file, writer, extraDataEntryWriter, closeCachedJarWriter, flattenJars,  isJar, ".jar",  jarFilter,  null, false, null);

            // Either we create an aab or apk; they can not be nested.
//            writer = isAab ?
//                wrapInJarWriter(file, writer, extraDataEntryWriter, closeCachedJarWriter, flattenAabs, isAab, true,  ".aab", aabFilter, null, false,               null) :
//                wrapInJarWriter(file, writer, extraDataEntryWriter, closeCachedJarWriter, flattenApks, isApk, false, ".apk", apkFilter, null, pageAlignNativeLibs, null);

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

            // Add a control manifest to the outermost archive.
            if (addCheckingJarWriter &&
                checkedFileNames != null)
            {
                writer =
                    wrapInCheckingJarWriter(isAab, writer, extraDataEntryWriter);
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

        // Let the writer cascade, if specified.
        return alternativeWriter != null ?
            new CascadingDataEntryWriter(writer, alternativeWriter) :
            writer;
    }


    /**
     * Wraps the given DataEntryWriter in a ZipWriter, filtering and signing
     * if necessary.
     */
    private DataEntryWriter wrapInJarWriter(File            file,
                                            DataEntryWriter writer,
                                            DataEntryWriter extraDataEntryWriter,
                                            boolean         closeCachedJarWriter,
                                            boolean         flatten,
                                            boolean         isJar,
//                                            boolean         isAab,
                                            String          jarFilterExtension,
                                            List            jarFilter,
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
            zipWriter = writer;

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

            // Wrap with an app bundle writer if necessary, to include the
            // necessary metadata files.
            // TODO
//            if (isAab)
//            {
//                zipWriter =
//                    new AabWriter(splitDimensionFilter,
//                                  uncompressedFilter,
//                                  uncompressedGlobs,
//                                  bundleUncompressNativeLibraries,
//                                  bundleUncompressDexFiles,
//                                  nativeDirectoryArchitectures(),
//                                  zipWriter);
//            }

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
                    writer);
    }


    /**
     * Wraps the given DataEntryWriter in a ZipWriter, signing if necessary.
     */
    private DataEntryWriter wrapInSignedJarWriter(DataEntryWriter writer,
                                                  DataEntryWriter extraDataEntryWriter,
                                                  boolean         isAab,
                                                  byte[]          jarHeader,
                                                  StringMatcher   pageAlignmentFilter)
    {
        // Do we need Android apk signature schemes v2 or v3?
//        boolean apkSignatureSchemeV2 =
//            dalvik &&
//            privateKeyEntries != null &&
//            !isAab &&
//            apkSignatureSchemeFilter.matches("v2");
//
//        boolean apkSignatureSchemeV3 =
//            dalvik                                 &&
//            privateKeyEntries != null              &&
//            !isAab                                 &&
//            apkSignatureSchemeFilter.matches("v3");

        // Pack the jar.

//        // Do we need to sign the jar with signature scheme v1?
//        if (privateKeyEntries != null &&
//            apkSignatureSchemeFilter.matches("v1"))
//        {
//            // Sign the jar (signature scheme v1).
//            // Are we signing an Android app?
//            zipWriter = !dalvik || isAab ?
//                new SignedJarWriter(privateKeyEntries[0],
//                                    new String[] { SignedJarWriter.DEFAULT_DIGEST_ALGORITHM },
//                                    About.VERSION_STRING,
//                                    zipWriter) :
//                new SignedApkJarWriter(minSdkVersion,
//                                       maxSdkVersion,
//                                       privateKeyEntries[0],
//                                       apkSignatureSchemeIDs,
//                                       About.VERSION_STRING,
//                                       zipWriter);
//        }

        return new ZipWriter(uncompressedFilter,
                      uncompressedAlignment,
                      !DISABLE_ZIP64_SUPPORT && isAab,
                      pageAlignmentFilter,
                      PAGE_ALIGNMENT,
                      modificationTime,
                      jarHeader,
                      writer);
    }


    /**
     * Wraps the given DataEntryWriter instances in a JarWriter that adds a
     * control manifest.
     */
    private DataEntryWriter wrapInCheckingJarWriter(boolean         isAab,
                                                    DataEntryWriter writer,
                                                    DataEntryWriter extraDataEntryWriter)
    {
        StringFunction manifestEntryNameFunction =
            StringFunction.IDENTITY_FUNCTION;

        // Strip the aab prefixes from the manifest entry names.
        // The prefixes all start with "base/" at this point, except
        // possibly for AndroidManifest.xml and resources.pb, which
        // can't be checked anyway.
//        if (isAab)
//        {
//            manifestEntryNameFunction =
//                new PrefixRemovingStringFunction(AndroidConstants.AAB_BASE_MANIFEST_PREFIX, manifestEntryNameFunction,
//                new PrefixRemovingStringFunction(AndroidConstants.AAB_BASE_DEX_PREFIX, manifestEntryNameFunction,
//                new PrefixRemovingStringFunction(AndroidConstants.AAB_BASE_ROOT_PREFIX, manifestEntryNameFunction,
//                new PrefixRemovingStringFunction(AndroidConstants.AAB_BASE_PREFIX))));
//        }

        // Add a control manifest to the extra data entry writer,
        // so it can be encrypted, etc.
        DataEntryWriter checkedWriter =
            new JarWriter(MANDATORY_DIGEST_ALGORITHMS,
                          null,
                          CONTROL_MANIFEST_FILE_NAME,
                          manifestEntryNameFunction,
                          writer,
                          extraDataEntryWriter);

        // Don't close the unchecked writer, so the checked writer is always
        // closed first, considering the filtering writers below.
        writer = new NonClosingDataEntryWriter(writer);

        // Filter on the checked file names, if specified.
        if (!checkedFileNames.isEmpty())
        {
            checkedWriter =
                new NameFilteredDataEntryWriter(
                new TransformedStringMatcher(manifestEntryNameFunction,
                new CollectionMatcher(checkedFileNames)),
                    checkedWriter,
                    writer);
        }

        // Filter app bundle files, if applicable.
        if (isAab)
        {
            // Does any of the manifest files specify to fuse code into the
            // universal apk?

            // Bundletool then merges the classes.dex files for the
            // universal apk.
            String unwantedDexExpression = AndroidConstants.AAB_CLASSES_DEX_EXPRESSION;

            // We shouldn't include bundle metadata files, and we can't check
            // protobuffer files, since bundletool will still convert them.
            checkedWriter =
                new NameFilteredDataEntryWriter(AndroidConstants.AAB_BUNDLE_METADATA_EXPRESSION + ',' +
                                                AndroidConstants.AAB_RESOURCES_PB_EXPRESSION    + ',' +
                                                AndroidConstants.AAB_XML_FILE_EXPRESSION        + ',' +
                                                unwantedDexExpression,
                                                writer,
                                                checkedWriter);
        }

        // We can't check many library files, since most of them will be
        // compiled and converted. We'll only check asset files for now.
        if (!dalvik)
        {
            checkedWriter =
                new NameFilteredDataEntryWriter(AndroidConstants.ASSETS_FILE_EXPRESSION,
                                                checkedWriter,
                                                writer);
        }

        return checkedWriter;
    }


}
