/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2020 Guardsquare NV
 */
package proguard.dexfile.writer;

import proguard.classfile.ClassPool;
import proguard.io.DataEntryReader;
import proguard.io.DataEntryWriter;
import proguard.io.NonClosingDataEntryWriter;
import proguard.util.StringMatcher;

/**
 * This class can create DataEntryWriter instances for writing dex files.
 *
 * @author Eric Lafortune
 * @author Thomas Neidhart
 */
public class DexDataEntryWriterFactory
{
    private final ClassPool           programClassPool;
    private final Configuration       configuration;
    private final boolean             appBundle;
    private final DataEntryReader extraDexDataEntryVisitor;
    private final int                 threadCount;


    /**
     * Creates a new DexDataEntryWriterFactory.
     *
     * @param programClassPool         the program class pool to process.
     * @param appBundle                specifies whether the dex files should
     *                                 be named following the app bundle
     *                                 directory structure.
//     * @param multiDexCount            specifies the number of dex files in
//     *                                 the multidex partitioning.
//     * @param splitDexFiles            an optional list of String filters for
//     *                                 classes that should be written to
//     *                                 separate dex files, using our own dex
//     *                                 splitting support at run-time.
//     * @param minSdkVersion            the minimum supported API level.
//     * @param debuggable               whether the dex file shall be debuggable
//     *                                 or not.
//     * @param classNameOrder           an optional list of class names to
//     *                                 specify their intended order in the dex
//     *                                 files,
     * @param extraDexDataEntryVisitor an optional extra visitor for all dex
     *                                 data entries that are written. The
     *                                 visitor can use the data entry names,
     *                                 but must not read their contents.
     */
    public DexDataEntryWriterFactory(ClassPool       programClassPool,
                                     Configuration configuration,
                                     boolean         appBundle,
                                     DataEntryReader extraDexDataEntryVisitor)
    {
        this.programClassPool = programClassPool;
        this.configuration = configuration;
        this.appBundle = appBundle;
        this.extraDexDataEntryVisitor = extraDexDataEntryVisitor;

        // Get a suitable thread count.
        String threadCountString = System.getProperty("conversion.threads");
        this.threadCount = threadCountString != null ?
            Integer.parseInt(threadCountString) :
            Runtime.getRuntime().availableProcessors() - 2;
    }


    /**
     * Wraps the given data entry writer in dex data entry writers for
     * "classes.dex", etc, supporting feature dex files, multidex, and
     * split dex files.
     * @param dexWriter the data entry writer to which dex files can be
     *                  written.
     */
    public DataEntryWriter wrapInDexWriter(DataEntryWriter dexWriter)
    {
        return wrapInDexWriter(dexWriter, dexWriter);
    }


    /**
     * Wraps the given data entry writers in dex data entry writers for
     * "classes.dex", etc, supporting feature dex files, multidex, and
     * split dex files.
     * @param dexWriter   the data entry writer to which dex files can be
     *                    written.
     * @param otherWriter the data entry writer to which all other files
     *                    can be written.
     */
    private DataEntryWriter wrapInDexWriter(DataEntryWriter dexWriter,
                                            DataEntryWriter otherWriter)
    {

        // Wrap in a writer for the only classes.dex file.
        otherWriter = wrapInDexWriter(appBundle ?
                                          AndroidConstants.AAB_BASE + AndroidConstants.AAB_DEX_INFIX :
                                          "",
                                      appBundle ?
                                          AndroidConstants.AAB_BASE + AndroidConstants.AAB_ROOT_INFIX :
                                          "",
                                      dexWriter,
                                      otherWriter);



        return otherWriter;
    }



    /**
     * Wraps the given data entry writers in dex data entry writers for
     * "classes.dex", etc, supporting multidex and split dex files.
     * @param dexFilePrefix      the path prefix for dex files.
     * @param extraDexFilePrefix the path prefix for extra dex files (with
     *                           non-standard names like extra1.dex).
     * @param dexWriter          the data entry writer to which dex files can be
     *                           written.
     * @param otherWriter        the data entry writer to which all other files
     *                           can be written.
     */
    private DataEntryWriter wrapInDexWriter(String          dexFilePrefix,
                                            String          extraDexFilePrefix,
                                            DataEntryWriter dexWriter,
                                            DataEntryWriter otherWriter)
    {
        // Start with wrapping in a writer for the basic classes.dex
        // file.
        otherWriter = wrapInSimpleDexWriter(dexFilePrefix,
                                            dexWriter,
                                            otherWriter);

        // Don't close the writer for dex files from enclosing writers.
        dexWriter = new NonClosingDataEntryWriter(dexWriter);

        // Wrap with writers for any multidex files.
        // The Android runtime and util classes will load them eagerly.
        // TODO: Multiple dex files
//        for (int index = multiDexCount; index > 0; index--)
//        {
//            otherWriter = wrapInMultiDexWriter(dexFilePrefix,
//                                               dexWriter,
//                                               otherWriter,
//                                               index);
//        }

        // Wrap with writers for any split dex files.
        // The code wil load them lazily.
        // TODO: split dex files
//        if (splitDexFiles != null)
//        {
//            // Construct filters for classes that should be written to separate
//            // dex files.
////            for (int index = 0; index < splitDexFiles.size(); index++)
////            {
////                //StringMatcher splitDexFileFilter =
////                //    new ListParser(new ClassNameParser()).parse((List)splitDexFiles.get(index));
////
////                otherWriter = wrapInSplitDexWriter(extraDexFilePrefix,
////                                                   dexWriter,
////                                                   otherWriter,
////                                                   index);
////            }
//        }

        return otherWriter;
    }


