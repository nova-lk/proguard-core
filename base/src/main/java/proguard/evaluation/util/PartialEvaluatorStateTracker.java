/*
 * ProGuardCORE -- library to process Java bytecode.
 *
 * Copyright (c) 2002-2023 Guardsquare NV
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

package proguard.evaluation.util;

import java.util.List;
import proguard.classfile.Clazz;
import proguard.classfile.Method;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.ExceptionInfo;
import proguard.classfile.instruction.Instruction;
import proguard.evaluation.BasicBranchUnit;
import proguard.evaluation.PartialEvaluator;
import proguard.evaluation.TracedStack;
import proguard.evaluation.TracedVariables;
import proguard.evaluation.Variables;
import proguard.evaluation.value.InstructionOffsetValue;

/**
 * Interface with callback methods called by the Partial Evaluator when it enters certain states.
 * States are visualised in the Partial evaluator docs. Implementers of this interface are allowed
 * to assume a fixed order in the calls. For example: startCodeAttribute is followed with a
 * startInstructionBlock.
 */
public interface PartialEvaluatorStateTracker {
  // region Code attribute level

  /** The partial evaluator starts with the evaluation of a code block. */
  default void startCodeAttribute(
      Clazz clazz, Method method, CodeAttribute codeAttribute, Variables parameters) {}

  /** An exception has been thrown while evaluating the current code attribute. */
  default void registerException(
      Clazz clazz,
      Method method,
      CodeAttribute codeAttribute,
      PartialEvaluator evaluator,
      Throwable cause) {}

  // endregion

  // region Exception handling

  /**
   * Partial evaluator starts evaluating the exceptions handlers that catch over a certain
   * instruction range.
   */
  default void startExceptionHandlingForBlock(
      Clazz clazz, Method method, int startOffset, int endOffset) {}

  /** Partial evaluator starts evaluating a specific exception handler. */
  default void registerExceptionHandler(
      Clazz clazz, Method method, int startPC, int endPC, ExceptionInfo info) {}

  /**
   * Partial evaluator detects that the evaluation of the exception handler over a certain range is
   * not needed. For example when the exceptionType can not be thrown from with the instruction
   * range.
   */
  default void registerUnusedExceptionHandler(
      Clazz clazz, Method method, int startPC, int endPC, ExceptionInfo info) {}

  // endregion

  // region Results

  /**
   * Partial evaluator is done evaluating the code attribute. You can extract the results straight
   * from the evaluator.
   */
  default void evaluationResults(
      Clazz clazz, Method method, CodeAttribute codeAttribute, PartialEvaluator evaluator) {}

  // endregion

  // region Instruction block level

  /**
   * Partial evaluator starts evaluating an instruction block, specified by an instruction range
   * within the code attribute and the variables and stack it would start with.
   */
  default void startInstructionBlock(
      Clazz clazz,
      Method method,
      CodeAttribute codeAttribute,
      TracedVariables startVariables,
      TracedStack startStack,
      int startOffset) {}

  /**
   * Evaluation of the first general block has been done. The evaluator now starts evaluating the
   * blocks generated by branch instructions and will do this in a stack like behaviour.
   */
  default void startBranchCodeBlockEvaluation(
      List<PartialEvaluator.InstructionBlock> branchStack) {}

  /** An instruction block has been evaluated. */
  default void instructionBlockDone(
      Clazz clazz,
      Method method,
      CodeAttribute codeAttribute,
      TracedVariables startVariables,
      TracedStack startStack,
      int startOffset) {}

  // endregion

  // region Instruction level

  /**
   * The instruction within this instruction block has been seen with the those variables and this
   * stack, and thus it is not needed to evaluate this block any further.
   */
  default void skipInstructionBlock(
      Clazz clazz,
      Method method,
      int instructionOffset,
      Instruction instruction,
      TracedVariables variablesBefore,
      TracedStack stackBefore,
      int evaluationCount) {}

  /**
   * The instruction has been seen a certain number of times and the evaluator deems it time to
   * start generalizing the instruction.
   */
  default void generalizeInstructionBlock(
      Clazz clazz,
      Method method,
      int instructionOffset,
      Instruction instruction,
      TracedVariables variablesBefore,
      TracedStack stackBefore,
      int evaluationCount) {}

  /**
   * The partial evaluator will forward the evaluation of the instruction. An exception right after
   * this state is likely.
   */
  default void startInstructionEvaluation(
      Clazz clazz,
      Method method,
      int instructionOffset,
      Instruction instruction,
      TracedVariables variablesBefore,
      TracedStack stackBefore,
      int evaluationCount) {}

  /** The instruction is evaluated and branching information is obtained. */
  default void afterInstructionEvaluation(
      Clazz clazz,
      Method method,
      int instructionOffset,
      Instruction instruction,
      TracedVariables variablesAfter,
      TracedStack stackAfter,
      BasicBranchUnit branchUnit,
      InstructionOffsetValue branchTarget) {}

  /**
   * The instruction causes a definitive branch. This means the partial evaluator discovered a jump
   * is used and is sure what the next instruction will be. This next instruction will be evaluated
   * next and within the same instructionBlock evaluation.
   */
  default void definitiveBranch(
      Clazz clazz,
      Method method,
      int instructionOffset,
      Instruction instruction,
      TracedVariables variablesAfter,
      TracedStack stackAfter,
      InstructionOffsetValue branchTargets) {}

  /**
   * The evaluator detects multiple branches need to be evaluated. It adds these block evaluations
   * to a stack of blocks that still need to be evaluated. After all branches have been registered,
   * the evaluator exits the evaluation of the current instruction block.
   */
  default void registerAlternativeBranch(
      Clazz clazz,
      Method method,
      int fromInstructionOffset,
      Instruction fromInstruction,
      TracedVariables variablesAfter,
      TracedStack stackAfter,
      int branchIndex,
      int branchTargetCount,
      int offset) {}

  // endregion

  // region subroutines

  /**
   * The current instruction was JSR or JSR_W and the partial evaluator starts with the evaluation
   * of the subroutine.
   */
  default void startSubroutine(
      Clazz clazz,
      Method method,
      TracedVariables startVariables,
      TracedStack startStack,
      int subroutineStart,
      int subroutineEnd) {}

  /**
   * The current instruction was RET and the partial evaluator pushes the return address to the
   * branch stack of the calling partial evaluator.
   */
  default void registerSubroutineReturn(
      Clazz clazz,
      Method method,
      int returnOffset,
      TracedVariables returnVariables,
      TracedStack returnStack) {}

  /** The partial evaluator will start generalizing the results of the evaluated subroutine. */
  default void generalizeSubroutine(
      Clazz clazz,
      Method method,
      TracedVariables startVariables,
      TracedStack startStack,
      int subroutineStart,
      int subroutineEnd) {}

  /**
   * The evaluation and generalization of the subroutine is done, the partial evaluator moves on.
   */
  default void endSubroutine(
      Clazz clazz,
      Method method,
      TracedVariables variablesAfter,
      TracedStack stackAfter,
      int subroutineStart,
      int subroutineEnd) {}

  // endregion
}
