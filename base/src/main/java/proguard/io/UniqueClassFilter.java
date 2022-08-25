/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2020 Guardsquare NV
 */
package proguard.io;

import proguard.classfile.Clazz;
import proguard.classfile.visitor.ClassVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * This <code>ClassVisitor</code> delegates its visits to another given
 * <code>ClassVisitor</code>, but at most a single time.
 *
 * @author Eric Lafortune
 */
public class UniqueClassFilter
implements ClassVisitor
{
    private final ClassVisitor classVisitor;

    private final Set<String> classNames = new HashSet<>();


    /**
     * Creates a new UniqueClassFilter.
     * @param classVisitor the <code>ClassVisitor</code> to which visits
     *                     will be delegated.
     */
    public UniqueClassFilter(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }


    // Implementations for ClassVisitor.

    @Override
    public void visitAnyClass(Clazz clazz)
    {
        // Only visit the class if it hasn't been visited before.
        if (classNames.add(clazz.getName()))
        {
            clazz.accept(classVisitor);
        }
    }
}
