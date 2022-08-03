// T5788: Tests that passing an APK as a -libraryjar correctly initializes the
//        library classpool. The library class (TestDexLibraryJar) is referenced
//        from a program class (Foo), so the final reduced library class pool
//        should have size 1.
//
// RUN: %javac -d %t %s
//
// RUN: %jar -cf %t/library-java.jar \
// RUN:      -C %t %basename_s.class
//
// RUN: %jar -cf %t/classes-java.jar \
// RUN:      -C %t Foo.class
//
// RUN: %d8 --release \
// RUN:     --output %t/library-dalvik.jar \
// RUN:     --lib %android \
// RUN:     %t/library-java.jar
//
// RUN: mv %t/library-dalvik.jar %t/library-dalvik.apk
//
// Dump the original class files with DexGuard.
// RUN: %dexguard -dontoptimize \
// RUN:           -dontshrink \
// RUN:           -ignorewarnings \
// RUN:           -injars %t/classes-java.jar \
// RUN:           -libraryjars %t/library-dalvik.apk \
// RUN:           -verbose \
// RUN:           -dalvik \
// RUN:           -keep class '*' \
// RUN:           | FileCheck %s --check-prefix DG

// DG: Final number of library classes:            1

public class TestDexLibraryJar {
    public static void main(String[] args) {
        System.out.println("The answer is 42");
    }
}

class Foo extends TestDexLibraryJar { }
