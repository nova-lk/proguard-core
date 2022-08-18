import com.android.tools.r8.CompilationFailedException;
//import com.guardsquare.proguard.tools.Smali2DexReader;
import proguard.classfile.ClassPool;
import proguard.classfile.visitor.ClassPrinter;
import proguard.dexfile.writer.ClassPath;
import proguard.dexfile.writer.ClassPathEntry;
import proguard.dexfile.writer.Configuration;
import proguard.dexfile.writer.DataEntryReaderFactory;
import proguard.dexfile.writer.DataEntryWriterFactory;
import proguard.dexfile.writer.DexDataEntryWriter;
import proguard.dexfile.writer.DexDataEntryWriterFactory;
import proguard.io.DataEntryClassWriter;
import proguard.io.DataEntryNameFilter;
import proguard.io.DataEntryWriter;
import proguard.io.DirectoryWriter;
import proguard.io.FilteredDataEntryWriter;
import proguard.io.FixedFileWriter;
import proguard.io.InputReader;
import proguard.io.OutputWriter;
import proguard.io.util.IOUtil;
import proguard.util.ExtensionMatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TestRun {

    public static void main(String[] args) throws CompilationFailedException, IOException {

//        String inputPath = "/home/pramitha/Downloads/classes.dex";
//        String inputPath = "/home/pramitha/Downloads/app2.apk";
//        String inputPath = "/home/pramitha/Downloads/SmaliSamples";

        File inputFile = new File("/home/pramitha/Downloads/app1.apk");
        File outputFile = new File("/home/pramitha/Downloads/DexOut/output/");

        ClassPath programFilePaths = new ClassPath();
        ClassPath libraryClassPaths = new ClassPath();

        programFilePaths.add(new ClassPathEntry(inputFile, false));
        programFilePaths.add(new ClassPathEntry(outputFile, true));

        ClassPool programClassPool = new ClassPool();
        ClassPool libraryClassPool = new ClassPool();

        Configuration configuration = new Configuration();
        configuration.programJars = programFilePaths;
        configuration.libraryJars = libraryClassPaths;
        configuration.android = true;

        new InputReader(configuration)
        .execute(programClassPool, libraryClassPool);

//        libraryClassPool.classesAccept(new ClassPrinter());
//        programClassPool.classesAccept(new ClassPrinter());
        programClassPool.removeClass("com.example.MyClass");


//        new OutputWriter(configuration)
//        .execute(programClassPool, libraryClassPool);
//

        // Create the writer for the main file or directory.
        DataEntryWriter writer = outputFile.isFile() ? new FixedFileWriter(outputFile) : new DirectoryWriter(outputFile);
//
////
        // A dex file can't contain resource files.
        writer =
                new FilteredDataEntryWriter(
                        new DataEntryNameFilter(
                                new ExtensionMatcher("dex")),
                        writer);


        writer = new DexDataEntryWriter(2, programClassPool, "test_classes.dex", true, writer, writer);

        programClassPool.classesAccept(
                new DataEntryClassWriter(writer));

        writer.close();

    }
}



//        File file = new File("/home/pramitha/Downloads/DexOut/output");
//
//        // Create the writer for the main file or directory.
//        DataEntryWriter writer = file.isFile() ? new FixedFileWriter(file) : new DirectoryWriter(file);
//
//        // A dex file can't contain resource files.
//        writer =
//                new FilteredDataEntryWriter(
//                        new DataEntryNameFilter(
//                                new ExtensionMatcher("dex")),
//                        writer);
//
//        ClassPool cp = IOUtil.read("/home/pramitha/Downloads/spacemafia.jar", false);
//
//        DataEntryReaderFactory reader = new DataEntryReaderFactory();
//
//        writer = new DexDataEntryWriter(2, cp, "test_classes.dex", true, writer, writer);
//
//        cp.classesAccept(
//                new DataEntryClassWriter(writer));
//
//        writer.close();

//        System.out.println(Arrays.toString(consumer.getContents()));

//        File file = new File("/home/pramitha/Downloads/SmaliSamples/smali_a.smali"); // from D8.run