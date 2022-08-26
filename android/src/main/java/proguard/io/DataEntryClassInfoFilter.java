/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2020 Guardsquare NV
 */
package proguard.io;

import proguard.classfile.ClassConstants;
import proguard.classfile.ClassPool;
import proguard.classfile.Clazz;
import proguard.io.DataEntry;
import proguard.io.DataEntryFilter;

/**
 * This DataEntryFilter filters data entries based on whether they correspond
 * to classes in a given class pool that have a given processing info.
 *
 * @author Eric Lafortune
 */
public class DataEntryClassInfoFilter
implements DataEntryFilter
{
    private final ClassPool classPool;
    private final Object    processingInfo;


    /**
     * Creates a new DataEntryClassInfoFilter.
     * @param classPool   the class pool in which the data entry is searched.
     * @param processingInfo the processing info that the found class should have.
     */
    public DataEntryClassInfoFilter(ClassPool classPool,
                                    Object    processingInfo)
    {
        this.classPool   = classPool;
        this.processingInfo = processingInfo;
    }


    // Implementations for DataEntryFilter.

    @Override
    public boolean accepts(DataEntry dataEntry)
    {
        // Is it a class entry?
        String name = dataEntry.getName();
        if (name.endsWith(ClassConstants.CLASS_FILE_EXTENSION))
        {
            // Does it still have a corresponding class?
            String className = name.substring(0, name.length() - ClassConstants.CLASS_FILE_EXTENSION.length());
            Clazz clazz = classPool.getClass(className);
            if (clazz != null)
            {
                // Does it have the specified processing info?
                return processingInfo.equals(clazz.getProcessingInfo());
            }
        }

        return false;
    }
}
