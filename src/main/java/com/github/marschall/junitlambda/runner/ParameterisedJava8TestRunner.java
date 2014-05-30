package com.github.marschall.junitlambda.runner;

import com.github.marschall.junitlambda.annotations.FirstTest;
import com.github.marschall.junitlambda.annotations.LastTest;
import com.github.marschall.junitlambda.internal.Java8TestMethod;
import junitparams.internal.ParameterisedTestClassRunner;
import junitparams.internal.TestMethod;
import org.junit.Test;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO AC: JavaDoc
 *
 * @author Alasdair Collinson
 * @since 0.2.0
 */
public abstract class ParameterisedJava8TestRunner extends ParameterisedTestClassRunner {

    private List<Class<? extends Annotation>> testAnnotations;

    public ParameterisedJava8TestRunner(TestClass testClass, List<Class<? extends Annotation>> testAnnotations) {
        super(testClass);
        this.testAnnotations = testAnnotations;
        reallyComputeTestMethods(testClass);
        fillTestMethodsMap();
        computeFrameworkMethods();
    }

    @Override
    protected void computeTestMethods(TestClass testClass) {
        testMethodsList = new ArrayList<>();
    }

    protected void reallyComputeTestMethods(TestClass testClass) {
        for (Class<? extends Annotation> annotation : testAnnotations) {
            testMethodsList.addAll(Java8TestMethod.listFrom(testClass.getAnnotatedMethods(annotation), testClass));
        }
        for(TestMethod testMethod : testMethodsList) {
            if(testMethod.frameworkMethod().getAnnotation(FirstTest.class) != null) {
                onFirstTest(testMethod);
            }
            if(testMethod.frameworkMethod().getAnnotation(LastTest.class) != null) {
                onLastTest(testMethod);
            }
        }
    }

    /**
     * TODO AC: JavaDoc
     *
     * @param testMethod
     */
    protected abstract void onFirstTest(TestMethod testMethod);

    /**
     * TODO AC: JavaDoc
     *
     * @param testMethod
     */
    protected abstract void onLastTest(TestMethod testMethod);

    private void fillTestMethodsMap() {
        for (TestMethod testMethod : testMethodsList)
            testMethods.put(testMethod.frameworkMethod(), testMethod);
    }

}
