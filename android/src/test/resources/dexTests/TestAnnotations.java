// This test checks whether DexGuard correctly converts all types of
// annotations from dex format to class format.
//
// Compile the java source code to a dex file.
// RUN: %javac -d %t %s
//
// RUN: %jar -cf %t/classes-java.jar \
// RUN:      -C %t TestAnnotations.class \
// RUN:      -C %t MainAnnotation.class \
// RUN:      -C %t SimpleAnnotation.class \
// RUN:      -C %t SimpleEnum.class
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

// Convert the dex file and dump the processed class files with DexGuard.
// RUN: %dexguard -dalvik \
// RUN:           -dontoptimize \
// RUN:           -dontshrink \
// RUN:           -dontobfuscate \
// RUN:           -injars %t/classes-dalvik.jar \
// RUN:           -libraryjars %android \
// RUN:           -verbose \
// RUN:           -dump %t/dump-processed.txt

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// RUN: cat %t/dump-processed.txt | FileCheck %s --check-prefix CLASS
// CLASS:       Program class: TestAnnotations
// CLASS:       Class file attributes (count = 2):
// CLASS-NEXT:   - Source file attribute:
// CLASS-NEXT:    - Utf8 [TestAnnotations.java]
// CLASS-NEXT:   - Runtime visible annotations attribute:
// CLASS-NEXT:     - Annotation [LMainAnnotation;]:
// CLASS-NEXT:      - Array element value [annotationArrayValue]:
// CLASS-NEXT:        - Annotation element value [(default)]:
// CLASS-NEXT:          - Annotation [LSimpleAnnotation;]:
// CLASS-NEXT:            - Constant element value [value 's']
// CLASS-NEXT:              - Utf8 [annotation1a]
// CLASS-NEXT:        - Annotation element value [(default)]:
// CLASS-NEXT:          - Annotation [LSimpleAnnotation;]:
// CLASS-NEXT:            - Constant element value [value 's']
// CLASS-NEXT:              - Utf8 [annotation1b]
// CLASS-NEXT:      - Annotation element value [annotationValue]:
// CLASS-NEXT:        - Annotation [LSimpleAnnotation;]:
// CLASS-NEXT:          - Constant element value [value 's']
// CLASS-NEXT:            - Utf8 [annotation1]
// CLASS-NEXT:      - Array element value [booleanArrayValue]:
// CLASS-NEXT:        - Constant element value [(default) 'Z']
// CLASS-NEXT:          - Integer [1]
// CLASS-NEXT:        - Constant element value [(default) 'Z']
// CLASS-NEXT:          - Integer [0]
// CLASS-NEXT:      - Constant element value [booleanValue 'Z']
// CLASS-NEXT:        - Integer [1]
// CLASS-NEXT:      - Array element value [byteArrayValue]:
// CLASS-NEXT:        - Constant element value [(default) 'B']
// CLASS-NEXT:          - Integer [1]
// CLASS-NEXT:        - Constant element value [(default) 'B']
// CLASS-NEXT:          - Integer [-1]
// CLASS-NEXT:      - Constant element value [byteValue 'B']
// CLASS-NEXT:        - Integer [1]
// CLASS-NEXT:      - Array element value [charArrayValue]:
// CLASS-NEXT:        - Constant element value [(default) 'C']
// CLASS-NEXT:          - Integer [99]
// CLASS-NEXT:        - Constant element value [(default) 'C']
// CLASS-NEXT:          - Integer [98]
// CLASS-NEXT:        - Constant element value [(default) 'C']
// CLASS-NEXT:          - Integer [65535]
// CLASS-NEXT:      - Constant element value [charValue 'C']
// CLASS-NEXT:        - Integer [65]
// CLASS-NEXT:      - Array element value [classArrayValue]:
// CLASS-NEXT:        - Class element value [(default), Ljava/lang/Integer;]
// CLASS-NEXT:        - Class element value [(default), Ljava/lang/Long;]
// CLASS-NEXT:      - Class element value [classValue, Ljava/lang/Integer;]
// CLASS-NEXT:      - Array element value [doubleArrayValue]:
// CLASS-NEXT:        - Constant element value [(default) 'D']
// CLASS-NEXT:          - Double [1.0]
// CLASS-NEXT:        - Constant element value [(default) 'D']
// CLASS-NEXT:          - Double [-1.0]
// CLASS-NEXT:      - Constant element value [doubleValue 'D']
// CLASS-NEXT:        - Double [1.0]
// CLASS-NEXT:      - Array element value [enumArrayValue]:
// CLASS-NEXT:        - Enum constant element value [(default), LSimpleEnum;, ONE]
// CLASS-NEXT:        - Enum constant element value [(default), LSimpleEnum;, TWO]
// CLASS-NEXT:      - Enum constant element value [enumValue, LSimpleEnum;, ONE]
// CLASS-NEXT:      - Array element value [floatArrayValue]:
// CLASS-NEXT:        - Constant element value [(default) 'F']
// CLASS-NEXT:          - Float [1.0]
// CLASS-NEXT:        - Constant element value [(default) 'F']
// CLASS-NEXT:          - Float [-1.0]
// CLASS-NEXT:      - Constant element value [floatValue 'F']
// CLASS-NEXT:        - Float [1.0]
// CLASS-NEXT:      - Array element value [intArrayValue]:
// CLASS-NEXT:        - Constant element value [(default) 'I']
// CLASS-NEXT:          - Integer [1]
// CLASS-NEXT:        - Constant element value [(default) 'I']
// CLASS-NEXT:          - Integer [-1]
// CLASS-NEXT:      - Constant element value [intValue 'I']
// CLASS-NEXT:        - Integer [1]
// CLASS-NEXT:      - Array element value [longArrayValue]:
// CLASS-NEXT:        - Constant element value [(default) 'J']
// CLASS-NEXT:          - Long [1]
// CLASS-NEXT:        - Constant element value [(default) 'J']
// CLASS-NEXT:          - Long [-1]
// CLASS-NEXT:      - Constant element value [longValue 'J']
// CLASS-NEXT:        - Long [1]
// CLASS-NEXT:      - Array element value [shortArrayValue]:
// CLASS-NEXT:        - Constant element value [(default) 'S']
// CLASS-NEXT:          - Integer [1]
// CLASS-NEXT:        - Constant element value [(default) 'S']
// CLASS-NEXT:          - Integer [-1]
// CLASS-NEXT:      - Constant element value [shortValue 'S']
// CLASS-NEXT:        - Integer [1]
// CLASS-NEXT:      - Array element value [stringArrayValue]:
// CLASS-NEXT:        - Constant element value [(default) 's']
// CLASS-NEXT:          - Utf8 [string1a]
// CLASS-NEXT:        - Constant element value [(default) 's']
// CLASS-NEXT:          - Utf8 [string1b]
// CLASS-NEXT:      - Constant element value [stringValue 's']
// CLASS-NEXT:        - Utf8 [string1]
@MainAnnotation
(
  booleanValue = true,
  byteValue    = 1,
  shortValue   = 1,
  charValue    = 'A',
  intValue     = 1,
  longValue    = 1L,
  floatValue   = 1.0f,
  doubleValue  = 1.0d,
  stringValue  = "string1",

  annotationValue = @SimpleAnnotation("annotation1"),
  enumValue       = SimpleEnum.ONE,
  classValue      = Integer.class,

  booleanArrayValue = {true, false },
  byteArrayValue    = { 1, -1 },
  shortArrayValue   = { (short)1, (short)-1 },
  charArrayValue    = { 'c', 'b', (char)-1 },
  intArrayValue     = { 1, -1 },
  longArrayValue    = { 1L, -1L },
  floatArrayValue   = { 1.0f, -1.0f },
  doubleArrayValue  = { 1.0d, -1.0d },
  stringArrayValue  = { "string1a", "string1b" },

  annotationArrayValue = { @SimpleAnnotation("annotation1a"), @SimpleAnnotation("annotation1b") },
  enumArrayValue       = { SimpleEnum.ONE, SimpleEnum.TWO },
  classArrayValue      = { Integer.class, Long.class }
)
public class TestAnnotations
{
    // RUN: cat %t/dump-processed.txt | FileCheck %s --check-prefix FIELD
    // FIELD:           - Annotation [LMainAnnotation;]:
    // FIELD-NEXT:        - Array element value [annotationArrayValue]:
    // FIELD-NEXT:          - Annotation element value [(default)]:
    // FIELD-NEXT:            - Annotation [LSimpleAnnotation;]:
    // FIELD-NEXT:              - Constant element value [value 's']
    // FIELD-NEXT:                - Utf8 [annotation2a]
    // FIELD-NEXT:          - Annotation element value [(default)]:
    // FIELD-NEXT:            - Annotation [LSimpleAnnotation;]:
    // FIELD-NEXT:              - Constant element value [value 's']
    // FIELD-NEXT:                - Utf8 [annotation2b]
    // FIELD-NEXT:        - Annotation element value [annotationValue]:
    // FIELD-NEXT:          - Annotation [LSimpleAnnotation;]:
    // FIELD-NEXT:            - Constant element value [value 's']
    // FIELD-NEXT:              - Utf8 [annotation2]
    // FIELD-NEXT:        - Array element value [booleanArrayValue]:
    // FIELD-NEXT:          - Constant element value [(default) 'Z']
    // FIELD-NEXT:            - Integer [1]
    // FIELD-NEXT:          - Constant element value [(default) 'Z']
    // FIELD-NEXT:            - Integer [0]
    // FIELD-NEXT:        - Constant element value [booleanValue 'Z']
    // FIELD-NEXT:          - Integer [1]
    // FIELD-NEXT:        - Array element value [byteArrayValue]:
    // FIELD-NEXT:          - Constant element value [(default) 'B']
    // FIELD-NEXT:            - Integer [2]
    // FIELD-NEXT:          - Constant element value [(default) 'B']
    // FIELD-NEXT:            - Integer [-2]
    // FIELD-NEXT:        - Constant element value [byteValue 'B']
    // FIELD-NEXT:          - Integer [2]
    // FIELD-NEXT:        - Array element value [charArrayValue]:
    // FIELD-NEXT:          - Constant element value [(default) 'C']
    // FIELD-NEXT:            - Integer [99]
    // FIELD-NEXT:          - Constant element value [(default) 'C']
    // FIELD-NEXT:            - Integer [98]
    // FIELD-NEXT:          - Constant element value [(default) 'C']
    // FIELD-NEXT:            - Integer [65534]
    // FIELD-NEXT:        - Constant element value [charValue 'C']
    // FIELD-NEXT:          - Integer [65]
    // FIELD-NEXT:        - Array element value [classArrayValue]:
    // FIELD-NEXT:          - Class element value [(default), Ljava/lang/Integer;]
    // FIELD-NEXT:          - Class element value [(default), Ljava/lang/Long;]
    // FIELD-NEXT:        - Class element value [classValue, Ljava/lang/Integer;]
    // FIELD-NEXT:        - Array element value [doubleArrayValue]:
    // FIELD-NEXT:          - Constant element value [(default) 'D']
    // FIELD-NEXT:            - Double [2.0]
    // FIELD-NEXT:          - Constant element value [(default) 'D']
    // FIELD-NEXT:            - Double [-2.0]
    // FIELD-NEXT:        - Constant element value [doubleValue 'D']
    // FIELD-NEXT:          - Double [2.0]
    // FIELD-NEXT:        - Array element value [enumArrayValue]:
    // FIELD-NEXT:          - Enum constant element value [(default), LSimpleEnum;, ONE]
    // FIELD-NEXT:          - Enum constant element value [(default), LSimpleEnum;, TWO]
    // FIELD-NEXT:        - Enum constant element value [enumValue, LSimpleEnum;, ONE]
    // FIELD-NEXT:        - Array element value [floatArrayValue]:
    // FIELD-NEXT:          - Constant element value [(default) 'F']
    // FIELD-NEXT:            - Float [2.0]
    // FIELD-NEXT:          - Constant element value [(default) 'F']
    // FIELD-NEXT:            - Float [-2.0]
    // FIELD-NEXT:        - Constant element value [floatValue 'F']
    // FIELD-NEXT:          - Float [2.0]
    // FIELD-NEXT:        - Array element value [intArrayValue]:
    // FIELD-NEXT:          - Constant element value [(default) 'I']
    // FIELD-NEXT:            - Integer [2]
    // FIELD-NEXT:          - Constant element value [(default) 'I']
    // FIELD-NEXT:            - Integer [-2]
    // FIELD-NEXT:        - Constant element value [intValue 'I']
    // FIELD-NEXT:          - Integer [2]
    // FIELD-NEXT:        - Array element value [longArrayValue]:
    // FIELD-NEXT:          - Constant element value [(default) 'J']
    // FIELD-NEXT:            - Long [2]
    // FIELD-NEXT:          - Constant element value [(default) 'J']
    // FIELD-NEXT:            - Long [-2]
    // FIELD-NEXT:        - Constant element value [longValue 'J']
    // FIELD-NEXT:          - Long [2]
    // FIELD-NEXT:        - Array element value [shortArrayValue]:
    // FIELD-NEXT:          - Constant element value [(default) 'S']
    // FIELD-NEXT:            - Integer [2]
    // FIELD-NEXT:          - Constant element value [(default) 'S']
    // FIELD-NEXT:            - Integer [-2]
    // FIELD-NEXT:        - Constant element value [shortValue 'S']
    // FIELD-NEXT:          - Integer [2]
    // FIELD-NEXT:        - Array element value [stringArrayValue]:
    // FIELD-NEXT:          - Constant element value [(default) 's']
    // FIELD-NEXT:            - Utf8 [string2a]
    // FIELD-NEXT:          - Constant element value [(default) 's']
    // FIELD-NEXT:            - Utf8 [string2b]
    // FIELD-NEXT:        - Constant element value [stringValue 's']
    // FIELD-NEXT:          - Utf8 [string2]
    @MainAnnotation
    (
      booleanValue = true,
      byteValue    = 2,
      shortValue   = 2,
      charValue    = 'A',
      intValue     = 2,
      longValue    = 2L,
      floatValue   = 2.0f,
      doubleValue  = 2.0d,
      stringValue  = "string2",

      annotationValue = @SimpleAnnotation("annotation2"),
      enumValue       = SimpleEnum.ONE,
      classValue      = Integer.class,

      booleanArrayValue = {true, false },
      byteArrayValue    = { 2, -2 },
      shortArrayValue   = { (short)2, (short)-2 },
      charArrayValue    = { 'c', 'b', (char)-2 },
      intArrayValue     = { 2, -2 },
      longArrayValue    = { 2L, -2L },
      floatArrayValue   = { 2.0f, -2.0f },
      doubleArrayValue  = { 2.0d, -2.0d },
      stringArrayValue  = { "string2a", "string2b" },

      annotationArrayValue = { @SimpleAnnotation("annotation2a"), @SimpleAnnotation("annotation2b") },
      enumArrayValue       = { SimpleEnum.ONE, SimpleEnum.TWO },
      classArrayValue      = { Integer.class, Long.class }
    )
    private int someField;

