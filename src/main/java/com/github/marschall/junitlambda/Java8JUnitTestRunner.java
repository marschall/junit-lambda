package com.github.marschall.junitlambda;

import com.github.marschall.junitlambda.annotations.FinalTest;
import com.github.marschall.junitlambda.annotations.FirstTest;
import com.github.marschall.junitlambda.annotations.ParallelTesting;
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
import java.util.*;
import java.util.stream.Stream;

/**
 * A JUnit Runner that can execute test methods in parallel, thus improving execution times in most cases.
 *
 * @author Alasdair Collinson
 * @since 0.2.0
 */
public class Java8JUnitTestRunner extends BlockJUnit4ClassRunner {

    private static final Logger LOG = LoggerFactory.getLogger(Java8JUnitTestRunner.class);

    private List<FrameworkMethod> childrenInstance = null;
    private Sorter sorter = Sorter.NULL;

    private List<Class<? extends Annotation>> testAnnotations = new ArrayList<>();
    private FrameworkMethod firstTest = null, finalTest = null;

    private RunnerScheduler scheduler = new RunnerScheduler() {
        public void schedule(Runnable childStatement) {
            childStatement.run();
        }

        public void finished() {
            // do nothing
        }
    };

    /**
     * Constructor.
     *
     * @param testClass The class from which tests are executed
     * @throws InitializationError
     */
    public Java8JUnitTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        setScheduler(scheduler);
        // A maximum of one FirstTest and one FinalTest annotation are allowed
        if(getTestClass().getAnnotatedMethods(FirstTest.class).size() > 1) {
            throw new InitializationError("There is more than one test method annotated with @FirstTest");
        }
        if(getTestClass().getAnnotatedMethods(FinalTest.class).size() > 1) {
            throw new InitializationError("There is more than one test method annotated with @FinalTest");
        }
        // Add the annotations which should be considered test annotations
        addTestAnnotation(FirstTest.class);
        addTestAnnotation(Test.class);
        addTestAnnotation(FinalTest.class);
    }

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
        boolean runInParallel = true;
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
        if(firstTest != null) {
            LOG.trace("Running first test");
            runTest(testClassName, firstTest, notifier);
        }
        stream.forEach(each -> {
            if (each.getAnnotation(FinalTest.class) != null) {
                setFinalTest(each);
            } else if(each.getAnnotation(FirstTest.class) == null) { // Skip the first test as we've already run it
                runTest(testClassName, each, notifier);
            }
        });
        // Run the final test
        if(finalTest != null) {
            LOG.trace("Running final test");
            runTest(testClassName, finalTest, notifier);
            finalTest = null;
        }
        scheduler.finished();
    }

    /**
     * Set the first test of this class to be run.
     *
     * @param firstTest the first test to be run
     */
    private void setFirstTest(FrameworkMethod firstTest) {
        this.firstTest = firstTest;
    }

    /**
     * Set the last test of this class to be run.
     *
     * @param finalTest the final test to be run
     */
    private void setFinalTest(FrameworkMethod finalTest) {
        this.finalTest = finalTest;
    }

    /**
     * Run a given test.
     *
     * @param testClassName the name of the class our test is implemented in
     * @param test the actual test method to be run
     * @param notifier the RunNotifier which will react to the starting and ending of a test run
     */
    private void runTest(String testClassName, FrameworkMethod test, RunNotifier notifier) {
        LOG.trace("Running test {}#{}", testClassName, test.getName());
        scheduler.schedule(() -> Java8JUnitTestRunner.this.runChild(test, notifier));
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
            List<FrameworkMethod> result = new ArrayList<>();
            for (Class<? extends Annotation> annotation : testAnnotations) {
                List<FrameworkMethod> annotatedTests = getTestClass().getAnnotatedMethods(annotation);
                if(annotation == FirstTest.class && annotatedTests.size() > 0) {
                    setFirstTest(annotatedTests.get(0));
                }
                result.addAll(annotatedTests);
            }
            return result;
        } else {
            return super.computeTestMethods();
        }
    }

    @Override
    public void sort(Sorter sorter) {
        this.sorter = sorter;
        getChildrenInstance().forEach(this.sorter::apply);
        Collections.sort(getChildrenInstance(), (o1, o2) -> this.sorter.compare(describeChild(o1), describeChild(o2)));
    }

    @Override
    public Description getDescription() {
        Description description = Description.createSuiteDescription(getName(),
                getRunnerAnnotations());
        for (FrameworkMethod child : getChildrenInstance()) {
            description.addChild(describeChild(child));
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