import com.android.tools.r8.CompilationFailedException;
import proguard.classfile.ClassPool;
import proguard.classfile.VersionConstants;
import proguard.classfile.attribute.visitor.AllAttributeVisitor;
import proguard.classfile.util.PrimitiveArrayConstantReplacer;
import proguard.classfile.visitor.AllMethodVisitor;
import proguard.classfile.visitor.ClassVersionFilter;
import proguard.io.ClassPath;
import proguard.io.ClassPathEntry;
import proguard.io.InputReader;
import proguard.io.OutputWriter;
import proguard.preverify.CodePreverifier;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestRun {

    public static void main(String[] args) throws CompilationFailedException, IOException {

//        String inputPath = "/home/pramitha/Downloads/classes.dex";
//        String inputPath = "/home/pramitha/Downloads/app2.apk";
//        String inputPath = "/home/pramitha/Downloads/SmaliSamples";

        File inputFile = new File("/home/pramitha/Downloads/apprelease.apk");
        File outputFile = new File("/home/pramitha/Downloads/DexOut/output/pgc_out_debug.apk");

        File libraryFile = new File("/home/pramitha/Android/Sdk/platforms/android-33/android.jar");


        ClassPath programJars = new ClassPath();
        ClassPath libraryjars = new ClassPath();

        programJars.add(new ClassPathEntry(inputFile, false));
        programJars.add(new ClassPathEntry(outputFile, true));
        libraryjars.add(new ClassPathEntry(libraryFile, false));

        ClassPool programClassPool = new ClassPool();
        ClassPool libraryClassPool = new ClassPool();


        InputReader inputReader = new InputReader(programJars, libraryjars);
        inputReader.execute(programClassPool, libraryClassPool);

        List<String> dontCompress = inputReader.getDontCompressList();

//        libraryClassPool.classesAccept(new ClassPrinter());
//        programClassPool.classesAccept(new ClassPrinter());
//        programClassPool.classesAccept(new PrimitiveArrayConstantReplacer());
        programClassPool.classesAccept(
                new ClassVersionFilter(VersionConstants.CLASS_VERSION_1_6,
                        new AllMethodVisitor(
                                new AllAttributeVisitor(
                                        new CodePreverifier(false)))));


        new OutputWriter(programJars, libraryjars, dontCompress)
        .execute(programClassPool, libraryClassPool);



////         Create the writer for the main file or directory.
//        DataEntryWriter writer = outputDex.isFile() ? new FixedFileWriter(outputDex) : new DirectoryWriter(outputDex);
//
//        // A dex file can't contain resource files.
//        writer =
//                new FilteredDataEntryWriter(
//                        new DataEntryNameFilter(
//                                new ExtensionMatcher("dex")),
//                        writer);
//
//
//        writer = new DexDataEntryWriter(6, programClassPool, "test_classes.dex", true, writer, writer);
////
//        programClassPool.classesAccept(
//                new DataEntryClassWriter(writer));
//
//        writer.close();

    }
}