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


@ExperimentalCli
fun main(args: Array<String>) {

    val parser = ArgParser("proguard-core-tools")

    class PrintCmd : Subcommand("print", "Print content") {

        var input by argument(ArgType.String, description = "Input file name")
        val programClassPool: ClassPool by lazy { readJar(input, "**", false) }

        init { subcommands( ClassPrinterCmd(), MethodPrinterCmd(), FieldPrinterCmd() ) }
        override fun execute() { }

        inner class ClassPrinterCmd : Subcommand("classes", "Print all the classes") {
            override fun execute() { programClassPool.classesAccept(ClassPrinter()) }
        }

        inner class MethodPrinterCmd : Subcommand("methods", "Print all the methods") {
            override fun execute() { programClassPool.classesAccept(AllMethodVisitor(MyMethodPrinter())) }
            inner class MyMethodPrinter : MemberVisitor {
                override fun visitAnyMember(clazz: Clazz, member: Member) {}
                override fun visitProgramMethod(programClass: ProgramClass, programMethod: ProgramMethod) {
                    println( programClass.name + "." +
                            programMethod.getName(programClass) +
                            programMethod.getDescriptor(programClass) ) } } }

        inner class FieldPrinterCmd : Subcommand("fields", "Print all the fields") {
            override fun execute() { programClassPool.classesAccept(AllFieldVisitor(MyFieldPrinter())) }
            inner class MyFieldPrinter : MemberVisitor {
                override fun visitProgramField(programClass: ProgramClass, programField: ProgramField) {
                    println(
                        programClass.name + "." +
                                programField.getName(programClass) ) } } }
    }

    class Dex2Jar : Subcommand("dex2jar", "Convert dex to jar") {

        var input by argument(ArgType.String, description = "Input file name")
        var output by option(ArgType.String, description = "Output file name", shortName = "o", fullName = "output").default("classes.jar")
        var classNameFilter by option(ArgType.String, description = "Class name filter", shortName = "cf",fullName = "classNameFilter").default("**")
        var forceOverwrite by option(ArgType.Boolean, description = "Force file overwriting", shortName = "f", fullName = "force").default(false)

        override fun execute() {
            val programClassPool = readJar(input, classNameFilter, false)
            val file = File(output)
            if (file.exists() && !forceOverwrite) {
                System.err.println("$file exists, use --force to overwrite")
                return
            }
//            if (file.exists() && !forceOverwrite) throw FileAlreadyExistsException()

            writeJar(programClassPool, file)
        }

        private fun writeJar(programClassPool: ClassPool, file: File) {
            class MyJarWriter(zipEntryWriter: DataEntryWriter) : JarWriter(zipEntryWriter), Closeable {
                override fun close() { super.close() }
            }
            val jarWriter = MyJarWriter(ZipWriter(FixedFileWriter(file)))
            jarWriter.use { programClassPool.classesAccept(DataEntryClassWriter(it)) }
        }
    }

    class Jar2DexCmd : Subcommand("jar2dex", "Convert jar to dex - NOT YET IMPLEMENTED") {
        var input by argument(ArgType.String, description = "Input file name")
        var output by option(ArgType.String, description = "Output file name", shortName = "o", fullName = "output")
        override fun execute() {
            TODO("Not yet implemented")
        }
    }

    parser.subcommands(Dex2Jar(), PrintCmd(), Jar2DexCmd())
    parser.parse(args)
}

fun readJar(
    jarFileName: String,
    classNameFilter: String,
    isLibrary: Boolean
): ClassPool {
    val classPool = ClassPool()
    val source: DataEntrySource = FileSource(File(jarFileName))
    val acceptedClassVisitor = ClassPoolFiller(classPool)

    var classReader: DataEntryReader = NameFilteredDataEntryReader(
        "**.class",
        ClassReader(isLibrary, false, false, false, null,
            ClassNameFilter(classNameFilter, acceptedClassVisitor) ) )

    classReader = NameFilteredDataEntryReader("classes*.dex",
        DexClassReader(!isLibrary, acceptedClassVisitor), classReader)

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