// This test checks whether DexGuard correctly converts class member access
// flags, from Dalvik bytecode to Java bytecode.
//
// Compile the java source code to a dex file.
// RUN: %javac -d %t %s
//
// RUN: %jar -cf %t/classes-java.jar \
// RUN:      -C %t TestClassMemberAccessFlags.class
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

// RUN: cat %t/dump-processed.txt | FileCheck %s

public abstract class TestClassMemberAccessFlags
{
    // Note: DexGuard will dump the fields out of order, which is reflected in the check order.
    // CHECK:        Field:        staticField I
    // CHECK-NEXT:   Access flags: 0x8
    // CHECK:        Field:        defaultField I
    // CHECK-NEXT:   Access flags: 0x0
    // CHECK:        Field:        finalField I
    // CHECK-NEXT:   Access flags: 0x10
    // CHECK:        Field:        privateField I
    // CHECK-NEXT:   Access flags: 0x2
    // CHECK:        Field:        protectedField I
    // CHECK-NEXT:   Access flags: 0x4
    // CHECK:        Field:        publicField I
    // CHECK-NEXT:   Access flags: 0x1
    // CHECK:        Field:        transientField I
    // CHECK-NEXT:   Access flags: 0x80
    // CHECK:        Field:        volatileField I
    // CHECK-NEXT:   Access flags: 0x40
              int defaultField;
    public    int publicField;
    private   int privateField;
    protected int protectedField;
    static    int staticField;
    final     int finalField = 0;
    volatile  int volatileField;
    transient int transientField;

    // Note: DexGuard will dump the fields out of order, which is reflected in the check order.
    // CHECK:        Method:        <init>()V
    // CHECK-NEXT:   Access flags:  0x1
    // CHECK:        Method:        privateMethod()V
    // CHECK-NEXT:   Access flags:  0x2
    // CHECK:        Method:        staticMethod()V
    // CHECK-NEXT:   Access flags:  0x8
    // CHECK:        Method:        abstractMethod()V
    // CHECK-NEXT:   Access flags:  0x400
    // CHECK:        Method:        defaultMethod()V
    // CHECK-NEXT:   Access flags:  0x0
    // CHECK:        Method:        finalMethod()V
    // CHECK-NEXT:   Access flags:  0x10
    // CHECK:        Method:        nativeMethod()V
    // CHECK-NEXT:   Access flags:  0x100
    // CHECK:        Method:        protectedMethod()V
    // CHECK-NEXT:   Access flags:  0x4
    // CHECK:        Method:        publicMethod()V
    // CHECK-NEXT:   Access flags:  0x1
                 void defaultMethod()   {}
    public       void publicMethod()    {}
    private      void privateMethod()   {}
    protected    void protectedMethod() {}
    static       void staticMethod()    {}
    final        void finalMethod()     {}
    abstract     void abstractMethod();
    native       void nativeMethod();

    // Dalvik bytecode doesn't have the synchronized flag.
    //synchronized void synchronizedMethod() {}
}
