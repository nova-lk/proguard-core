import com.android.tools.r8.CompilationFailedException;
import proguard.classfile.ClassPool;
import proguard.dexfile.writer.DexDataEntryWriter;
import proguard.io.DataEntryClassWriter;
import proguard.io.DataEntryNameFilter;
import proguard.io.DataEntryWriter;
import proguard.io.DirectoryWriter;
import proguard.io.FilteredDataEntryWriter;
import proguard.io.FixedFileWriter;
import proguard.io.util.IOUtil;
import proguard.util.ExtensionMatcher;

import java.io.File;
import java.io.IOException;

public class Writer {

    public static void main(String[] args) throws CompilationFailedException, IOException {

        File file = new File("/home/pramitha/Downloads/DexOut/output");

        // Create the writer for the main file or directory.
        DataEntryWriter writer = file.isFile() ? new FixedFileWriter(file) : new DirectoryWriter(file);

        // A dex file can't contain resource files.
        writer =
                new FilteredDataEntryWriter(
                        new DataEntryNameFilter(
                                new ExtensionMatcher("dex")),
                        writer);

        ClassPool cp = IOUtil.read("/home/pramitha/Downloads/Hello.jar", false);

        writer = new DexDataEntryWriter(2, cp, "java_classes.dex", true, writer, writer);

        cp.classesAccept(
                new DataEntryClassWriter(writer));

        writer.close();

//        System.out.println(Arrays.toString(consumer.getContents()));

    }
}
