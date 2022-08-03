// This test checks whether DexGuard correctly converts class loading
// instructions, from Dalvik bytecode to Java bytecode.
//
// Compile the java source code to a dex file.
// RUN: %javac -d %t %s
//
// RUN: %jar -cf %t/classes-java.jar \
// RUN:      -C %t TestDotClass.class
//
// RUN: %d8 --release \
// RUN:     --output %t/classes-dalvik.jar \
// RUN:     --lib %android \
// RUN:     %t/classes-java.jar
//
// Dump the original class files with DexGuard.
// ___: %dexguard -dontoptimize \
// ___:           -dontshrink \
// ___:           -dontobfuscate \
// ___:           -injars %t/classes-java.jar \
// ___:           -libraryjars %android \
// ___:           -verbose \
// ___:           -dump %t/dump-original.txt
//
// Convert the dex file and dump the processed class files with DexGuard.
// RUN: %dexguard -dalvik \
// RUN:           -dontoptimize \
// RUN:           -dontshrink \
// RUN:           -dontobfuscate \
// RUN:           -injars %t/classes-dalvik.jar \
// RUN:           -libraryjars %android \
// RUN:           -verbose \
// RUN:           -dump %t/dump-processed.txt
//
// RUN: cat %t/dump-processed.txt | FileCheck %s
public class TestDotClass
{
    public static void main(String[] args) {
        // CHECK: ldc #{{[0-9]+}} = Class(java/lang/String)
        System.out.println(String.class);
        // CHECK: ldc #{{[0-9]+}} = Class([Ljava/lang/String;)
        System.out.println(String[].class);
        // Note:                           v-- Avoid collision with FileCheck regex syntax
        // CHECK: ldc #{{[0-9]+}} = Class({{[[][[]}}Ljava/lang/String;)
        System.out.println(String[][].class);

        // CHECK: getstatic #{{[0-9]+}} = Fieldref(java/lang/Integer.TYPE Ljava/lang/Class;)
        System.out.println(int.class);
        // CHECK: ldc #{{[0-9]+}} = Class([I)
        System.out.println(int[].class);
        // Note:                           v-- Avoid collision with FileCheck regex syntax
        // CHECK: ldc #{{[0-9]+}} = Class({{[[][[]}}I)
        System.out.println(int[][].class);
    }
}
