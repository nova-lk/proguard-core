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
import testutils.ClassPoolBuilder
import testutils.InstructionBuilder

class GoToFirstLabelTest : FreeSpec({
    "Go to first label test" -{
        val smaliFile = getSmaliResource("goto-first-label.smali")
        val (programClassPool, _) = ClassPoolBuilder.fromSmali(SmaliSource(smaliFile.name, smaliFile.readText()))
//        programClassPool.classesAccept(ClassPrinter())

        val testClass = programClassPool.getClass("xgoto/first/label")

        "Check if classPool is not null" - {
            programClassPool
                .shouldNotBe(null)
        }

        "Check if classPool contains xgoto/first/label class" - {
            testClass
                .shouldNotBe(null)
        }

        "Check if i contains assertSlept method" - {
            testClass
                .findMethod("assertSlept", "()V")
                .shouldNotBe(null)
        }

        "Check if i contains g2 method" - {
            testClass
                .findMethod("g2", "(LObj;)V")
                .shouldNotBe(null)
        }

        "Check if sequence of operations after translation match original smali code" - {
            val instructionBuilder = InstructionBuilder()

            instructionBuilder
                .getstatic("A","sleepSemaphore","Ljava/util/concurrent/Semaphore;")
                .invokevirtual("java/util/concurrent/Semaphore","availablePermits","()I")
                .ifne(4)
                .return_()
                .ldc2_w(50L)
                .invokestatic("java/lang/Thread", "sleep", "(J)V")
                .goto_(-16)
                .aload(0)
                .invokevirtual("Obj","next","()LObj;")
                .astore(0)
                .aload(0)
                .ifnonnull(4)
                .return_()
                .aload(0)
                .invokevirtual("Obj","next","()LObj;")
                .astore(0)
                .goto_(-15)

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