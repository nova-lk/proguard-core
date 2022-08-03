-libraryjars <java.home>/jmods/java.base.jmod

-verbose
#-dontshrink
#-dontoptimize
#-dontobfuscate
-forceprocessing

-keep public class Test* {
    public static void main(java.lang.String[]);
}
