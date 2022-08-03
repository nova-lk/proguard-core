// This test checks whether DexGuard correctly converts class access flags,
// from Dalvik bytecode to Java bytecode.
//
// Compile the java source code to a dex file.
// RUN: %javac -d %t %s
//
// RUN: %jar -cf %t/classes-java.jar \
// RUN:      -C %t TestClassAccessFlags.class \
// RUN:      -C %t TestClassAccessFlags$DefaultAccess.class \
// RUN:      -C %t TestClassAccessFlags$PublicAccess.class \
// RUN:      -C %t TestClassAccessFlags$PrivateAccess.class \
// RUN:      -C %t TestClassAccessFlags$StaticAccess.class \
// RUN:      -C %t TestClassAccessFlags$AbstractAccess.class \
// RUN:      -C %t TestClassAccessFlags$InterfaceAccess.class \
// RUN:      -C %t TestClassAccessFlags$EnumAccess.class \
// RUN:      -C %t TestClassAccessFlags$AnnotationAccess.class
//
// RUN: %d8 --release \
// RUN:     --output %t/classes-dalvik.jar \
// RUN:     --lib %android \
// RUN:     %t/classes-java.jar
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

// Verify that the dumped class files have the correct access flags.
// RUN: cat %t/dump-processed.txt | FileCheck %s

// CHECK: Access flags:  0x21
// CHECK-NEXT:   = public class TestClassAccessFlags
// CHECK: Access flags:  0x420
// CHECK-NEXT   = abstract class TestClassAccessFlags$AbstractAccess extends java.lang.Object
// CHECK: Access flags:  0x2600
// CHECK-NEXT   = @interface TestClassAccessFlags$AnnotationAccess
// CHECK: Access flags:  0x20
// CHECK-NEXT   = class TestClassAccessFlags$DefaultAccess
// CHECK: Access flags:  0x4030
// CHECK-NEXT   = final enum TestClassAccessFlags$EnumAccess
// CHECK: Access flags:  0x600
// CHECK-NEXT   = interface TestClassAccessFlags$InterfaceAccess
// CHECK: Access flags:  0x20
// CHECK-NEXT   = class TestClassAccessFlags$PrivateAccess
// CHECK: Access flags:  0x21
// CHECK-NEXT   = public class TestClassAccessFlags$PublicAccess
// CHECK: Access flags:  0x20
// CHECK-NEXT   = class TestClassAccessFlags$StaticAccess

public class TestClassAccessFlags
{
    class DefaultAccess {}

    public class PublicAccess {}

    private class PrivateAccess {}

    static class StaticAccess {}

    abstract class AbstractAccess {}

    interface InterfaceAccess {}

    enum EnumAccess {}

    @interface AnnotationAccess {}
}
