// This test checks whether DexGuard correctly converts class loading
// instructions, from Dalvik bytecode to Java bytecode.
//
// Compile the java source code to a dex file.
// RUN: %javac -d %t %s
//
// RUN: %jar -cf %t/classes-java.jar \
// RUN:      -C %t TestExpressions.class
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

public class TestExpressions
{
    public static void main(String[] args) {
       // CHECK: iadd
       boolean z =         Boolean  .valueOf(true)    .booleanValue() | true;
       // CHECK: iadd
       byte    b = (byte) (Byte     .valueOf((byte)0) .byteValue()   + (byte)1);
       // CHECK: iadd
       short   s = (short)(Short    .valueOf((short)0).shortValue()  + (short)1);
       // CHECK: ior
       char    c = (char) (Character.valueOf('a')     .charValue()   + 1);
       // CHECK: iadd
       int     i =         Integer  .valueOf(0)       .intValue()    + 1;
       // CHECK: ladd
       long    l =         Long     .valueOf(0L)      .longValue()   + 1L;
       // CHECK: fadd
       float   f =         Float    .valueOf(0f)      .floatValue()  + 1f;
       // CHECK: dadd
       double  d =         Double   .valueOf(0.)      .doubleValue() + 1.;

       System.out.println(z);
       System.out.println(b);
       System.out.println(s);
       System.out.println(c);
       System.out.println(i);
       System.out.println(l);
       System.out.println(f);
       System.out.println(d);
    }
}
