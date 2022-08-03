// This test checks whether DexGuard correctly converts exception
// handlers, from Dalvik bytecode to Java bytecode.
//
// Compile the java source code to a dex file.
// RUN: %javac -d %t %s
//
// RUN: %jar -cf %t/classes-java.jar \
// RUN:      -C %t TestExceptions.class
//
// RUN: %d8 --release \
// RUN:     --output %t/classes-dalvik.jar \
// RUN:     --lib %android \
// RUN:     %t/classes-java.jar
//
// Dump the original class files with DexGuard.
// RUN: %dexguard -dontoptimize \
// RUN:           -dontshrink \
// RUN:           -dontobfuscate \
// RUN:           -injars %t/classes-java.jar \
// RUN:           -libraryjars %android \
// RUN:           -verbose \
// RUN:           -dump %t/dump-original.txt
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
// Unfortunately, the original code and the code that is converted to and from
// Dalvik diverge too much. We're checking the output against output produced
// by dex2jar+ASM. This is fairly brittle.

public class TestExceptions
{
    // RUN: FileCheck %s --check-prefix=catchException --input-file=%t/dump-processed.txt
    // catchException:      Method: catchException()V
    // catchException:      Code attribute exceptions (count = 1):
    // catchException-NEXT: ExceptionInfo (0 -> 4: 7):
    // catchException-NEXT: Class [java/lang/RuntimeException]
    public void catchException()
    {
        try
        {
            System.getProperties();
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
        }
    }

    // RUN: FileCheck %s --check-prefix=catchMultipleExceptions --input-file=%t/dump-processed.txt
    // catchMultipleExceptions:      Method: catchMultipleExceptions()V
    // catchMultipleExceptions:      Code attribute exceptions (count = 3):
    // catchMultipleExceptions-NEXT: ExceptionInfo (0 -> 4: 23):
    // catchMultipleExceptions-NEXT: Class [java/lang/IllegalStateException]
    // catchMultipleExceptions-NEXT: ExceptionInfo (0 -> 4: 15):
    // catchMultipleExceptions-NEXT: Class [java/lang/IllegalArgumentException]
    // catchMultipleExceptions-NEXT: ExceptionInfo (0 -> 4: 7):
    // catchMultipleExceptions-NEXT: Class [java/lang/UnsupportedOperationException]
    public void catchMultipleExceptions()
    {
        try
        {
            System.getProperties();
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedOperationException e)
        {
            e.printStackTrace();
        }
    }

