package proguard.dexfile

import SmaliSource
import fromSmali
import getSmaliResource
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldNotBe
import testutils.ClassPoolBuilder

class MethodCodeTooLargeTest : FreeSpec({

    "Method code too large test" - {
        val smaliFile = getSmaliResource("Large.smali")
        val (programClassPool, _) = ClassPoolBuilder.fromSmali(SmaliSource(smaliFile.name, smaliFile.readText()))

        val testClass = programClassPool.getClass("code/Large")

        "Check if classPool is not null" {
            programClassPool shouldNotBe null
        }

        "Check if classPool contains class code/Large" {
            testClass shouldNotBe null
        }
    }
})
