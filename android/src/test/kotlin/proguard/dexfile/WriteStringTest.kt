package proguard.dexfile

import SmaliSource
import fromSmali
import getSmaliResource
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldNotBe
import proguard.classfile.Clazz
import proguard.classfile.Method
import proguard.classfile.attribute.CodeAttribute
import proguard.classfile.attribute.visitor.AllAttributeVisitor
import proguard.classfile.instruction.Instruction
import proguard.classfile.instruction.visitor.AllInstructionVisitor
import proguard.classfile.instruction.visitor.InstructionVisitor
import proguard.classfile.util.InstructionSequenceMatcher
import runClassPool
import testutils.ClassPoolBuilder
import testutils.InstructionBuilder

class WriteStringTest : FreeSpec ({
    "Write string test" - {
    val smaliFile = getSmaliResource("writeString.smali")
    val (programClassPool, libraryClassPool) = ClassPoolBuilder.fromSmali(SmaliSource(smaliFile.name, smaliFile.readText()))
//        programClassPool.classesAccept(ClassPrinter())

    val testClass = programClassPool.getClass("DD")

    "Check if classPool is not null" - {
        programClassPool
            .shouldNotBe(null)
    }

    "Check if classPool has class DD" - {
        testClass
            .shouldNotBe(null)
    }

    "Check if class ML has method writeString" - {
        testClass
            .findMethod("writeString", "(Ljava/lang/String;[BIZ)I")
            .shouldNotBe(null)
    }

//    "exec check" - {
//
//        runClassPool(programClassPool, 1)
//
//    }


    "Check if sequence of operations after translation match original smali code" - {
        val instructionBuilder = InstructionBuilder()

        instructionBuilder
            .ldc("UTF-16LE")



        val matcher = InstructionSequenceMatcher(instructionBuilder.constants(), instructionBuilder.instructions())

        // Find the match in the code and print it out.
        class MatchPrinter : InstructionVisitor {
            override fun visitAnyInstruction(clazz: Clazz, method: Method, codeAttribute: CodeAttribute, offset: Int, instruction: Instruction) {
                println(instruction.toString(clazz, offset))
                instruction.accept(clazz, method, codeAttribute, offset, matcher)
                if (matcher.isMatching()) {
                    println("  -> matching sequence starting at [" + matcher.matchedInstructionOffset(0) + "]")
                }
            }
        }

        testClass.methodsAccept(
            AllAttributeVisitor(
                AllInstructionVisitor(
                    MatchPrinter())
            )
        )
    }
}


})