// This test checks whether DexGuard correctly converts inner classes
// attributes.
//
// Compile the java source code to a dex file.
// RUN: %javac -d %t %s
//
// RUN: %jar -cf %t/classes-java.jar        \
// RUN:      -C %t TestDexBypassing.class   \
// RUN:      -C %t TestDexBypassing$A.class \
// RUN:      -C %t TestDexBypassing$B.class \
// RUN:      -C %t TestDexBypassing$C.class
//
// RUN: %d8 --release                      \
// RUN:     --output %t/classes-dalvik.jar \
// RUN:     --lib %android                 \
// RUN:     %t/classes-java.jar
//
// Convert the dex file and dump the processed class files with DexGuard.
// RUN: %dexguard -dalvik                                  \
// RUN:           -injars %t/classes-dalvik.jar            \
// RUN:           -outjars %t/out.jar                      \
// RUN:           -libraryjars %android                    \
// RUN:           -dontoptimize                            \
// RUN:           -dontobfuscate                           \
// RUN:           -dontshrink                              \
// RUN:           -forceprocessing                         \
// RUN:           -bypassdexprocessing class %basename_s$B \
// RUN:           -keep class %basename_s { public static void main(java.lang.String[])\; }

// RUN: %dalvikvm -cp %t/out.jar %basename_s | FileCheck %s -check-prefix OUTPUT

// OUTPUT: 42

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TestDexBypassing
{

    public static void main(String[] args)
    {
        System.out.println(new A().foo());
    }

    static class A
    {
        public String foo()
        {
            return new B().foo();
        }
    }

    static class B
    {
        public String foo()
        {
            return new C().foo();
        }
    }

    static class C
    {
        public String foo()
        {
            return "42";
        }
    }
}