    // RUN: FileCheck %s --check-prefix=catchNestedExceptions --input-file=%t/dump-processed.txt
    // catchNestedExceptions:      Method: catchNestedExceptions()V
    // catchNestedExceptions:      Code attribute exceptions (count = 8):
    // catchNestedExceptions-NEXT: ExceptionInfo (0 -> 4: 23):
    // catchNestedExceptions-NEXT: Class [java/lang/IllegalArgumentException]
    // catchNestedExceptions-NEXT: ExceptionInfo (0 -> 4: 19):
    // catchNestedExceptions-NEXT: Class [java/lang/UnsupportedOperationException]
    // catchNestedExceptions-NEXT: ExceptionInfo (4 -> 8: 11):
    // catchNestedExceptions-NEXT: Class [java/lang/IllegalStateException]
    // catchNestedExceptions-NEXT: ExceptionInfo (4 -> 8: 23):
    // catchNestedExceptions-NEXT: Class [java/lang/IllegalArgumentException]
    // catchNestedExceptions-NEXT: ExceptionInfo (4 -> 8: 19):
    // catchNestedExceptions-NEXT: Class [java/lang/UnsupportedOperationException]
    // catchNestedExceptions-NEXT: ExceptionInfo (12 -> 16: 23):
    // catchNestedExceptions-NEXT: Class [java/lang/IllegalArgumentException]
    // catchNestedExceptions-NEXT: ExceptionInfo (12 -> 16: 19):
    // catchNestedExceptions-NEXT: Class [java/lang/UnsupportedOperationException]
    // catchNestedExceptions-NEXT: ExceptionInfo (24 -> 28: 19):
    // catchNestedExceptions-NEXT: Class [java/lang/UnsupportedOperationException]
    public void catchNestedExceptions()
    {
        try
        {
            try
            {
                System.currentTimeMillis();

                try
                {
                    System.getProperties();
                }
                catch (IllegalStateException e)
                {
                    e.printStackTrace();
                }
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
        }
        catch (UnsupportedOperationException e)
        {
            e.printStackTrace();
        }
    }

    // RUN: FileCheck %s --check-prefix=tryWithFinally --input-file=%t/dump-processed.txt
    // tryWithFinally:      Method: tryWithFinally()V
    // tryWithFinally:      Code attribute exceptions (count = 1):
    // tryWithFinally-NEXT: ExceptionInfo (0 -> 4: 13):
    public void tryWithFinally()
    {
        try
        {
            System.currentTimeMillis();
        }
        finally
        {
            System.out.println("finally2");
        }
    }

    // RUN: FileCheck %s --check-prefix=catchExceptionWithFinally --input-file=%t/dump-processed.txt
    // catchExceptionWithFinally:      Method: catchExceptionWithFinally()V
    // catchExceptionWithFinally:      Code attribute exceptions (count = 3):
    // catchExceptionWithFinally-NEXT: ExceptionInfo (0 -> 4: 19):
    // catchExceptionWithFinally-NEXT: Class [java/lang/RuntimeException]
    // catchExceptionWithFinally-NEXT: ExceptionInfo (0 -> 4: 15):
    // catchExceptionWithFinally-NEXT: ExceptionInfo (20 -> 24: 15):
    public void catchExceptionWithFinally()
    {
        try
        {
             System.currentTimeMillis();
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
        }
        finally
        {
            System.out.println("finally2");
        }
    }

    // RUN: FileCheck %s --check-prefix=trySynchronizedWithFinally --input-file=%t/dump-processed.txt
    // trySynchronizedWithFinally:      Method: trySynchronizedWithFinally()V
    // trySynchronizedWithFinally:      Code attribute exceptions (count = 5):
    // trySynchronizedWithFinally-NEXT: ExceptionInfo (0 -> 2: 22):
    // trySynchronizedWithFinally-NEXT: ExceptionInfo (2 -> 6: 17):
    // trySynchronizedWithFinally-NEXT: ExceptionInfo (6 -> 8: 17):
    // trySynchronizedWithFinally-NEXT: ExceptionInfo (18 -> 20: 17):
    // trySynchronizedWithFinally-NEXT: ExceptionInfo (20 -> 22: 22):
    public void trySynchronizedWithFinally()
    {
        try
        {
            synchronized (this)
            {
                System.currentTimeMillis();
            }
        }
        finally
        {
            System.out.println("finally2");
        }
    }

    // RUN: FileCheck %s --check-prefix=synchronizedTryWithFinally --input-file=%t/dump-processed.txt
    // synchronizedTryWithFinally:      Method: synchronizedTryWithFinally()V
    // synchronizedTryWithFinally:      Code attribute exceptions (count = 6):
    // synchronizedTryWithFinally-NEXT: ExceptionInfo (2 -> 6: 17):
    // synchronizedTryWithFinally-NEXT: ExceptionInfo (6 -> 14: 28):
    // synchronizedTryWithFinally-NEXT: ExceptionInfo (14 -> 16: 28):
    // synchronizedTryWithFinally-NEXT: ExceptionInfo (18 -> 26: 28):
    // synchronizedTryWithFinally-NEXT: ExceptionInfo (26 -> 28: 28):
    // synchronizedTryWithFinally-NEXT: ExceptionInfo (29 -> 31: 28):
    public void synchronizedTryWithFinally()
    {
        synchronized (this)
        {
            try
            {
                System.currentTimeMillis();
            }
            finally
            {
                System.out.println("finally2");
            }
        }
    }

    // RUN: FileCheck %s --check-prefix=catchSynchronizedExceptionWithFinally --input-file=%t/dump-processed.txt
    // catchSynchronizedExceptionWithFinally:      Method: catchSynchronizedExceptionWithFinally()V
    // catchSynchronizedExceptionWithFinally:      Code attribute exceptions (count = 8):
    // catchSynchronizedExceptionWithFinally-NEXT: ExceptionInfo (0 -> 2: 20):
    // catchSynchronizedExceptionWithFinally-NEXT: Class [java/lang/RuntimeException]
    // catchSynchronizedExceptionWithFinally-NEXT: ExceptionInfo (0 -> 2: 16):
    // catchSynchronizedExceptionWithFinally-NEXT: ExceptionInfo (2 -> 6: 11):
    // catchSynchronizedExceptionWithFinally-NEXT: ExceptionInfo (6 -> 8: 11):
    // catchSynchronizedExceptionWithFinally-NEXT: ExceptionInfo (12 -> 14: 11):
    // catchSynchronizedExceptionWithFinally-NEXT: ExceptionInfo (14 -> 16: 20):
    // catchSynchronizedExceptionWithFinally-NEXT: Class [java/lang/RuntimeException]
    // catchSynchronizedExceptionWithFinally-NEXT: ExceptionInfo (14 -> 16: 16):
    // catchSynchronizedExceptionWithFinally-NEXT: ExceptionInfo (21 -> 25: 16):
    public void catchSynchronizedExceptionWithFinally()
    {
        try
        {
            synchronized (this)
            {
                System.currentTimeMillis();
            }
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
        }
        finally
        {
            System.out.println("finally2");
        }
    }

    // RUN: FileCheck %s --check-prefix=synchronizedCatchExceptionWithFinally --input-file=%t/dump-processed.txt
    // synchronizedCatchExceptionWithFinally:      Method:  synchronizedCatchExceptionWithFinally()V
    // synchronizedCatchExceptionWithFinally:      Code attribute exceptions (count = 10):
    // synchronizedCatchExceptionWithFinally-NEXT: ExceptionInfo (2 -> 6: 23):
    // synchronizedCatchExceptionWithFinally-NEXT: Class [java/lang/RuntimeException]
    // synchronizedCatchExceptionWithFinally-NEXT: ExceptionInfo (2 -> 6: 19):
    // synchronizedCatchExceptionWithFinally-NEXT: ExceptionInfo (6 -> 10: 48):
    // synchronizedCatchExceptionWithFinally-NEXT: ExceptionInfo (10 -> 16: 48):
    // synchronizedCatchExceptionWithFinally-NEXT: ExceptionInfo (24 -> 28: 19):
    // synchronizedCatchExceptionWithFinally-NEXT: ExceptionInfo (28 -> 32: 48):
    // synchronizedCatchExceptionWithFinally-NEXT: ExceptionInfo (35 -> 37: 48):
    // synchronizedCatchExceptionWithFinally-NEXT: ExceptionInfo (38 -> 46: 48):
    // synchronizedCatchExceptionWithFinally-NEXT: ExceptionInfo (46 -> 48: 48):
    // synchronizedCatchExceptionWithFinally-NEXT: ExceptionInfo (49 -> 51: 48):
    public void synchronizedCatchExceptionWithFinally()
    {
        synchronized (this)
        {
            try
            {
                System.currentTimeMillis();
            }
            catch (RuntimeException e)
            {
                e.printStackTrace();
            }
            finally
            {
                System.out.println("finally2");
            }
        }
    }

    // RUN: FileCheck %s --check-prefix=catchSynchronizedNestedExceptions --input-file=%t/dump-processed.txt
    // catchSynchronizedNestedExceptions:      Method: catchSynchronizedNestedExceptions()V
    // catchSynchronizedNestedExceptions:      Code attribute exceptions (count = 14):
    // catchSynchronizedNestedExceptions-NEXT: ExceptionInfo (0 -> 4: 32):
    // catchSynchronizedNestedExceptions-NEXT: Class [java/lang/IllegalArgumentException]
    // catchSynchronizedNestedExceptions-NEXT: ExceptionInfo (0 -> 4: 28):
    // catchSynchronizedNestedExceptions-NEXT: Class [java/lang/UnsupportedOperationException]
    // catchSynchronizedNestedExceptions-NEXT: ExceptionInfo (4 -> 6: 20):
    // catchSynchronizedNestedExceptions-NEXT: Class [java/lang/IllegalStateException]
    // catchSynchronizedNestedExceptions-NEXT: ExceptionInfo (4 -> 6: 32):
    // catchSynchronizedNestedExceptions-NEXT: Class [java/lang/IllegalArgumentException]
    // catchSynchronizedNestedExceptions-NEXT: ExceptionInfo (4 -> 6: 28):
    // catchSynchronizedNestedExceptions-NEXT: Class [java/lang/UnsupportedOperationException]
    // catchSynchronizedNestedExceptions-NEXT: ExceptionInfo (6 -> 10: 15):
    // catchSynchronizedNestedExceptions-NEXT: ExceptionInfo (10 -> 12: 15):
    // catchSynchronizedNestedExceptions-NEXT: ExceptionInfo (16 -> 18: 15):
    // catchSynchronizedNestedExceptions-NEXT: ExceptionInfo (18 -> 20: 20):
    // catchSynchronizedNestedExceptions-NEXT: Class [java/lang/IllegalStateException]
    // catchSynchronizedNestedExceptions-NEXT: ExceptionInfo (18 -> 20: 32):
    // catchSynchronizedNestedExceptions-NEXT: Class [java/lang/IllegalArgumentException]
    // catchSynchronizedNestedExceptions-NEXT: ExceptionInfo (18 -> 20: 28):
    // catchSynchronizedNestedExceptions-NEXT: Class [java/lang/UnsupportedOperationException]
    // catchSynchronizedNestedExceptions-NEXT: ExceptionInfo (21 -> 25: 32):
    // catchSynchronizedNestedExceptions-NEXT: Class [java/lang/IllegalArgumentException]
    // catchSynchronizedNestedExceptions-NEXT: ExceptionInfo (21 -> 25: 28):
    // catchSynchronizedNestedExceptions-NEXT: Class [java/lang/UnsupportedOperationException]
    // catchSynchronizedNestedExceptions-NEXT: ExceptionInfo (33 -> 37: 28):
    // catchSynchronizedNestedExceptions-NEXT: Class [java/lang/UnsupportedOperationException]
    public void catchSynchronizedNestedExceptions()
    {
        try
        {
            try
            {
                System.currentTimeMillis();

                try
                {
                    synchronized (this)
                    {
                        System.currentTimeMillis();
                    }
                }
                catch (IllegalStateException e)
                {
                    e.printStackTrace();
                }
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
        }
        catch (UnsupportedOperationException e)
        {
            e.printStackTrace();
        }
    }

    // RUN: FileCheck %s --check-prefix=synchronizedCatchNestedExceptions --input-file=%t/dump-processed.txt
    // synchronizedCatchNestedExceptions:      Method: synchronizedCatchNestedExceptions()V
    // synchronizedCatchNestedExceptions:      Code attribute exceptions (count = 15):
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (2 -> 6: 29):
    // synchronizedCatchNestedExceptions-NEXT: Class [java/lang/IllegalArgumentException]
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (2 -> 6: 25):
    // synchronizedCatchNestedExceptions-NEXT: Class [java/lang/UnsupportedOperationException]
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (2 -> 6: 21):
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (6 -> 10: 13):
    // synchronizedCatchNestedExceptions-NEXT: Class [java/lang/IllegalStateException]
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (6 -> 10: 29):
    // synchronizedCatchNestedExceptions-NEXT: Class [java/lang/IllegalArgumentException]
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (6 -> 10: 25):
    // synchronizedCatchNestedExceptions-NEXT: Class [java/lang/UnsupportedOperationException]
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (6 -> 10: 21):
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (14 -> 18: 29):
    // synchronizedCatchNestedExceptions-NEXT: Class [java/lang/IllegalArgumentException]
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (14 -> 18: 25):
    // synchronizedCatchNestedExceptions-NEXT: Class [java/lang/UnsupportedOperationException]
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (14 -> 18: 21):
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (30 -> 34: 25):
    // synchronizedCatchNestedExceptions-NEXT: Class [java/lang/UnsupportedOperationException]
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (30 -> 34: 21):
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (37 -> 41: 21):
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (41 -> 43: 21):
    // synchronizedCatchNestedExceptions-NEXT: ExceptionInfo (44 -> 46: 21):
    public void synchronizedCatchNestedExceptions()
    {
        synchronized (this)
        {
            try
            {
                try
                {
                    System.currentTimeMillis();

                    try
                    {
                        System.currentTimeMillis();
                    }
                    catch (IllegalStateException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
            }
            catch (UnsupportedOperationException e)
            {
                e.printStackTrace();
            }
        }
    }
}
