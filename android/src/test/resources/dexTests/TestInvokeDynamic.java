// This test checks whether DexGuard correctly converts invokedynamic calls
// and bootstrap attributes, from Dalvik bytecode to Java bytecode.
//
// We need a non-obfuscated copy of DexGuard.
// REQUIRES: UNOBFUSCATED-DEXGUARD
//
// Compile the java source code to a dex file.
// RUN: %javac -d %t %s
//
// RUN: %jar -cf %t/classes-java.jar \
// RUN:      -C %t TestInvokeDynamic.class
//
// RUN: %d8 --release \
// RUN:     --min-api 26 \
// RUN:     --no-desugaring \
// RUN:     --output %t/classes-dalvik.jar \
// RUN:     --lib %android \
// RUN:     %t/classes-java.jar
//
// Dump the original class files with DexGuard.
// ___: %dexguard -dontoptimize \
// ___:           -dontshrink \
// ___:           -dontobfuscate \
// ___:           -injars %t/classes-java.jar \
// ___:           -libraryjars %javahome/jmods/java.base.jmod' \
// ___:           -verbose \
// ___:           -dump %t/dump-original.txt
// ___: %java -cp %dexguardjar proguard.io.ClassReader \
// ___:           %t/classes-java.jar > %t/dump-original.txt
//
// Convert the dex file and dump the processed class files with DexGuard.
// DexGuard currently desugars the invokedynamic call, so we can't use it.
// ___: %dexguard -dalvik \
// ___:           -dontoptimize \
// ___:           -dontshrink \
// ___:           -dontobfuscate \
// ___:           -injars %t/classes-dalvik.jar \
// ___:           -libraryjars %javahome/jmods/java.base.jmod \
// ___:           -verbose \
// ___:           -dump %t/dump-processed.txt
// RUN: %java -cp %dexguardjar proguard.io.DexClassReader \
// RUN:           %t/classes-dalvik.jar > %t/dump-processed.txt
//
// RUN: cat %t/dump-processed.txt | FileCheck %s
import java.util.*;

public class TestInvokeDynamic
{
    public void test()
    {
        List a = new ArrayList();

        // CHECK: BootstrapMethodInfo (argument count = 3):
        // CHECK:   MethodHandle [kind = 6]:
        // CHECK:       Methodref [java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;]
        // CHECK:   MethodType [(Ljava/lang/Object;)Ljava/lang/Object;]
        // CHECK:   MethodHandle [kind = 5]:
        // CHECK:       Methodref [java/lang/Object.toString ()Ljava/lang/String;]
        // CHECK:   MethodType [(Ljava/lang/Object;)Ljava/lang/Object;]
        a.replaceAll(Object::toString);
    }
}
