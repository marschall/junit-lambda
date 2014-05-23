package com.github.marschall.junitlambda.annotations;

import java.lang.annotation.*;

/**
 * Allows setting whether the tests in a test class may be run in parallel or not.<p/>
 * This is read by the {@link com.github.marschall.junitlambda.runner.Java8JUnitTestRunner} and if it is absent the test are
 * run in parallel by default. If you want to prevent this, add the following annotation to your test class:
 * <pre>@ParallelTesting(parallel = false)</pre>
 *
 * @author Alasdair Collinson
 * @see com.github.marschall.junitlambda.runner.Java8JUnitTestRunner
 * @since 0.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface ParallelTesting {

    /**
     * Should the tests be run in parallel?
     */
    boolean parallel() default true;
}
