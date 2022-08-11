/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2020 Guardsquare NV
 */
package proguard.dexfile.writer;

import com.android.tools.r8.ByteDataView;
import com.android.tools.r8.CompilationFailedException;
import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.DexIndexedConsumer;
import com.android.tools.r8.DiagnosticsHandler;
import com.android.tools.r8.origin.Origin;
import proguard.classfile.Clazz;
import proguard.classfile.ProgramClass;
import proguard.classfile.io.ProgramClassWriter;
import proguard.classfile.visitor.ClassVisitor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * This ClassVisitor converts and collects the classes that it visits, and uses D8
 * to convert them to the dex format.
 * <p>
 * Note: this converter works slightly different from our own or the dx-based one.
 *       When visiting the classes to convert we only convert them to class files
 *       and collect them. The actual conversion is done at the very end, as D8
 *       uses an approach similar to ProGuard.
 *
 * @author Thomas Neidhart
 */
public class D8ClassConverter
implements   ClassVisitor
{

    private final D8DexFile dexFile;


    public D8ClassConverter(D8DexFile dexFile)
    {
        this.dexFile = dexFile;
    }


    // Implementations for ClassVisitor.

    @Override
    public void visitAnyClass(Clazz clazz) {}


    @Override
    public void visitProgramClass(ProgramClass programClass)
    {
        try
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            programClass.accept(
                new ProgramClassWriter(
                new DataOutputStream(byteArrayOutputStream)
                )
            );
            byte[] classData = byteArrayOutputStream.toByteArray();
            synchronized (dexFile)
            {
                dexFile.addProgramClassData(classData);
            }
        }
        catch (Exception e)
        {
            System.err.println("Unexpected error while converting:");
            System.err.println("  Class       = ["+programClass.getName()+"]");

            e.printStackTrace();
            System.err.println("Not converting this class");
        }
    }


    // Helper classes.

    public static class D8DexFile
    {
        private final InMemoryDexConsumer consumer;
        private final D8Command.Builder  androidAppBuilder;
        private final int                 threadCount;


        public D8DexFile(int           threadCount,
                         List<Path> libraryJars)
        {
            consumer          = new InMemoryDexConsumer();
            androidAppBuilder = D8Command.builder();
            this.threadCount  = threadCount;

            if (libraryJars != null)
            {
                // Add all configured library jars for D8.
                for (int index = 0; index < libraryJars.size(); index++)
                {
                    Path classPathEntry = libraryJars.get(index);

                    // Only add existing library jars and supported file types to D8,
                    // otherwise we might get runtime errors.
                    if (Files.exists(classPathEntry) &&
                        (isDexFile(classPathEntry) ||
                         isApkFile(classPathEntry) ||
                         isArchive(classPathEntry)))
                    {
                        androidAppBuilder.addLibraryFiles(classPathEntry);
                    }
                }
            }
        }

        public static boolean isDexFile(Path path) {
            String name = path.getFileName().toString().toLowerCase();
            return name.endsWith(".dex");
        }

        public static boolean isApkFile(Path path) {
            String name = path.getFileName().toString().toLowerCase();
            return name.endsWith(".apk");
        }

        public static boolean isArchive(Path path) {
            String name = path.getFileName().toString().toLowerCase();
            return name.endsWith(".apk")
                || name.endsWith(".jar")
                || name.endsWith(".zip")
                || name.endsWith(".aar");
        }

        public void addProgramClassData(byte[] data)
        {
            androidAppBuilder.addClassProgramData(data, Origin.unknown());
        }


        public void writeTo(OutputStream outputStream)
            throws IOException {

            try {
                D8.run(
                    androidAppBuilder
                        .setProgramConsumer(consumer)
                        .build()
                );

                outputStream.write(consumer.data);
                outputStream.flush();
            } catch (CompilationFailedException e) {
                throw new IOException(e);
            }

//            outputStream.write(consumer.data);
        }
    }


    /**
     * A simple implementation of a DexIndexedConsumer that just keeps the dex file content in memory.
     */
    private static class InMemoryDexConsumer implements DexIndexedConsumer
    {
        private byte[] data;


        @Override
        public void accept(int fileIndex, ByteDataView data, Set<String> descriptors, DiagnosticsHandler handler)
        {
            this.data = data.copyByteData();
        }


        @Override
        public void finished(DiagnosticsHandler handler)
        {
            // Do nothing.
        }
    }
}
