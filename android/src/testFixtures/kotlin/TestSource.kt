import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.jf.smali.Smali
import org.jf.smali.SmaliOptions
import proguard.classfile.ClassPool
import proguard.util.ClassPoolClassLoader
import proguard.util.JarUtil
import testutils.ClassPoolBuilder
import testutils.ClassPools
import testutils.TestSource
import java.io.File
import java.lang.reflect.InvocationTargetException


class SmaliSource(val filename: String, @Language("Smali") val contents: String) : TestSource() {
    override fun asSourceFile() = SourceFile.new(filename, contents)
}

fun ClassPoolBuilder.Companion.fromSmali(smali: SmaliSource) : ClassPools {
    // smali -> baksmali -> dex file -> dex2pro -> class -> JavaSource

    val file = File.createTempFile("tmp", null)
    file.writeText(smali.contents)

    val options = SmaliOptions()
    val dexFileName = "classes.dex"
    options.outputDexFile = dexFileName
    Smali.assemble(options, file.absolutePath)

    file.deleteOnExit()

    val classPool = JarUtil.readJar(dexFileName, false)
//    val f = File(dexFileName)
//    f.deleteOnExit()
//    classPool.classesAccept(ClassPrinter())

    initialize(classPool, false)
    return ClassPools(classPool, libraryClassPool)
}


 fun runClassPool(programClassPool: ClassPool, args: Array<String>) {
    val clazzLoader = ClassPoolClassLoader(programClassPool)
    try {
        clazzLoader
            .loadClass("Main")
            .getDeclaredMethod("main", Array<String>::class.java)
            .invoke(null, args as Any)
    } catch (e: IllegalAccessException) {
        throw RuntimeException(e)
    } catch (e: InvocationTargetException) {
        throw RuntimeException(e)
    } catch (e: NoSuchMethodException) {
        throw RuntimeException(e)
    } catch (e: ClassNotFoundException) {
        throw RuntimeException(e)
    }
}
