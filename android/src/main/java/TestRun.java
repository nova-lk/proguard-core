/*
 * ProGuardCORE -- library to process Java bytecode.
 *
 * Copyright (c) 2002-2022 Guardsquare NV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.android.tools.r8.CompilationFailedException;
import proguard.classfile.ClassPool;
import proguard.classfile.VersionConstants;
import proguard.classfile.attribute.visitor.AllAttributeVisitor;
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

        File inputFile = new File("/home/pramitha/Downloads/apprelease.apk");
        File outputFile = new File("/home/pramitha/Downloads/DexOut/output/pgc_out.apk");

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



//        libraryClassPool.classesAccept(new ClassPrinter());
//        programClassPool.classesAccept(new ClassPrinter());
//        programClassPool.classesAccept(new PrimitiveArrayConstantReplacer());
        programClassPool.classesAccept(
                new ClassVersionFilter(VersionConstants.CLASS_VERSION_1_6,
                        new AllMethodVisitor(
                                new AllAttributeVisitor(
                                        new CodePreverifier(false)))));

        // arguments to be passed to the OutputWriter
        List<String> dontCompress   = inputReader.getDontCompressList();
        int multiDexCount           = 1;
        int minSdkVersion           = 24;
        boolean  debuggable         = false;

        new OutputWriter(programJars, libraryjars, dontCompress, multiDexCount, minSdkVersion, debuggable)
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