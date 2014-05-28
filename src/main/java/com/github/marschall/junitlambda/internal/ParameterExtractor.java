package com.github.marschall.junitlambda.internal;

import com.github.marschall.junitlambda.annotations.ParameterRecord;
import com.github.marschall.junitlambda.annotations.ParameterRecords;
import com.sun.xml.internal.ws.util.StreamUtils;
import junitparams.FileParameters;
import junitparams.Parameters;
import junitparams.mappers.DataMapper;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.type.NullType;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * TODO AC: JavaDoc
 *
 * @author Alasdair Collinson
 * @since 0.2.0
 */
class ParameterExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(ParameterExtractor.class);

    private ParameterExtractor() {
    }

    @SafeVarargs
    static <T> List<T> toList(T... array) {
        return Stream.of(array).collect(Collectors.toList());
    }

    static List<Object> paramsFromAnnotation(Parameters parametersAnnotation, ParameterRecords parameterRecordsAnnotation) {
        List<Object> result = toList();
        if (parametersAnnotation != null) {
            result.addAll(toList(parametersAnnotation.value()));
        }
        if (parameterRecordsAnnotation != null) {
            // For each ParameterRecord annotation, add all values that aren't empty
            result.addAll(Arrays.stream(parameterRecordsAnnotation.value()).map(ParameterRecord::value).
                    filter(array -> array.length != 0).collect(Collectors.toList()));
        }
        return result;
    }

    static List<Object> paramsFromFile(FileParameters fileParametersAnnotation, Class<?> testMethodClass) {
        try (Reader reader = createProperReader(fileParametersAnnotation, testMethodClass)) {
            DataMapper mapper = fileParametersAnnotation.mapper().newInstance();
            return toList(mapper.map(reader));
        } catch (IOException | IllegalAccessException | InstantiationException e) {
            LOG.error("Error while retrieving parameters from file", e);
            throw new RuntimeException("Could not successfully read parameters from file: " + fileParametersAnnotation.value(), e);
        }
    }

    private static Reader createProperReader(FileParameters fileParametersAnnotation, Class<?> testMethodClass) throws IOException {
        String filepath = fileParametersAnnotation.value();

        if (filepath.indexOf(':') < 0) {
            return new FileReader(filepath);
        }
        String protocol = filepath.substring(0, filepath.indexOf(':'));
        String filename = filepath.substring(filepath.indexOf(':') + 1);

        if ("classpath".equals(protocol)) {
            return new InputStreamReader(testMethodClass.getClassLoader().getResourceAsStream(filename));
        } else if ("file".equals(protocol)) {
            return new FileReader(filename);
        }

        throw new IllegalArgumentException("Unknown file access protocol. Only 'file' and 'classpath' are supported!");
    }

    /**
     * Collects the parameters given by source classes as {@link junitparams.Parameters#source()} and
     * {@link com.github.marschall.junitlambda.annotations.ParameterRecord#source()} values.
     *
     * @return a list of parameters defined in the <code>#source()</code> values of the recognised annotations.
     */
    static List<Object> paramsFromSource(Parameters parametersAnnotation, ParameterRecords parameterRecordsAnnotation, FrameworkMethod frameworkMethod) {
        // if the source class is undefined
        boolean noSourceGiven = false;
        if (parametersAnnotation == null && parameterRecordsAnnotation == null) {
            noSourceGiven = true;
        } else if (parametersAnnotation != null && parametersAnnotation.source().isAssignableFrom(NullType.class)) {
            noSourceGiven = true;
        } else if (parameterRecordsAnnotation != null && parameterRecordsAnnotation.value() != null && parameterRecordsAnnotation.value().length != 0) {
            boolean nullType = true;
            for (ParameterRecord parameterRecord : parameterRecordsAnnotation.value()) {
                if (!parameterRecord.source().isAssignableFrom(NullType.class)) {
                    nullType = false;
                    break;
                }
            }
            if (nullType) {
                noSourceGiven = true;
            }
        }
        if (parametersAnnotation != null) {
            noSourceGiven = false;
        }
        if (noSourceGiven) {
            return toList();
        }

        List<Class<?>> sourceClasses = new ArrayList<>();

        if (parametersAnnotation != null) {
            sourceClasses.add(parametersAnnotation.source());
        }
        if (parameterRecordsAnnotation != null) {
            Stream<ParameterRecord> parameterRecordStream = Arrays.asList(parameterRecordsAnnotation.value()).stream();
            sourceClasses.addAll(parameterRecordStream.map(ParameterRecord::source).collect(Collectors.toList()));
        }

        List<Object> params = toList();
        List<Object> providerMethods = fillResultWithAllParamProviderMethods(sourceClasses, frameworkMethod);
        if(!providerMethods.isEmpty()
                && providerMethods.get(0) instanceof Object[]
                && ((Object[]) providerMethods.get(0)).length > 0
                && ((Object[]) providerMethods.get(0))[0] instanceof Object[]) {
            List<Object> flatProviderMethods =
                    providerMethods
                            .stream()
                            .map(object -> (Object[]) object)
                            .flatMap(array -> Arrays.stream(array))
                            .collect(Collectors.toList());
            params.addAll(flatProviderMethods);
        } else {
            params.addAll(providerMethods);
        }
        params.addAll(paramsFromMethod(sourceClasses, parametersAnnotation, parameterRecordsAnnotation, frameworkMethod));
        return params;
    }

    static List<Object> paramsFromMethod(List<Class<?>> classesWithMethods, Parameters parametersAnnotation, ParameterRecords parameterRecordsAnnotation, FrameworkMethod frameworkMethod) {
        List<String> methodAnnotations = toList();
        if (parametersAnnotation != null) {
            methodAnnotations.addAll(toList(parametersAnnotation.method()));
        }

        List<Object> result = toList();
        methodAnnotations.forEach(methodAnnotation -> {
            if (methodAnnotation.isEmpty()) {
                classesWithMethods.forEach(classWithMethod -> {
                    String methodName = defaultMethodName(frameworkMethod);
                    try {
                        result.addAll(invokeMethodWithParams(defaultMethodName(frameworkMethod), classWithMethod, frameworkMethod));
                    } catch (NoSuchMethodException e) {
                        // that's ok, we'll just try the next class
                        LOG.trace("No method " + methodName + " could be found in class " + classWithMethod.getName());
                    }
                    if (result.isEmpty()) {
                        LOG.trace("No method " + methodName + " could be found in any of the tested classes");
                    }
                });
            } else {
                classesWithMethods.forEach(classWithMethod -> {
                    Stream<List<Object>> invokedMethods = Stream.of(methodAnnotation.split(",")).
                            map(methodName -> {
                                try {
                                    return invokeMethodWithParams(methodName.trim(), classWithMethod, frameworkMethod);
                                } catch (NoSuchMethodException e) {
                                    LOG.trace("No method " + methodName + " could be found in class " + classWithMethod.getName());
                                    return toList();
                                }
                            });
                    Optional<List<Object>> combinedMethods = invokedMethods.reduce((list1, list2) -> {
                        list1.addAll(list2);
                        return list1;
                    });
                    if (combinedMethods.isPresent()) {
                        result.addAll(combinedMethods.get());
                    }
                });
            }
        });

        if (parameterRecordsAnnotation != null && parameterRecordsAnnotation.value().length > 0) {
            for (ParameterRecord parameterRecord : parameterRecordsAnnotation.value()) {
                String methodName = parameterRecord.method();
                classesWithMethods.forEach(classWithMethod -> {
                    try {
                        if (methodName == null || methodName.isEmpty()) {
                            result.addAll(invokeMethodWithParams(defaultMethodName(frameworkMethod), classWithMethod, frameworkMethod));
                        } else {
                            List<Object> invoked = invokeMethodWithParams(methodName.trim(), classWithMethod, frameworkMethod);
                            result.addAll(invoked);
                        }
                    } catch (NoSuchMethodException e) {
                        LOG.trace("No method " + methodName + " could be found in class " + classWithMethod.getName());
                    }
                });
            }
        }

        return result;
    }

    private static List<Object> invokeMethodWithParams(String methodName, Class<?> testClass, FrameworkMethod frameworkMethod) throws NoSuchMethodException {
        Method provideMethod = findParamsProvidingMethodInTestclassHierarchy(methodName, testClass);

        return invokeParamsProvidingMethod(testClass, provideMethod, frameworkMethod);
    }

    private static Method findParamsProvidingMethodInTestclassHierarchy(String methodName, Class<?> testClass) throws NoSuchMethodException {
        Method provideMethod = null;
        NoSuchMethodException exception = null;
        for (Class<?> declaringClass = testClass; declaringClass.getSuperclass() != null; declaringClass = declaringClass.getSuperclass()) {
            try {
                provideMethod = declaringClass.getDeclaredMethod(methodName);
                break;
            } catch (NoSuchMethodException e) {
                if (exception == null) {
                    exception = e;
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
        return provideMethod;
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private static List<Object> invokeParamsProvidingMethod(Class<?> testClass, Method provideMethod, FrameworkMethod frameworkMethod) {
        if (provideMethod == null) {
            return toList();
        }
        try {
            Object testObject = testClass.newInstance();
            provideMethod.setAccessible(true);
            Object invocationResult = provideMethod.invoke(testObject);
            try {
                List<Object> params = toList((Object[]) invocationResult);
                return encapsulateParamsIntoArrayIfSingleParamsetPassed(params, frameworkMethod);
            } catch (ClassCastException e) {
                // Iterable
                try {
                    List<Object[]> res;
                    res = StreamSupport.stream(((Iterable<Object[]>) invocationResult).spliterator(), true).
                            collect(Collectors.toList());
                    return toList(res.toArray());

                } catch (ClassCastException e1) {
                    // Iterable with consecutive paramsets, each of one param
                    List<Object> res;
                    res = StreamSupport.stream(((Iterable<Object>) invocationResult).spliterator(), true).
                            map(param -> new Object[]{param}).collect(Collectors.toList());
                    return res;
                }
            }
        } catch (ClassCastException e) {
            throw new RuntimeException("The return type of: " + provideMethod.getName() + " defined in class "
                    + testClass + " is not Object[][] nor Iterable<Object[]>. Fix it!", e);
        } catch (Exception e) {
            throw new RuntimeException("Could not invoke method: " + provideMethod.getName() + " defined in class "
                    + testClass + " so no params were used.", e);
        }
    }

    private static List<Object> fillResultWithAllParamProviderMethods(List<Class<?>> sourceClasses, FrameworkMethod frameworkMethod) {
        // send all source classes that are not NullType through the getParamsFromSourceHierachy function
        List<Object> result = sourceClasses.stream().
                filter(sourceClass -> !sourceClass.isAssignableFrom(NullType.class)).
                map(sourceClass -> getParamsFromSourceHierarchy(sourceClass, frameworkMethod)).
                map(List<Object>::toArray).
                collect(Collectors.toList());
        return result;
    }

    private static List<Object> getParamsFromSourceHierarchy(Class<?> sourceClass, FrameworkMethod frameworkMethod) {
        List<Object> result = new ArrayList<>();
        while (sourceClass.getSuperclass() != null) {
            result.addAll(gatherParamsFromAllMethodsFrom(sourceClass, frameworkMethod));
            sourceClass = sourceClass.getSuperclass();
        }

        return result;
    }

    private static List<Object> gatherParamsFromAllMethodsFrom(Class<?> sourceClass, FrameworkMethod frameworkMethod) {
        List<Object> result = new ArrayList<>();
        Method[] methods = sourceClass.getDeclaredMethods();
        for (Method providerMethod : methods) {
            if (providerMethod.getName().startsWith("provide")) {
                if (!Modifier.isStatic(providerMethod.getModifiers()))
                    throw new RuntimeException("Parameters source method " +
                            providerMethod.getName() +
                            " is not declared as static. Change it to a static method.");
                try {
                    result.addAll(getDataFromMethod(providerMethod, frameworkMethod));
                } catch (Exception e) {
                    throw new RuntimeException("Cannot invoke parameters source method: " + providerMethod.getName(), e);
                }
            }
        }
        return result;
    }

    private static String defaultMethodName(FrameworkMethod frameworkMethod) {
        String methodName;
        methodName = "parametersFor" + frameworkMethod.getName().substring(0, 1).toUpperCase()
                + frameworkMethod.getName().substring(1);
        return methodName;
    }

    private static List<Object> getDataFromMethod(Method providerMethod, FrameworkMethod frameworkMethod) throws IllegalAccessException, InvocationTargetException {
        return encapsulateParamsIntoArrayIfSingleParamsetPassed(toList((Object[]) providerMethod.invoke(null)), frameworkMethod);
    }

    private static List<Object> encapsulateParamsIntoArrayIfSingleParamsetPassed(List<Object> params, FrameworkMethod frameworkMethod) {
        if (frameworkMethod.getMethod().getParameterTypes().length != params.size())
            return params;

        if (params.size() == 0)
            return params;

        Object param = params.get(0);
        if (param == null || !param.getClass().isArray())
            return params;

        return params;
    }
}
