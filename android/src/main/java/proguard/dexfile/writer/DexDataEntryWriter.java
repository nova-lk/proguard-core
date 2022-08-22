/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2020 Guardsquare NV
 */
package proguard.dexfile.writer;

import proguard.classfile.ClassConstants;
import proguard.classfile.ClassPool;
import proguard.classfile.Clazz;
import proguard.classfile.visitor.ClassVisitor;
import proguard.io.DataEntry;
import proguard.io.DataEntryWriter;
import proguard.io.IdleRewriter;
import proguard.io.RenamedDataEntry;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;

/**
 * This DataEntryWriter finds received class entries in the given class pool,
 * collects them in a dex file, and writes it out to the given data entry
 * writer. For resource entries, it returns valid output streams. For class
 * entries, it returns output streams that must not be used.
 *
 * @see IdleRewriter
 * @author Eric Lafortune
 */
public class DexDataEntryWriter implements DataEntryWriter
{
    private   final ClassPool       classPool;
    protected final String          dexFileName;
    private   final boolean         forceDex;
    private final D8ClassConverter.D8DexFile dexFile;
    protected final DataEntryWriter dexDataEntryWriter;
    protected final DataEntryWriter otherDataEntryWriter;

    private DataEntry currentDexEntry;
    private ClassVisitor currentClassConverter;


    /**
     * Creates a new DexDataEntryWriter.
     *
     * @param classPool                the class pool from which classes are
     *                                 collected.
     * @param dexFileName              the dex file name.
     * @param forceDex                 specifies whether the dex files should
     *                                 always be written, even if they don't
     *                                 contain any code.
     * @param dexDataEntryWriter       the writer to which the converted dex
     *                                 file is written.
     * @param otherDataEntryWriter     the writer to which other data entries
     *                                 are written.
     */
    public DexDataEntryWriter(int             threadCount,
                              ClassPool       classPool,
                              String          dexFileName,
                              boolean         forceDex,
                              DataEntryWriter dexDataEntryWriter,
                              DataEntryWriter otherDataEntryWriter)
    {
        this.classPool                = classPool;
        this.dexFileName              = dexFileName;
        this.forceDex                 = forceDex;
        this.dexFile                  = new D8ClassConverter.D8DexFile(threadCount, new LinkedList<>());
        this.dexDataEntryWriter       = dexDataEntryWriter;
        this.otherDataEntryWriter     = otherDataEntryWriter;

        this.currentClassConverter = new D8ClassConverter(dexFile);
    }


    // Implementations for DataEntryWriter.

    @Override
    public boolean createDirectory(DataEntry dataEntry) throws IOException
    {
        finishIfNecessary(dataEntry);

        return dexDataEntryWriter.createDirectory(dataEntry);
    }


    @Override
    public boolean sameOutputStream(DataEntry dataEntry1,
                                    DataEntry dataEntry2)
    throws IOException
    {
        return dexDataEntryWriter.sameOutputStream(dataEntry1, dataEntry2);
    }


    @Override
    public OutputStream createOutputStream(DataEntry dataEntry) throws IOException
    {
        finishIfNecessary(dataEntry);

        // Is it a class entry?
        String name = dataEntry.getName();
        if (name.endsWith(ClassConstants.CLASS_FILE_EXTENSION))
        {
            // Does it still have a corresponding class?
            String className = name.substring(0, name.length() - ClassConstants.CLASS_FILE_EXTENSION.length());
            Clazz clazz = classPool.getClass(className);
            if (clazz != null)
            {
                setUp(dataEntry);
                clazz.accept(currentClassConverter);
            }

            // Return a dummy, non-null output stream (to work with
            // cascading output writers).
//            return new FilterOutputStream(null);
            return new OutputStream() {
                @Override
                public void write(int i) {}
            };
        }

        // It's not a class entry. Do we always want a dex file?
        else if (forceDex)
        {
            // Make sure we at least have an empty dex file.
            setUp(dataEntry);
        }

        // Delegate for other class entries and resource entries.
        return otherDataEntryWriter.createOutputStream(dataEntry);
    }


    @Override
    public void close() throws IOException
    {
        finish();

        // Close the delegate writers.
        dexDataEntryWriter.close();
        otherDataEntryWriter.close();
    }


    @Override
    public void println(PrintWriter pw, String prefix)
    {
        pw.println(prefix + "DexDataEntryWriter");
        dexDataEntryWriter.println(pw, prefix + "  ");
        otherDataEntryWriter.println(pw, prefix + "  ");
    }


    // Small utility methods.

    private void writeClass(String className, DataEntry dataEntry) throws IOException
    {
        Clazz clazz = classPool.getClass(className);
        if (clazz != null)
        {
            setUp(dataEntry);

            // Collect the class in the Dex converter.
            clazz.accept(currentClassConverter);
        }
    }


    private void setUp(DataEntry dataEntry) throws IOException
    {
        if (currentDexEntry == null)
        {
            // Create a new class-to-dex converter.
            currentDexEntry       = new DexDataEntry(dataEntry, dexFileName);
//            currentClassConverter = new UniqueClassFilter(createClassConverter());
        }
    }


    private void finishIfNecessary(DataEntry dataEntry) throws IOException
    {
        // Would the new classes.dex end up in a different jar?
        if (currentDexEntry != null &&
            !dexDataEntryWriter.sameOutputStream(currentDexEntry, new DexDataEntry(dataEntry, dexFileName)))
        {
            finish();
        }
    }


    /**
     * Converts the collected classes and writes out the resulting dex file.
     */
    private void finish() throws IOException
    {
        // Do we have anything to write?
        if (currentDexEntry != null)
        {
            OutputStream outputStream =
                dexDataEntryWriter.createOutputStream(currentDexEntry);

            if (outputStream != null)
            {
                dexFile.writeTo(outputStream);
            }

            currentDexEntry       = null;
            currentClassConverter = null;
        }
    }


    private static class DexDataEntry extends RenamedDataEntry {

        public DexDataEntry(DataEntry dataEntry, String name)
        {
            super(dataEntry, name);
        }


        public String getOriginalName()
        {
            return getName();
        }
    }

}
