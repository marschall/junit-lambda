package com.github.marschall.junitlambda.runner;

import com.github.marschall.junitlambda.annotations.FirstTest;
import com.github.marschall.junitlambda.annotations.LastTest;
import com.github.marschall.junitlambda.annotations.ParallelTesting;
import junitparams.internal.ParameterisedTestClassRunner;
import junitparams.internal.TestMethod;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * A JUnit Runner that can execute test methods in parallel, thus improving execution times in most cases.<p/>
 * It is also built with the {@link junitparams.JUnitParamsRunner} in mind, thereby enabeling parameterised testing.
 *
 * @author Alasdair Collinson
 * @since 0.2.0
 * @see junitparams.JUnitParamsRunner
 */
public class Java8JUnitTestRunner extends BlockJUnit4ClassRunner {

    private static final Logger LOG = LoggerFactory.getLogger(Java8JUnitTestRunner.class);

    /**
     * TODO AC: JavaDoc
     */
    private Sorter sorter = Sorter.NULL;
    /**
     * TODO AC: JavaDoc
     */
    private ParameterisedTestClassRunner parameterisedRunner;
    /**
     * TODO AC: JavaDoc
     */
    private RunnerScheduler scheduler = new RunnerScheduler() {
        public void schedule(Runnable childStatement) {
            childStatement.run();
        }

        public void finished() {
            // do nothing
        }
    };

    /**
     * TODO AC: JavaDoc
     */
    private List<FrameworkMethod> childrenInstance;
    /**
     * TODO AC: JavaDoc
     */
    private List<Class<? extends Annotation>> testAnnotations = new ArrayList<>();
    /**
     * TODO AC: JavaDoc
     */
    private FrameworkMethod firstTest, finalTest;
    /**
     * TODO AC: JavaDoc
     */
    private Description description;

