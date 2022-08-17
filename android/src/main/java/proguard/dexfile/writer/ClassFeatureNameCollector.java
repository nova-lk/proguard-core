/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2020 Guardsquare NV
 */
package proguard.dexfile.writer;

import proguard.classfile.Clazz;
import proguard.classfile.visitor.ClassVisitor;

import java.util.Collection;

/**
 * This <code>ClassVisitor</code> collects the feature names of the classes
 * that it visits in the given collection.
 *
 * @author Eric Lafortune
 */
public class ClassFeatureNameCollector
implements ClassVisitor
{
    private final Collection<String> collection;


    /**
     * Creates a new ClassNameCollector.
     * @param collection the Collection in which all feature names will be
     *                   collected.
     */
    public ClassFeatureNameCollector(Collection<String> collection)
    {
        this.collection = collection;
    }


    // Implementations for ClassVisitor.

    public void visitAnyClass(Clazz clazz)
    {
        collection.addAll(clazz.getExtraFeatureNames());
    }
}
