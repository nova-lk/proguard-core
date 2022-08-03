// Build and run.

// REQUIRES: DALVIKVM

// TODO(#2464): PrimitiveArrayConstantToStringReplacer doesn't support boolean arrays yet.
// XFAIL: *

// Compile.
// RUN: javac %s -d %t | tee %t.javac.log

// Process.
// RUN: %dexguard @rules.pro -injars %t -outjars %t-java.jar           | tee %t.dexguard-java.log   | FileCheck %s -check-prefix=DEXGUARD
// RUN: %dexguard @rules.pro -injars %t -outjars %t-dalvik.jar -dalvik | tee %t.dexguard-dalvik.log | FileCheck %s -check-prefix=DEXGUARD

// Check the code.
// RUN: javap -p -v -c -cp %t            %basename_s | FileCheck %s -check-prefix=JAVAP-ORIGINAL
// RUN: javap -p -v -c -cp %t-java.jar   %basename_s | FileCheck %s -check-prefix=JAVAP-PROCESSED
// RUN: %dexdump -d -f     %t-dalvik.jar             | FileCheck %s -check-prefix=DEXDUMP-PROCESSED

// Run.
// RUN: java           -cp %t            %basename_s | FileCheck %s -check-prefix OUTPUT
// RUN: java           -cp %t-java.jar   %basename_s | FileCheck %s -check-prefix OUTPUT
// RUN: %dalvikvm      -cp %t-dalvik.jar %basename_s | FileCheck %s -check-prefix OUTPUT

// Make sure we can process the resulting Dalvik code again.
// RUN: %dexguard @rules.pro -injars %t-dalvik.jar -outjars %t-dalvik2.jar -dalvik | tee %t.dexguard-dalvik2.log | FileCheck %s -check-prefix=DEXGUARD
// RUN: %dexdump -d -f     %t-dalvik2.jar                                                                        | FileCheck %s -check-prefix=DEXDUMP-PROCESSED
// RUN: %dalvikvm      -cp %t-dalvik2.jar %basename_s                                                            | FileCheck %s -check-prefix OUTPUT

// Check the outputs.

// DEXGUARD-NOT: Unexpected error

// Java bytecode initializes a primitive array with array store instructions.
// JAVAP-ORIGINAL:  bastore
// JAVAP-PROCESSED: bastore

// Dalvik bytecode initializes a primitive array with a fill array instruction.
// DEXDUMP-PROCESSED-NOT: aput
// DEXDUMP-PROCESSED:     fill-array-data
// DEXDUMP-PROCESSED:     array-data

// OUTPUT: The answer is 42

/**
 * This class tests conversion of initialized primitive arrays.
 */
public class TestLargeBooleanArray {

  public static void main(String[] args) {

    // 4096 values.
    boolean[] array = new boolean[] {
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,
      true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false,

    };

    int answer = !array[0] ? 0 :
                  array[1] ? 1 :
                 !array[2] ? 2 :
                  array[3] ? 3 : 42;

    System.out.println("The answer is "+answer);
  }
}
