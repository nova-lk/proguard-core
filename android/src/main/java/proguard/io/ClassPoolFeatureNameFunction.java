/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2020 Guardsquare NV
 */
package proguard.io;

import proguard.classfile.*;
import proguard.util.StringFunction;

import java.util.Set;

/**
 * This StringFunction transforms class names to feature names, based on a
 * given class pool.
 *
 * @author Eric Lafortune
 */
public class ClassPoolFeatureNameFunction implements StringFunction
{
    private final ClassPool classPool;
    private final String    defaultFeatureName;


    /**
     * Creates a new ClassPoolNameFunction based on the given class pool.
     */
    public ClassPoolFeatureNameFunction(ClassPool classPool)
    {
        this(classPool, null);
    }


    /**
     * Creates a new ClassPoolNameFunction based on the given class pool,
     * with a default string for classes that are not in the class pool.
     */
    public ClassPoolFeatureNameFunction(ClassPool classPool,
                                        String    defaultFeatureName)
    {
        this.classPool          = classPool;
        this.defaultFeatureName = defaultFeatureName;
    }


    // Implementations for StringFunction.

    public String transform(String string)
    {
        Clazz clazz = classPool.getClass(string);
        if (clazz == null)
        {
            return defaultFeatureName;
        }

        Set<String> features    = clazz.getExtraFeatureNames();
        // If there is only one feature then return that feature.
        if (features.size() == 1)
        {
            return features.iterator().next();
        }

        // TODO(T6954): If there are no features or more than one, conservatively return the base feature.
        return defaultFeatureName;
    }
}
