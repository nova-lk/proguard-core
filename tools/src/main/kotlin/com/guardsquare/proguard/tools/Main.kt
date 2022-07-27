package com.guardsquare.proguard.tools

import dexreader.reader.DexClassReader
import kotlinx.cli.*
import proguard.classfile.*
import proguard.classfile.visitor.*
import proguard.io.*
import proguard.util.ExtensionMatcher
import proguard.util.OrMatcher
import java.io.Closeable
import java.io.File
import java.io.OutputStream

val parser = ArgParser("proguard-core-tools")
@ExperimentalCli
fun main(args: Array<String>) {

    class DexPrinter : Subcommand("dumpclasses", "Dump all the classes") {

        var input by argument(ArgType.String, description = "Input file name")

        override fun execute() {

            val programClassPool = readJar(input, "**", false)

            programClassPool?.classesAccept(ClassPrinter())

        }
//        inner class MyMethodPrinter : MemberVisitor {
//            override fun visitAnyMember(clazz: Clazz, member: Member) {}
//            override fun visitProgramMethod(programClass: ProgramClass, programMethod: ProgramMethod) {
//                println(
//                    programClass.name + "." +
//                            programMethod.getName(programClass) +
//                            programMethod.getDescriptor(programClass)
//                )
//            }
//        }

    }

    class Dex2Jar : Subcommand("dex2jar", "Convert dex to jar") {

        var input by argument(ArgType.String, description = "Input file name")
        var output by option(ArgType.String, description = "Output file name", shortName = "o").default("dex2jar_output.jar")

        override fun execute() {
            val programClassPool = readJar(input, "**", false)
            writeJar(programClassPool!!, (File(output)), "Main")
        }
        private fun writeJar(programClassPool: ClassPool, file: File, mainClass: String) {
            class MyJarWriter(zipEntryWriter: DataEntryWriter) : JarWriter(zipEntryWriter), Closeable {
                override fun createManifestOutputStream(manifestEntry: DataEntry): OutputStream {
                    val outputStream = super.createManifestOutputStream(manifestEntry)
                    outputStream.writer().apply {
                        appendLine("Main-Class: $mainClass")
                        flush()
                    }
                    return outputStream
                }

                override fun close() {
                    super.close()
                }
            }

            val jarWriter = MyJarWriter(
                ZipWriter(
                    FixedFileWriter(
                        file
                    )
                )
            )

            jarWriter.use {
                programClassPool.classesAccept(
                    DataEntryClassWriter(it)
                )
            }
        }
    }

    // TODO
    class Jar2Dex : Subcommand("jar2dex", "Convert jar to dex") {

        var input by argument(ArgType.String, description = "Input file name")
        var output by option(ArgType.String, description = "Output file name")
        override fun execute() {
            TODO("Not yet implemented")
        }
    }

    val dex2jar = Dex2Jar()
    val jar2Dex = Jar2Dex()
    val dumpclaases = DexPrinter()

    parser.subcommands(dex2jar, jar2Dex, dumpclaases)
    parser.parse(args)

}



fun readJar(
    jarFileName: String?,
    classNameFilter: String?,
    isLibrary: Boolean
): ClassPool? {
    val classPool = ClassPool()

    // Parse all classes from the input jar and
    // collect them in the class pool.
    val source: DataEntrySource = FileSource(
        File(jarFileName)
    )

    val acceptedClasssVisitor = ClassPoolFiller(classPool)

    var classReader: DataEntryReader = NameFilteredDataEntryReader(
        "**.class",
        ClassReader(
            isLibrary, false, false, false, null,
            ClassNameFilter(
                classNameFilter,
                acceptedClasssVisitor
            )
        )
    )

    // Convert dex files to a JAR first.
    classReader = NameFilteredDataEntryReader(
        "classes*.dex",
        DexClassReader(
            !isLibrary,
            acceptedClasssVisitor
        ),
        classReader)

    // Extract files from an archive if necessary.
    classReader = FilteredDataEntryReader(
        DataEntryNameFilter(ExtensionMatcher("aar")),
        JarReader(
            NameFilteredDataEntryReader(
                "classes.jar",
                JarReader(classReader)
            )
        ),
        FilteredDataEntryReader(
            DataEntryNameFilter(
                OrMatcher(
                    ExtensionMatcher("jar"),
                    ExtensionMatcher("zip"),
                    ExtensionMatcher("apk")
                )
            ),
            JarReader(classReader),
            classReader
        )
    )
    source.pumpDataEntries(classReader)
    return classPool
}