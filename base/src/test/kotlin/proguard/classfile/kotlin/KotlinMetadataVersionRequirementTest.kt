/*
 * ProGuardCORE -- library to process Java bytecode.
 *
 * Copyright (c) 2002-2024 Guardsquare NV
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

package proguard.classfile.kotlin

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.spyk
import io.mockk.verify
import proguard.classfile.Clazz
import proguard.classfile.kotlin.KotlinVersionRequirementLevel.ERROR
import proguard.classfile.kotlin.KotlinVersionRequirementVersionKind.COMPILER_VERSION
import proguard.classfile.kotlin.visitor.KotlinMetadataVisitor
import proguard.classfile.kotlin.visitor.KotlinVersionRequirementVisitor
import proguard.classfile.kotlin.visitor.ReferencedKotlinMetadataVisitor
import proguard.testutils.ClassPoolBuilder
import proguard.testutils.KotlinSource

class KotlinMetadataVersionRequirementTest : BehaviorSpec({
    val (_, libraryClassPool) = ClassPoolBuilder.fromSource(
        KotlinSource(
            "Test.kt",
            """
            // Random class for the ProgramClass pool. It is difficult to create a class with a version requirement,
            // so instead we will check the kotlin/jvm/JvmDefaultWithCompatibility class which has one.
            // https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/jvm/src/kotlin/jvm/JvmDefault.kt#L48
            class T
            """.trimIndent(),
        ),
    )

    Given("a class with a versionRequirement") {
        val versionRequirementVisitor = spyk<KotlinVersionRequirementVisitor>()
        libraryClassPool.getClass("kotlin/jvm/JvmDefaultWithCompatibility").accept(
            ReferencedKotlinMetadataVisitor(
                object : KotlinMetadataVisitor {
                    override fun visitAnyKotlinMetadata(clazz: Clazz, kotlinMetadata: KotlinMetadata) {}
                    override fun visitKotlinClassMetadata(clazz: Clazz, kotlinClassKindMetadata: KotlinClassKindMetadata) {
                        kotlinClassKindMetadata.versionRequirementAccept(clazz, versionRequirementVisitor)
                    }
                },
            ),
        )

        Then("the version requirement should be visited") {
            verify {
                versionRequirementVisitor.visitAnyVersionRequirement(
                    libraryClassPool.getClass("kotlin/jvm/JvmDefaultWithCompatibility"),
                    withArg {
                        it.major shouldBe 1
                        it.minor shouldBe 6
                        it.patch shouldBe 0
                        it.level shouldBe ERROR
                        it.kind shouldBe COMPILER_VERSION
                    },
                )
            }
        }

        // TODO - Find test snippets for other Levels and  VersionKinds.
    }
})