    // RUN: cat %t/dump-processed.txt | FileCheck %s --check-prefix METHOD
    // METHOD:           Method:       someMethod(I)V
    // METHOD:              Access flags: 0x2
    // METHOD:                = private void someMethod(int)
    // METHOD:           - Annotation [LMainAnnotation;]:
    // METHOD-NEXT:        - Array element value [annotationArrayValue]:
    // METHOD-NEXT:          - Annotation element value [(default)]:
    // METHOD-NEXT:            - Annotation [LSimpleAnnotation;]:
    // METHOD-NEXT:              - Constant element value [value 's']
    // METHOD-NEXT:                - Utf8 [annotation3a]
    // METHOD-NEXT:          - Annotation element value [(default)]:
    // METHOD-NEXT:            - Annotation [LSimpleAnnotation;]:
    // METHOD-NEXT:              - Constant element value [value 's']
    // METHOD-NEXT:                - Utf8 [annotation3b]
    // METHOD-NEXT:        - Annotation element value [annotationValue]:
    // METHOD-NEXT:          - Annotation [LSimpleAnnotation;]:
    // METHOD-NEXT:            - Constant element value [value 's']
    // METHOD-NEXT:              - Utf8 [annotation3]
    // METHOD-NEXT:        - Array element value [booleanArrayValue]:
    // METHOD-NEXT:          - Constant element value [(default) 'Z']
    // METHOD-NEXT:            - Integer [1]
    // METHOD-NEXT:          - Constant element value [(default) 'Z']
    // METHOD-NEXT:            - Integer [0]
    // METHOD-NEXT:        - Constant element value [booleanValue 'Z']
    // METHOD-NEXT:          - Integer [1]
    // METHOD-NEXT:        - Array element value [byteArrayValue]:
    // METHOD-NEXT:          - Constant element value [(default) 'B']
    // METHOD-NEXT:            - Integer [3]
    // METHOD-NEXT:          - Constant element value [(default) 'B']
    // METHOD-NEXT:            - Integer [-3]
    // METHOD-NEXT:        - Constant element value [byteValue 'B']
    // METHOD-NEXT:          - Integer [3]
    // METHOD-NEXT:        - Array element value [charArrayValue]:
    // METHOD-NEXT:          - Constant element value [(default) 'C']
    // METHOD-NEXT:            - Integer [99]
    // METHOD-NEXT:          - Constant element value [(default) 'C']
    // METHOD-NEXT:            - Integer [98]
    // METHOD-NEXT:          - Constant element value [(default) 'C']
    // METHOD-NEXT:            - Integer [65533]
    // METHOD-NEXT:        - Constant element value [charValue 'C']
    // METHOD-NEXT:          - Integer [65]
    // METHOD-NEXT:        - Array element value [classArrayValue]:
    // METHOD-NEXT:          - Class element value [(default), Ljava/lang/Integer;]
    // METHOD-NEXT:          - Class element value [(default), Ljava/lang/Long;]
    // METHOD-NEXT:        - Class element value [classValue, Ljava/lang/Integer;]
    // METHOD-NEXT:        - Array element value [doubleArrayValue]:
    // METHOD-NEXT:          - Constant element value [(default) 'D']
    // METHOD-NEXT:            - Double [3.0]
    // METHOD-NEXT:          - Constant element value [(default) 'D']
    // METHOD-NEXT:            - Double [-3.0]
    // METHOD-NEXT:        - Constant element value [doubleValue 'D']
    // METHOD-NEXT:          - Double [3.0]
    // METHOD-NEXT:        - Array element value [enumArrayValue]:
    // METHOD-NEXT:          - Enum constant element value [(default), LSimpleEnum;, ONE]
    // METHOD-NEXT:          - Enum constant element value [(default), LSimpleEnum;, TWO]
    // METHOD-NEXT:        - Enum constant element value [enumValue, LSimpleEnum;, ONE]
    // METHOD-NEXT:        - Array element value [floatArrayValue]:
    // METHOD-NEXT:          - Constant element value [(default) 'F']
    // METHOD-NEXT:            - Float [3.0]
    // METHOD-NEXT:          - Constant element value [(default) 'F']
    // METHOD-NEXT:            - Float [-3.0]
    // METHOD-NEXT:        - Constant element value [floatValue 'F']
    // METHOD-NEXT:          - Float [3.0]
    // METHOD-NEXT:        - Array element value [intArrayValue]:
    // METHOD-NEXT:          - Constant element value [(default) 'I']
    // METHOD-NEXT:            - Integer [3]
    // METHOD-NEXT:          - Constant element value [(default) 'I']
    // METHOD-NEXT:            - Integer [-3]
    // METHOD-NEXT:        - Constant element value [intValue 'I']
    // METHOD-NEXT:          - Integer [3]
    // METHOD-NEXT:        - Array element value [longArrayValue]:
    // METHOD-NEXT:          - Constant element value [(default) 'J']
    // METHOD-NEXT:            - Long [3]
    // METHOD-NEXT:          - Constant element value [(default) 'J']
    // METHOD-NEXT:            - Long [-3]
    // METHOD-NEXT:        - Constant element value [longValue 'J']
    // METHOD-NEXT:          - Long [3]
    // METHOD-NEXT:        - Array element value [shortArrayValue]:
    // METHOD-NEXT:          - Constant element value [(default) 'S']
    // METHOD-NEXT:            - Integer [3]
    // METHOD-NEXT:          - Constant element value [(default) 'S']
    // METHOD-NEXT:            - Integer [-3]
    // METHOD-NEXT:        - Constant element value [shortValue 'S']
    // METHOD-NEXT:          - Integer [3]
    // METHOD-NEXT:        - Array element value [stringArrayValue]:
    // METHOD-NEXT:          - Constant element value [(default) 's']
    // METHOD-NEXT:            - Utf8 [string3a]
    // METHOD-NEXT:          - Constant element value [(default) 's']
    // METHOD-NEXT:            - Utf8 [string3b]
    // METHOD-NEXT:        - Constant element value [stringValue 's']
    // METHOD-NEXT:          - Utf8 [string3]
    @MainAnnotation
    (
      booleanValue = true,
      byteValue    = 3,
      shortValue   = 3,
      charValue    = 'A',
      intValue     = 3,
      longValue    = 3L,
      floatValue   = 3.0f,
      doubleValue  = 3.0d,
      stringValue  = "string3",

      annotationValue = @SimpleAnnotation("annotation3"),
      enumValue       = SimpleEnum.ONE,
      classValue      = Integer.class,

      booleanArrayValue = {true, false },
      byteArrayValue    = { 3, -3 },
      shortArrayValue   = { (short)3, (short)-3 },
      charArrayValue    = { 'c', 'b', (char)-3 },
      intArrayValue     = { 3, -3 },
      longArrayValue    = { 3L, -3L },
      floatArrayValue   = { 3.0f, -3.0f },
      doubleArrayValue  = { 3.0d, -3.0d },
      stringArrayValue  = { "string3a", "string3b" },

      annotationArrayValue = { @SimpleAnnotation("annotation3a"), @SimpleAnnotation("annotation3b") },
      enumArrayValue       = { SimpleEnum.ONE, SimpleEnum.TWO },
      classArrayValue      = { Integer.class, Long.class }
    )
    private void someMethod(
        // RUN: cat %t/dump-processed.txt | FileCheck %s --check-prefix PARAMETER
        // PARAMETER:            - Parameter #0, annotation [LMainAnnotation;]:
        // PARAMETER-NEXT:         - Array element value [annotationArrayValue]:
        // PARAMETER-NEXT:           - Annotation element value [(default)]:
        // PARAMETER-NEXT:             - Annotation [LSimpleAnnotation;]:
        // PARAMETER-NEXT:               - Constant element value [value 's']
        // PARAMETER-NEXT:                 - Utf8 [annotation4a]
        // PARAMETER-NEXT:           - Annotation element value [(default)]:
        // PARAMETER-NEXT:             - Annotation [LSimpleAnnotation;]:
        // PARAMETER-NEXT:               - Constant element value [value 's']
        // PARAMETER-NEXT:                 - Utf8 [annotation4b]
        // PARAMETER-NEXT:         - Annotation element value [annotationValue]:
        // PARAMETER-NEXT:           - Annotation [LSimpleAnnotation;]:
        // PARAMETER-NEXT:             - Constant element value [value 's']
        // PARAMETER-NEXT:               - Utf8 [annotation4]
        // PARAMETER-NEXT:         - Array element value [booleanArrayValue]:
        // PARAMETER-NEXT:           - Constant element value [(default) 'Z']
        // PARAMETER-NEXT:             - Integer [1]
        // PARAMETER-NEXT:           - Constant element value [(default) 'Z']
        // PARAMETER-NEXT:             - Integer [0]
        // PARAMETER-NEXT:         - Constant element value [booleanValue 'Z']
        // PARAMETER-NEXT:           - Integer [1]
        // PARAMETER-NEXT:         - Array element value [byteArrayValue]:
        // PARAMETER-NEXT:           - Constant element value [(default) 'B']
        // PARAMETER-NEXT:             - Integer [4]
        // PARAMETER-NEXT:           - Constant element value [(default) 'B']
        // PARAMETER-NEXT:             - Integer [-4]
        // PARAMETER-NEXT:         - Constant element value [byteValue 'B']
        // PARAMETER-NEXT:           - Integer [4]
        // PARAMETER-NEXT:         - Array element value [charArrayValue]:
        // PARAMETER-NEXT:           - Constant element value [(default) 'C']
        // PARAMETER-NEXT:             - Integer [99]
        // PARAMETER-NEXT:           - Constant element value [(default) 'C']
        // PARAMETER-NEXT:             - Integer [98]
        // PARAMETER-NEXT:           - Constant element value [(default) 'C']
        // PARAMETER-NEXT:             - Integer [65532]
        // PARAMETER-NEXT:         - Constant element value [charValue 'C']
        // PARAMETER-NEXT:           - Integer [65]
        // PARAMETER-NEXT:         - Array element value [classArrayValue]:
        // PARAMETER-NEXT:           - Class element value [(default), Ljava/lang/Integer;]
        // PARAMETER-NEXT:           - Class element value [(default), Ljava/lang/Long;]
        // PARAMETER-NEXT:         - Class element value [classValue, Ljava/lang/Integer;]
        // PARAMETER-NEXT:         - Array element value [doubleArrayValue]:
        // PARAMETER-NEXT:           - Constant element value [(default) 'D']
        // PARAMETER-NEXT:             - Double [4.0]
        // PARAMETER-NEXT:           - Constant element value [(default) 'D']
        // PARAMETER-NEXT:             - Double [-4.0]
        // PARAMETER-NEXT:         - Constant element value [doubleValue 'D']
        // PARAMETER-NEXT:           - Double [4.0]
        // PARAMETER-NEXT:         - Array element value [enumArrayValue]:
        // PARAMETER-NEXT:           - Enum constant element value [(default), LSimpleEnum;, ONE]
        // PARAMETER-NEXT:           - Enum constant element value [(default), LSimpleEnum;, TWO]
        // PARAMETER-NEXT:         - Enum constant element value [enumValue, LSimpleEnum;, ONE]
        // PARAMETER-NEXT:         - Array element value [floatArrayValue]:
        // PARAMETER-NEXT:           - Constant element value [(default) 'F']
        // PARAMETER-NEXT:             - Float [4.0]
        // PARAMETER-NEXT:           - Constant element value [(default) 'F']
        // PARAMETER-NEXT:             - Float [-4.0]
        // PARAMETER-NEXT:         - Constant element value [floatValue 'F']
        // PARAMETER-NEXT:           - Float [4.0]
        // PARAMETER-NEXT:         - Array element value [intArrayValue]:
        // PARAMETER-NEXT:           - Constant element value [(default) 'I']
        // PARAMETER-NEXT:             - Integer [4]
        // PARAMETER-NEXT:           - Constant element value [(default) 'I']
        // PARAMETER-NEXT:             - Integer [-4]
        // PARAMETER-NEXT:         - Constant element value [intValue 'I']
        // PARAMETER-NEXT:           - Integer [4]
        // PARAMETER-NEXT:         - Array element value [longArrayValue]:
        // PARAMETER-NEXT:           - Constant element value [(default) 'J']
        // PARAMETER-NEXT:             - Long [4]
        // PARAMETER-NEXT:           - Constant element value [(default) 'J']
        // PARAMETER-NEXT:             - Long [-4]
        // PARAMETER-NEXT:         - Constant element value [longValue 'J']
        // PARAMETER-NEXT:           - Long [4]
        // PARAMETER-NEXT:         - Array element value [shortArrayValue]:
        // PARAMETER-NEXT:           - Constant element value [(default) 'S']
        // PARAMETER-NEXT:             - Integer [4]
        // PARAMETER-NEXT:           - Constant element value [(default) 'S']
        // PARAMETER-NEXT:             - Integer [-4]
        // PARAMETER-NEXT:         - Constant element value [shortValue 'S']
        // PARAMETER-NEXT:           - Integer [4]
        // PARAMETER-NEXT:         - Array element value [stringArrayValue]:
        // PARAMETER-NEXT:           - Constant element value [(default) 's']
        // PARAMETER-NEXT:             - Utf8 [string4a]
        // PARAMETER-NEXT:           - Constant element value [(default) 's']
        // PARAMETER-NEXT:             - Utf8 [string4b]
        // PARAMETER-NEXT:         - Constant element value [stringValue 's']
        // PARAMETER-NEXT:           - Utf8 [string4]
      @MainAnnotation
      (
        booleanValue = true,
        byteValue    = 4,
        shortValue   = 4,
        charValue    = 'A',
        intValue     = 4,
        longValue    = 4L,
        floatValue   = 4.0f,
        doubleValue  = 4.0d,
        stringValue  = "string4",

        annotationValue = @SimpleAnnotation("annotation4"),
        enumValue       = SimpleEnum.ONE,
        classValue      = Integer.class,

        booleanArrayValue = {true, false },
        byteArrayValue    = { 4, -4 },
        shortArrayValue   = { (short)4, (short)-4 },
        charArrayValue    = { 'c', 'b', (char)-4 },
        intArrayValue     = { 4, -4 },
        longArrayValue    = { 4L, -4L },
        floatArrayValue   = { 4.0f, -4.0f },
        doubleArrayValue  = { 4.0d, -4.0d },
        stringArrayValue  = { "string4a", "string4b" },

        annotationArrayValue = { @SimpleAnnotation("annotation4a"), @SimpleAnnotation("annotation4b") },
        enumArrayValue       = { SimpleEnum.ONE, SimpleEnum.TWO },
        classArrayValue      = { Integer.class, Long.class }
      )
      int someParameter) {}
}


@Retention(RetentionPolicy.RUNTIME)
@interface MainAnnotation
{
    boolean booleanValue();
    byte    byteValue();
    short   shortValue();
    char    charValue();
    int     intValue();
    long    longValue();
    float   floatValue();
    double  doubleValue();
    String  stringValue();

    SimpleAnnotation annotationValue();
    SimpleEnum       enumValue();
    Class            classValue();

    boolean[] booleanArrayValue();
    byte[]    byteArrayValue();
    short[]   shortArrayValue();
    char[]    charArrayValue();
    int[]     intArrayValue();
    long[]    longArrayValue();
    float[]   floatArrayValue();
    double[]  doubleArrayValue();
    String[]  stringArrayValue();

    SimpleAnnotation[] annotationArrayValue();
    SimpleEnum[]       enumArrayValue();
    Class[]            classArrayValue();
}

@Retention(RetentionPolicy.RUNTIME)
@interface SimpleAnnotation {
    String value();
}

enum SimpleEnum {
    ONE, TWO, THREE;
}