    /**
     * Constructor.
     *
     * @param testClass The class from which tests are executed
     * @throws InitializationError
     */
    public Java8JUnitTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);

        setScheduler(scheduler);
        // A maximum of one FirstTest and one LastTest annotation are allowed
        if (getTestClass().getAnnotatedMethods(FirstTest.class).size() > 1) {
            throw new InitializationError("There is more than one test method annotated with @FirstTest");
        }
        if (getTestClass().getAnnotatedMethods(LastTest.class).size() > 1) {
            throw new InitializationError("There is more than one test method annotated with @LastTest");
        }
        // Add the annotations which should be considered test annotations
        addTestAnnotation(FirstTest.class);
        addTestAnnotation(Test.class);
        addTestAnnotation(LastTest.class);

        // TMP
        parameterisedRunner = new ParameterisedJava8TestRunner(getTestClass(), testAnnotations) {
            @Override
            protected void onFirstTest(TestMethod testMethod) {
                setFirstTest(testMethod.frameworkMethod());
            }

            @Override
            protected void onLastTest(TestMethod testMethod) {
                setLastTest(testMethod.frameworkMethod());
            }
        };
        // TMP
    }

    //
    // Test running
    //

    @Override
    protected Statement childrenInvoker(RunNotifier notifier) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                runChildren(notifier);
            }
        };
    }

    /**
     * The function that actually runs the tests.
     *
     * @param notifier the RunNotifier which will react to the starting and ending of a test run
     * @see org.junit.runners.ParentRunner#runChildren(org.junit.runner.notification.RunNotifier)
     */
    private void runChildren(final RunNotifier notifier) throws InitializationError {
        // Determine whether the tests can be run in parallel
        boolean runInParallel = false;
        ParallelTesting[] parallelTestings = getTestClass().getJavaClass().getAnnotationsByType(ParallelTesting.class);
        if (parallelTestings.length > 0) {
            runInParallel = parallelTestings[0].parallel();
        }
        // Depending on whether the tests can be run in parallel, use either a parallel Stream or a sequential one
        Stream<FrameworkMethod> stream;
        if (runInParallel) {
            stream = getChildrenInstance().parallelStream();
            LOG.trace("Running tests in parallel");
        } else {
            stream = getChildrenInstance().stream();
            LOG.trace("Running tests in sequence");
        }
        // Reset the finalTest field to null
        finalTest = null;

        // What class are we testing from?
        final String testClassName = getTestClass().getJavaClass().getName();

        // Run the tests
        if (firstTest != null) {
            LOG.trace("Running first test");
            runTest(testClassName, firstTest, notifier);
        }
        stream.forEach(each -> {
            if (each.getAnnotation(LastTest.class) != null) {
                setLastTest(each);
            } else if (each.getAnnotation(FirstTest.class) == null) { // Skip the first test as we've already run it
                runTest(testClassName, each, notifier);
            }
        });
        // Run the final test
        if (finalTest != null) {
            LOG.trace("Running final test");
            runTest(testClassName, finalTest, notifier);
            finalTest = null;
        }
        scheduler.finished();
    }

    /**
     * Run a given test.
     *
     * @param testClassName the name of the class our test is implemented in
     * @param test          the actual test method to be run
     * @param notifier      the RunNotifier which will react to the starting and ending of a test run
     */
    private void runTest(String testClassName, FrameworkMethod test, RunNotifier notifier) {
        LOG.trace("Running test {}#{}", testClassName, test.getName());
        scheduler.schedule(() -> Java8JUnitTestRunner.this.runChild(test, notifier));
    }

    //
    // Test setup
    //

    /**
     * Set the first test of this class to be run.
     *
     * @param firstTest the first test to be run
     */
    void setFirstTest(FrameworkMethod firstTest) {
        this.firstTest = firstTest;
    }

    /**
     * Set the last test of this class to be run.
     *
     * @param finalTest the final test to be run
     */
    void setLastTest(FrameworkMethod finalTest) {
        this.finalTest = finalTest;
    }

    /**
     * @return a List of the test methods.
     * @see org.junit.runners.ParentRunner#getFilteredChildren()
     */
    private List<FrameworkMethod> getChildrenInstance() {
        if (childrenInstance == null) {
            childrenInstance = new ArrayList<>(getChildren());
        }
        return childrenInstance;
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        for (Iterator<FrameworkMethod> iterator = getChildrenInstance().iterator(); iterator.hasNext(); ) {
            FrameworkMethod each = iterator.next();
            if (filter.shouldRun(describeChild(each))) {
                try {
                    filter.apply(each);
                } catch (NoTestsRemainException e) {
                    iterator.remove();
                }
            } else {
                iterator.remove();
            }
        }
        if (getChildrenInstance().isEmpty()) {
            throw new NoTestsRemainException();
        }
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        if (testAnnotations != null) {
            return parameterisedRunner.computeFrameworkMethods();
        } else {
            return super.computeTestMethods();
        }
    }

    //
    // Parameterizing tests
    //

    /**
     * TODO AC: JavaDoc
     *
     * @param method
     * @param notifier
     * @see junitparams.JUnitParamsRunner#run(org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        if (handleIgnored(method, notifier))
            return;

        TestMethod testMethod = parameterisedRunner.testMethodFor(method);
        if (parameterisedRunner.shouldRun(testMethod))
            parameterisedRunner.runParameterisedTest(testMethod, methodBlock(method), notifier);
        else
            super.runChild(method, notifier);
    }

    /**
     * TODO AC: JavaDoc
     *
     * @param method
     * @param notifier
     * @return
     * @see junitparams.JUnitParamsRunner#handleIgnored(org.junit.runners.model.FrameworkMethod, org.junit.runner.notification.RunNotifier)
     */
    private boolean handleIgnored(FrameworkMethod method, RunNotifier notifier) {
        TestMethod testMethod = parameterisedRunner.testMethodFor(method);
        if (testMethod.isIgnored())
            notifier.fireTestIgnored(describeMethod(method));

        return testMethod.isIgnored();
    }

    /**
     * TODO TMP
     *
     * @param method
     * @return
     * @see junitparams.JUnitParamsRunner#describeMethod(org.junit.runners.model.FrameworkMethod)
     */
    protected Description describeMethod(FrameworkMethod method) {
        Description child = parameterisedRunner.describeParameterisedMethod(method);

        if (child == null)
            child = describeChild(method);

        return child;
    }

    /**
     * TODO TMP
     *
     * @param method
     * @param test
     * @return
     * @see junitparams.JUnitParamsRunner#methodInvoker(org.junit.runners.model.FrameworkMethod, Object)
     */
    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        Statement methodInvoker = parameterisedRunner.parameterisedMethodInvoker(method, test);
        if (methodInvoker == null)
            methodInvoker = super.methodInvoker(method, test);

        return methodInvoker;
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        // do nothing
    }

    //
    // Other stuff
    //

    @Override
    public void sort(Sorter sorter) {
        this.sorter = sorter;
        getChildrenInstance().forEach(this.sorter::apply);
        Collections.sort(getChildrenInstance(), (o1, o2) -> this.sorter.compare(describeChild(o1), describeChild(o2)));
    }

    /**
     * TODO AC: JavaDoc
     *
     * @return
     * @see junitparams.JUnitParamsRunner#getDescription()
     */
    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), getRunnerAnnotations());
            List<FrameworkMethod> resultMethods = parameterisedRunner.returnListOfMethods();

            for (FrameworkMethod method : resultMethods)
                description.addChild(describeMethod(method));
        }

        return description;
    }

    /**
     * Allows a child class to add a test annotation.
     *
     * @param annotation an annotation which should be considered a test annotation
     */
    protected final void addTestAnnotation(Class<? extends Annotation> annotation) {
        testAnnotations.add(annotation);
    }
}