package com.github.marschall.junitlambda.internal;

import com.github.marschall.junitlambda.annotations.ParameterRecord;
import com.github.marschall.junitlambda.annotations.ParameterRecords;
import junitparams.FileParameters;
import junitparams.Parameters;
import junitparams.internal.TestMethod;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An extension of the {@link junitparams.internal.TestMethod} class as defined in the JUnitParams library which
 * supports the {@link com.github.marschall.junitlambda.annotations.ParameterRecord} annotation.
 *
 * @author Alasdair Collinson
 */
public class Java8TestMethod extends TestMethod {

    private Parameters parametersAnnotation;
    private FileParameters fileParametersAnnotation;
    private ParameterRecords parameterRecordsAnnotation;
    private Class<?> testClass;
    private List<Object> params;

    public Java8TestMethod(FrameworkMethod method, TestClass testClass) {
        super(method, testClass);
        parametersAnnotation = method.getAnnotation(Parameters.class);
        fileParametersAnnotation = method.getAnnotation(FileParameters.class);
        parameterRecordsAnnotation = method.getAnnotation(ParameterRecords.class);
        if (parameterRecordsAnnotation == null) {
            ParameterRecord parameterRecord = method.getAnnotation(ParameterRecord.class);
            if (parameterRecord != null) {
                parameterRecordsAnnotation = new ParameterRecords() {

                    @Override
                    public ParameterRecord[] value() {
                        return new ParameterRecord[]{parameterRecord};
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return ParameterRecords.class;
                    }
                };
            }
        }
        this.testClass = testClass.getJavaClass();
    }

    public static List<TestMethod> listFrom(List<FrameworkMethod> annotatedMethods, TestClass testClass) {
        return annotatedMethods
                .stream()
                .map(frameworkMethod -> new Java8TestMethod(frameworkMethod, testClass))
                .collect(Collectors.toList());
    }

    @Override
    public Object[] parametersSets() {
        if (params != null)
            return params.toArray();

        if (parametersAnnotation != null || parameterRecordsAnnotation != null) {
            // get the parameters from the {@link Parameters#value()} element
            params = ParameterExtractor.paramsFromAnnotation(parametersAnnotation, parameterRecordsAnnotation);

            // add the parameters from source files
            params.addAll(ParameterExtractor.paramsFromSource(parametersAnnotation, parameterRecordsAnnotation, frameworkMethod()));
            // and now add the parameters from "parametersFor" methods or ones named in the annotations
            params.addAll(ParameterExtractor.paramsFromMethod(ParameterExtractor.toList(testClass), parametersAnnotation, parameterRecordsAnnotation, frameworkMethod()));
            // finally add the parameters from the "lambda" field in the @ParameterRecord-Annotation
            params.addAll(ParameterExtractor.paramsFromLambda(testClass, parameterRecordsAnnotation, frameworkMethod()));
            // Test whether there are any parameters
            if (params.isEmpty()) {
                throw new RuntimeException("Could not find parameters for " + frameworkMethod() + " so no params were used.");
            }
        }
        if (fileParametersAnnotation != null) {
            params.addAll(ParameterExtractor.paramsFromFile(fileParametersAnnotation, getClass()));
        }


        if (params != null) {
            return params.toArray();
        } else {
            return new Object[]{};
        }
    }

    @Override
    public boolean isParameterised() {
        return super.isParameterised()
                || frameworkMethod().getMethod().isAnnotationPresent(ParameterRecords.class)
                || frameworkMethod().getMethod().isAnnotationPresent((ParameterRecord.class));
    }
}
