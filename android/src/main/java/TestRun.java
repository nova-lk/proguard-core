import com.android.tools.r8.CompilationFailedException;
import proguard.classfile.ClassPool;
import proguard.io.ClassPath;
import proguard.io.ClassPathEntry;
import proguard.io.InputReader;
import proguard.io.OutputWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestRun {

    public static void main(String[] args) throws CompilationFailedException, IOException {

//        String inputPath = "/home/pramitha/Downloads/classes.dex";
//        String inputPath = "/home/pramitha/Downloads/app2.apk";
//        String inputPath = "/home/pramitha/Downloads/SmaliSamples";

        File inputFile = new File("/home/pramitha/Downloads/app2.apk");
        File outputFile = new File("/home/pramitha/Downloads/DexOut/output/");
        File outputDex = new File("/home/pramitha/Downloads/DexOut/output/");

        ClassPath programFilePaths = new ClassPath();
        ClassPath libraryClassPaths = new ClassPath();

        programFilePaths.add(new ClassPathEntry(inputFile, false));
        programFilePaths.add(new ClassPathEntry(outputFile, true));

        ClassPool programClassPool = new ClassPool();
        ClassPool libraryClassPool = new ClassPool();


        InputReader inputReader = new InputReader(programFilePaths, libraryClassPaths);
        inputReader.execute(programClassPool, libraryClassPool);

        List<String> dontCompress = inputReader.getDontCompressList();

//        libraryClassPool.classesAccept(new ClassPrinter());
//        programClassPool.classesAccept(new ClassPrinter());

        new OutputWriter(programFilePaths, libraryClassPaths, dontCompress)
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