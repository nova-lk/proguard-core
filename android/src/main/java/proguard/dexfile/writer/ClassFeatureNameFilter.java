/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2020 Guardsquare NV
 */
package proguard.dexfile.writer;

import proguard.classfile.*;
import proguard.classfile.visitor.ClassVisitor;
import proguard.util.*;

import java.util.*;

/**
 * This ClassVisitor delegates its visits to one of two given ClassVisitor
 * instances, depending on whether visited class has a feature name that
 * matches a given regular expression.
 *
 * @author Eric Lafortune
 */
public class ClassFeatureNameFilter implements ClassVisitor
{
    private final StringMatcher regularExpressionMatcher;
    private final String        defaultFeatureName;
    private final ClassVisitor  acceptedClassVisitor;
    private final ClassVisitor  rejectedClassVisitor;


    /**
     * Creates a new ClassFeatureNameFilter.
     * @param regularExpression    the regular expression against which feature
     *                             names will be matched.
     * @param defaultFeatureName   the default feature name, for visited
     *                             classes that don't have one.
     * @param acceptedClassVisitor the <code>ClassVisitor</code> to which
     *                             accepted visits will be delegated.
     */
    public ClassFeatureNameFilter(String       regularExpression,
                                  String       defaultFeatureName,
                                  ClassVisitor acceptedClassVisitor)
    {
        this(regularExpression,
             defaultFeatureName,
             acceptedClassVisitor,
             null);
    }


    /**
     * Creates a new ClassFeatureNameFilter.
     * @param regularExpression    the regular expression against which feature
     *                             names will be matched.
     * @param defaultFeatureName   the default feature name, for visited
     *                             classes that don't have one.
     * @param acceptedClassVisitor the <code>ClassVisitor</code> to which
     *                             accepted visits will be delegated.
     */
    public ClassFeatureNameFilter(List         regularExpression,
                                  String       defaultFeatureName,
                                  ClassVisitor acceptedClassVisitor)
    {
        this(regularExpression,
             defaultFeatureName,
             acceptedClassVisitor,
             null);
    }


    /**
     * Creates a new ClassFeatureNameFilter.
     * @param regularExpressionMatcher the string matcher against which
     *                                 feature names will be matched.
     * @param defaultFeatureName       the default feature name, for visited
     *                                 classes that don't have one.
     * @param acceptedClassVisitor     the <code>ClassVisitor</code> to which
     *                                 accepted visits will be delegated.
     */
    public ClassFeatureNameFilter(StringMatcher regularExpressionMatcher,
                                  String        defaultFeatureName,
                                  ClassVisitor  acceptedClassVisitor)
    {
        this(regularExpressionMatcher,
             defaultFeatureName,
             acceptedClassVisitor,
             null);
    }


    /**
     * Creates a new ClassFeatureNameFilter.
     * @param regularExpression    the regular expression against which feature
     *                             names will be matched.
     * @param defaultFeatureName   the default feature name, for visited
     *                             classes that don't have one.
     * @param acceptedClassVisitor the <code>ClassVisitor</code> to which
     *                             accepted visits will be delegated.
     * @param rejectedClassVisitor the <code>ClassVisitor</code> to which
     *                             rejected visits will be delegated.
     */
    public ClassFeatureNameFilter(String       regularExpression,
                                  String       defaultFeatureName,
                                  ClassVisitor acceptedClassVisitor,
                                  ClassVisitor rejectedClassVisitor)
    {
        this(new ListParser(new NameParser()).parse(regularExpression),
             defaultFeatureName,
             acceptedClassVisitor, rejectedClassVisitor);
    }


    /**
     * Creates a new ClassFeatureNameFilter.
     * @param regularExpression    the regular expression against which feature
     *                             names will be matched.
     * @param defaultFeatureName   the default feature name, for visited
     *                             classes that don't have one.
     * @param acceptedClassVisitor the <code>ClassVisitor</code> to which
     *                             accepted visits will be delegated.
     * @param rejectedClassVisitor the <code>ClassVisitor</code> to which
     *                             rejected visits will be delegated.
     */
    public ClassFeatureNameFilter(List         regularExpression,
                                  String       defaultFeatureName,
                                  ClassVisitor acceptedClassVisitor,
                                  ClassVisitor rejectedClassVisitor)
    {
        this(new ListParser(new NameParser()).parse(regularExpression),
             defaultFeatureName,
             acceptedClassVisitor, rejectedClassVisitor);
    }


    /**
     * Creates a new ClassFeatureNameFilter.
     * @param regularExpressionMatcher the string matcher against which
     *                                 feature names will be matched.
     * @param defaultFeatureName       the default feature name, for visited
     *                                 classes that don't have one.
     * @param acceptedClassVisitor     the <code>ClassVisitor</code> to which
     *                                 accepted visits will be delegated.
     * @param rejectedClassVisitor     the <code>ClassVisitor</code> to which
     *                                 rejected visits will be delegated.
     */
    public ClassFeatureNameFilter(StringMatcher regularExpressionMatcher,
                                  String        defaultFeatureName,
                                  ClassVisitor  acceptedClassVisitor,
                                  ClassVisitor  rejectedClassVisitor)
    {
        this.regularExpressionMatcher = regularExpressionMatcher;
        this.defaultFeatureName       = defaultFeatureName;
        this.acceptedClassVisitor     = acceptedClassVisitor;
        this.rejectedClassVisitor     = rejectedClassVisitor;
    }


    // Implementations for ClassVisitor.

    @Override
    public void visitAnyClass(Clazz clazz)
    {
        ClassVisitor delegate = getDelegateVisitor(clazz);
        if (delegate != null)
        {
            clazz.accept(delegate);
        }
    }


    // Small utility methods.

    private ClassVisitor getDelegateVisitor(Clazz clazz)
    {
        Set<String> featureNames = clazz.getExtraFeatureNames();

        if (featureNames.isEmpty())
        {
            return regularExpressionMatcher.matches(defaultFeatureName) ?
                acceptedClassVisitor :
                rejectedClassVisitor;
        }

        for (String featureName : clazz.getExtraFeatureNames())
        {
            if (regularExpressionMatcher.matches(featureName))
            {
                return acceptedClassVisitor;
            }
        }
        return rejectedClassVisitor;
    }
}
