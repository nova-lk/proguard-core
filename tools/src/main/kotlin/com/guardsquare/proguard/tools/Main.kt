package com.guardsquare.proguard.tools

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import proguard.classfile.ClassPool
import proguard.classfile.visitor.ClassNameFilter
import proguard.classfile.visitor.ClassPoolFiller
import proguard.io.*
import proguard.util.ExtensionMatcher
import proguard.util.OrMatcher
import java.io.File
import java.io.IOException

val parser = ArgParser("proguard-core-tools")
@ExperimentalCli
fun main(args: Array<String>) {

    class Dex2Jar : Subcommand("dex2jar", "Convert dex to jar") {
        override fun execute() {
            TODO("Not yet implemented")
        }
    }

    class Jar2Dex : Subcommand("jar2dex", "Convert jar to dex") {
        override fun execute() {
            TODO("Not yet implemented")
        }
    }

    val dex2jar = Dex2Jar()
    val jar2Dex = Jar2Dex()
    parser.subcommands(dex2jar, jar2Dex)
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
    var classReader: DataEntryReader = NameFilteredDataEntryReader(
        "**.class",
        ClassReader(
            isLibrary, false, false, false, null,
            ClassNameFilter(
                classNameFilter,
                ClassPoolFiller(classPool)
            )
        )
    )

    // Convert dex files to a JAR first.
    classReader = NameFilteredDataEntryReader(
        "classes*.dex",
        Dex2JarReader(
            !isLibrary,
            classReader
        ),
        classReader
    )

//         Extract files from an archive if necessary.
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