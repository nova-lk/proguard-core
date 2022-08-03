// This test checks whether DexGuard correctly converts inner classes
// attributes.
//
// Compile the java source code to a dex file.
// RUN: %javac -d %t %s
//
// RUN: %jar -cf %t/classes-java.jar \
// RUN:      -C %t OuterClass.class \
// RUN:      -C %t OuterClass$MiddleClass.class \
// RUN:      -C %t OuterClass$MiddleClass$InnerClass.class \
// RUN:      -C %t OuterClass$MiddleClass$InnerClass$1.class \
// RUN:      -C %t OuterClass$MiddleClass$InnerClass$2.class \
// RUN:      -C %t OuterClass$MiddleClass$InnerClass$1EnclosedClass.class
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

// Test whether the inner class and enclosing method attributes were correctly converted.
// RUN: cat %t/dump-processed.txt | FileCheck %s

// CHECK: Program class: OuterClass
// CHECK: Inner classes attribute (count = 1):
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x8 = static
// CHECK-NEXT: Class [OuterClass$MiddleClass]
// CHECK-NEXT: Class [OuterClass]
// CHECK-NEXT: Utf8 [MiddleClass]


// CHECK: Program class: OuterClass$MiddleClass
// CHECK: Inner classes attribute (count = 2):
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x8 = static
// CHECK-NEXT: Class [OuterClass$MiddleClass]
// CHECK-NEXT: Class [OuterClass]
// CHECK-NEXT: Utf8 [MiddleClass]
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x8 = static
// CHECK-NEXT: Class [OuterClass$MiddleClass$InnerClass]
// CHECK-NEXT: Class [OuterClass$MiddleClass]
// CHECK-NEXT: Utf8 [InnerClass]


// CHECK: Program class: OuterClass$MiddleClass$InnerClass
// CHECK: Inner classes attribute (count = 5):
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x8 = static
// CHECK-NEXT: Class [OuterClass$MiddleClass]
// CHECK-NEXT: Class [OuterClass]
// CHECK-NEXT: Utf8 [MiddleClass]
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x8 = static
// CHECK-NEXT: Class [OuterClass$MiddleClass$InnerClass]
// CHECK-NEXT: Class [OuterClass$MiddleClass]
// CHECK-NEXT: Utf8 [InnerClass]
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x0 =
// CHECK-NEXT: Class [OuterClass$MiddleClass$InnerClass$1]
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x0 =
// CHECK-NEXT: Class [OuterClass$MiddleClass$InnerClass$1EnclosedClass]
// CHECK-NEXT: Utf8 [EnclosedClass]
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x0 =
// CHECK-NEXT: Class [OuterClass$MiddleClass$InnerClass$2]

// CHECK: Program class: OuterClass$MiddleClass$InnerClass$1
// CHECK: Enclosing method attribute:
// CHECK-NEXT: Class [OuterClass$MiddleClass$InnerClass]
// CHECK-NEXT: Inner classes attribute (count = 3):
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x8 = static
// CHECK-NEXT: Class [OuterClass$MiddleClass]
// CHECK-NEXT: Class [OuterClass]
// CHECK-NEXT: Utf8 [MiddleClass]
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x8 = static
// CHECK-NEXT: Class [OuterClass$MiddleClass$InnerClass]
// CHECK-NEXT: Class [OuterClass$MiddleClass]
// CHECK-NEXT: Utf8 [InnerClass]
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x0 =
// CHECK-NEXT: Class [OuterClass$MiddleClass$InnerClass$1]


// CHECK: Program class: OuterClass$MiddleClass$InnerClass$1EnclosedClass
// CHECK: Enclosing method attribute:
// CHECK-NEXT: Class [OuterClass$MiddleClass$InnerClass]
// CHECK-NEXT: NameAndType [enclosingMethod ()V]
// CHECK-NEXT: Inner classes attribute (count = 3):
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x8 = static
// CHECK-NEXT: Class [OuterClass$MiddleClass]
// CHECK-NEXT: Class [OuterClass]
// CHECK-NEXT: Utf8 [MiddleClass]
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x8 = static
// CHECK-NEXT: Class [OuterClass$MiddleClass$InnerClass]
// CHECK-NEXT: Class [OuterClass$MiddleClass]
// CHECK-NEXT: Utf8 [InnerClass]
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x0 =
// CHECK-NEXT: Class [OuterClass$MiddleClass$InnerClass$1EnclosedClass]
// CHECK-NEXT: Utf8 [EnclosedClass]


// CHECK: Program class: OuterClass$MiddleClass$InnerClass$2
// CHECK: Enclosing method attribute:
// CHECK-NEXT: Class [OuterClass$MiddleClass$InnerClass]
// CHECK-NEXT: NameAndType [enclosingMethod ()V]
// CHECK-NEXT: Inner classes attribute (count = 3):
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x8 = static
// CHECK-NEXT: Class [OuterClass$MiddleClass]
// CHECK-NEXT: Class [OuterClass]
// CHECK-NEXT: Utf8 [MiddleClass]
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x8 = static
// CHECK-NEXT: Class [OuterClass$MiddleClass$InnerClass]
// CHECK-NEXT: Class [OuterClass$MiddleClass]
// CHECK-NEXT: Utf8 [InnerClass]
// CHECK-NEXT: InnerClassesInfo:
// CHECK-NEXT: Access flags:  0x0 =
// CHECK-NEXT: Class [OuterClass$MiddleClass$InnerClass$2]

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TestNestedClasses
{
}

class OuterClass
{
    static class MiddleClass
    {
        static class InnerClass
        {
            private Object anonymous = new Object() {};

            void enclosingMethod()
            {
                class EnclosedClass
                {
                }

                Object anonynous = new Object() {};
            }
        }
    }
}
