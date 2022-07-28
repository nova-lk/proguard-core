package proguard.dexfile

import SmaliSource
import fromSmali
import getAllSmaliResources
import io.kotest.core.spec.style.FreeSpec
import proguard.classfile.ClassPool
import proguard.classfile.Clazz
import proguard.classfile.Member
import proguard.classfile.ProgramClass
import proguard.classfile.ProgramMethod
import proguard.classfile.visitor.AllMethodVisitor
import proguard.classfile.visitor.ClassPrinter
import proguard.classfile.visitor.MemberVisitor
import testutils.ClassPoolBuilder


class Smali2JarTest : FreeSpec({

    val smalis = getAllSmaliResources()
    
    "Print all methods in smali files" - {
        smalis.forEach {
            println(it.name)
            val (programClassPool, LibraryClassPool) = ClassPoolBuilder.fromSmali(
                SmaliSource(it.name, it.readText())
            )
            
//            programClassPool.classesAccept(
//                    AllMethodVisitor(
//                    MyMethodPrinter()
//                    )
//                )
            programClassPool.classesAccept(ClassPrinter())
            println()
            println("=================================================================================================")
            println()
        }
    }

})

class MyMethodPrinter() : MemberVisitor {
    override fun visitAnyMember(clazz: Clazz?, member: Member?) {}
    override fun visitProgramMethod(programClass: ProgramClass, programMethod: ProgramMethod) {
        println(
            programClass.name + "." +
                programMethod.getName(programClass) +
                programMethod.getDescriptor(programClass)
        )
    }
}