/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2021 Guardsquare NV
 */

package testutils

import proguard.classfile.Clazz
import proguard.classfile.Member
import proguard.classfile.Method
import proguard.classfile.attribute.CodeAttribute
import proguard.classfile.attribute.visitor.AllAttributeVisitor
import proguard.classfile.instruction.Instruction
import proguard.classfile.instruction.visitor.AllInstructionVisitor
import proguard.classfile.instruction.visitor.InstructionVisitor
import proguard.classfile.util.ClassUtil.externalFullMethodDescription

data class ClazzMemberPair(val clazz: Clazz, val member: Member) {
    override fun toString(): String =
            externalFullMethodDescription(clazz.name, member.accessFlags, member.getName(clazz), member.getDescriptor(clazz))
}

// Allows creation of a `ClassMemberPair` with the following:
// val pair = Clazz and Member
infix fun Clazz.and(member: Member) = ClazzMemberPair(this, member)

fun ClazzMemberPair.instructionsAccept(visitor: InstructionVisitor) =
        member.accept(clazz, AllAttributeVisitor(AllInstructionVisitor(visitor)))

fun ClazzMemberPair.match(builder: InstructionBuilder.() -> InstructionBuilder): Boolean = with(this) {
    val (constants, instructions) = builder(InstructionBuilder())
    val matcher = InstructionMatcher(constants, instructions)

    class EarlyReturn : RuntimeException()

    try {
        instructionsAccept(object : InstructionVisitor {
            override fun visitAnyInstruction(clazz: Clazz, method: Method, codeAttribute: CodeAttribute, offset: Int, instruction: Instruction) {
                if (matcher.isMatching) throw EarlyReturn()
                instruction.accept(clazz, method, codeAttribute, offset, matcher)
            }
        })
    } catch (ignored: EarlyReturn) { }

    return matcher.isMatching
}
