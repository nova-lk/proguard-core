/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2021 Guardsquare NV
 */

// Build and run.

// REQUIRES: DALVIKVM

// Compile.
// RUN: javac %s -d %t
// RUN: %dexguard @rules.pro -injars %t -outjars %t.apk -dalvik -dontshrink -dontoptimize -dontobfuscate

// Process.
// RUN: %dexguard @rules.pro -injars %t.apk -outjars %t-processed.apk -dalvik -dontshrink -dontoptimize -dontobfuscate

// Run.
// RUN: %java     -cp %t               %basename_s | FileCheck %s -check-prefix OUTPUT
// RUN: %dalvikvm -cp %t.apk           %basename_s | FileCheck %s -check-prefix OUTPUT
// RUN: %dalvikvm -cp %t-processed.apk %basename_s | FileCheck %s -check-prefix OUTPUT

// OUTPUT: The answer is 1

/**
 * This class tests conversion of initialized primitive arrays fixed by #3543.
 */
public class TestFillArrayTransformer {

    public static void main(String[] args) {
        int[] ints = new int[1];
        ints[0] = 1;
        int sum = 0;
        for (int i = 0; i < 4; i++)
        {
            if (methodWithSideEffect(ints))
            {
                sum += 1;
            }
        }
        System.out.println("The answer is " + sum);
    }

    public static boolean methodWithSideEffect(int[] ints)
    {
        ints[0] = ints[0] * 2;
        return (ints[0] == 2);
    }
}