    /**
     * Wraps the given data entry writer in a dex data entry writer for
     * "classes.dex".
     */
    private DataEntryWriter wrapInSimpleDexWriter(String          dexFilePrefix,
                                                  DataEntryWriter dexWriter,
                                                  DataEntryWriter otherWriter)
    {
        // Add a writer for the simple file.
        String dexFileName = dexFilePrefix +
                             AndroidConstants.CLASSES_DEX;

        // Add a writer for the base dex file.
        // TODO: Don't force empty dex files for features in app bundles, but put hasCode="false" in their manifest files.
        return createDataEntryWriter(null,
                                     dexFileName,
                                     true,
                                     dexWriter,
                                     otherWriter);
    }


    /**
     * Wraps the given data entry writer in a dex data entry writer for
     * "classes[index].dex".
     */
    // TODO: Multiple dex files
//    private DataEntryWriter wrapInMultiDexWriter(String          dexFilePrefix,
//                                                 DataEntryWriter dexWriter,
//                                                 DataEntryWriter otherWriter,
//                                                 int             index)
//    {
//        // Add a writer for the multidex file.
//        String dexBaseName = AndroidConstants.CLASSES_PREFIX + (index + 1) +
//                             AndroidConstants.DEX_FILE_EXTENSION;
//        String dexFileName = dexFilePrefix + dexBaseName;
//
//        // Is the entry to be partitioned into this dex file?
//        // Note that the filter currently works on the base name.
//        return new FilteredDataEntryWriter(
//            new DataEntryClassInfoFilter(programClassPool, dexBaseName),
//
//                // Then use the matching dex writer.
//                createDataEntryWriter(null,
//                                      dexFileName,
//                                      false,
//                                      dexWriter,
//                                      otherWriter),
//
//                // Otherwise, the standard writer.
//                otherWriter);
//    }


    /**
     * Wraps the given data entry writer in a dex data entry writer for
     * "extra[index].dex". Makes sure the corresponding dex loader class
     * "DexLoader[index].class" is written if the dex file is written.
     */
    // TODO: split dex files
//    private DataEntryWriter wrapInSplitDexWriter(String          dexFilePrefix,
//                                                 DataEntryWriter dexWriter,
//                                                 DataEntryWriter otherWriter,
//                                                 int             index)
//    {
//        // Add writers for the split dex file and for its loader, whenever
//        // the split dex file is written.
//        String dexBaseName       = SplitDexPartitioner.DEX_PREFIX + (index + 1) +
//                                   AndroidConstants.DEX_FILE_EXTENSION;
//        String dexFileName       = dexFilePrefix + dexBaseName;
//
//        String dexLoaderFileName = SplitDexPartitioner.DEX_LOADER + (index + 1) +
//                                   ClassConstants.CLASS_FILE_EXTENSION;
//
//        // Is the entry to be partitioned into this dex file?
//        // Note that the filter currently works on the base name.
//        return new FilteredDataEntryWriter(
//            new DataEntryClassInfoFilter(programClassPool, dexBaseName),
//
//                // Then use the matching dex writer.
//                createDataEntryWriter(null, // splitDexFileFilters[index],
//                                      dexFileName,
//                                      false,
//                                      new ExtraDataEntryWriter(dexLoaderFileName,
//                                                               dexWriter,
//                                                               otherWriter),
//                                      otherWriter),
//
//                // Otherwise, the standard writer.
//                otherWriter);
//    }


    /**
     * Wraps the given data entry writer in a dex data entry writer with the
     * given parameters.
     */
    private DataEntryWriter createDataEntryWriter(StringMatcher   classNameFilter,
                                                  String          dexFileName,
                                                  boolean         forceDex,
                                                  DataEntryWriter dexWriter,
                                                  DataEntryWriter otherWriter)
    {
        return
                // Convert with d8.
                // This converter does not support a class name order.
                new DexDataEntryWriter(threadCount,
                        programClassPool,
                        dexFileName,
                        forceDex,
                        dexWriter,
                        otherWriter
                );
    }
}
