/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2020 Guardsquare NV
 */
package proguard.io;

import proguard.classfile.ClassPool;

import java.util.HashSet;
import java.util.Set;

/**
 * This class can create DataEntryWriter instances for writing dex files.
 *
 * @author Eric Lafortune
 * @author Thomas Neidhart
 */
public class DexDataEntryWriterFactory
{
    private final ClassPool           programClassPool;
    private final int                 multiDexCount;
    private final boolean             appBundle;
    private final ClassPath           libraryJars;

    /**
     * Creates a new DexDataEntryWriterFactory.
     *
     * @param programClassPool         the program class pool to process.
     * @param appBundle                specifies whether the dex files should
     *                                 be named following the app bundle
     *                                 directory structure.
     * @param multiDexCount            specifies the number of dex files in
     *                                 the multidex partitioning.
     */
    public DexDataEntryWriterFactory(ClassPool       programClassPool,
                                     ClassPath       libraryJars,
                                     boolean         appBundle,
                                     int             multiDexCount)
    {
        this.programClassPool         = programClassPool;
        this.libraryJars              = libraryJars;
        this.appBundle                = appBundle;
        this.multiDexCount            = multiDexCount;
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
        // Collect all unique feature names.
        Set<String> featureNames = new HashSet<>();
        programClassPool.classesAccept(new ClassFeatureNameCollector(featureNames));

        if (featureNames.isEmpty())
        {
            // Wrap in a writer for the only classes.dex file.
            otherWriter = wrapInDexWriter(appBundle ?
                            AndroidConstants.AAB_BASE + AndroidConstants.AAB_DEX_INFIX :
                            "",
                    dexWriter,
                    otherWriter);
        }
        else
        {
            // Start with wrapping in a writer for the basic classes.dex
            // file.
            otherWriter = wrapInDexWriter(AndroidConstants.AAB_BASE + AndroidConstants.AAB_DEX_INFIX,
                                          dexWriter,
                                          otherWriter);

        }

        return otherWriter;
    }

    /**
     * Wraps the given data entry writers in dex data entry writers for
     * "classes.dex", etc, supporting multidex and split dex files.
     * @param dexFilePrefix      the path prefix for dex files.
     * @param dexWriter          the data entry writer to which dex files can be
     *                           written.
     * @param otherWriter        the data entry writer to which all other files
     *                           can be written.
     */
    private DataEntryWriter wrapInDexWriter(String          dexFilePrefix,
                                            DataEntryWriter dexWriter,
                                            DataEntryWriter otherWriter)
    {
        // Start with wrapping in a writer for the basic classes.dex
        // file.
        otherWriter = wrapInSimpleDexWriter(dexFilePrefix,
                                            dexWriter,
                                            otherWriter);

//        // Don't close the writer for dex files from enclosing writers.
        dexWriter = new NonClosingDataEntryWriter(otherWriter);

        // Wrap with writers for any multidex files.
        // The Android runtime and util classes will load them eagerly.
        for (int index = multiDexCount; index > 0; index--)
        {
            otherWriter = wrapInMultiDexWriter(dexFilePrefix,
                                               dexWriter,
                                               otherWriter,
                                               index);
        }

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

        return createDataEntryWriter(dexFileName,
                                     true,
                                     dexWriter,
                                     otherWriter);

    }


    /**
     * Wraps the given data entry writer in a dex data entry writer for
     * "classes[index].dex".
     */
    private DataEntryWriter wrapInMultiDexWriter(String          dexFilePrefix,
                                                 DataEntryWriter dexWriter,
                                                 DataEntryWriter otherWriter,
                                                 int             index)
    {
        // Add a writer for the multidex file.
        String dexBaseName = AndroidConstants.CLASSES_PREFIX + (index + 1) +
                             AndroidConstants.DEX_FILE_EXTENSION;
        String dexFileName = dexFilePrefix + dexBaseName;

        // Is the entry to be partitioned into this dex file?
        // Note that the filter currently works on the base name.
        return new FilteredDataEntryWriter(
            new DataEntryClassInfoFilter(programClassPool, dexBaseName),

                // Then use the matching dex writer.
                createDataEntryWriter(dexFileName,
                                      false,
                                      dexWriter,
                                      otherWriter),

                // Otherwise, the standard writer.
                otherWriter);
    }

    /**
     * Wraps the given data entry writer in a dex data entry writer with the
     * given parameters.
     */
    private DataEntryWriter createDataEntryWriter(String          dexFileName,
                                                  boolean         forceDex,
                                                  DataEntryWriter dexWriter,
                                                  DataEntryWriter otherWriter)
    {
        return
                // Convert with d8.
                // This converter does not support a class name order.
                new DexDataEntryWriter(programClassPool,
                                       libraryJars,
                                       dexFileName,
                                       forceDex,
                                       dexWriter,
                                       otherWriter
                );
    }
}
