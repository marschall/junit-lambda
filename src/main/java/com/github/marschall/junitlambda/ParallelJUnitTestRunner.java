package com.github.marschall.junitlambda;

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

import java.util.*;

/**
 * A JUnit Runner that will execute test methods in parallel, thus improving execution times in most cases.
 *
 * @author Alasdair Collinson
 * @since 0.2.0
 */
public class ParallelJUnitTestRunner extends BlockJUnit4ClassRunner {

    private List<FrameworkMethod> fFilteredChildren = null;

    private Sorter fSorter = Sorter.NULL;

    private RunnerScheduler fScheduler = new RunnerScheduler() {
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
    public ParallelJUnitTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        setScheduler(fScheduler);
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

    private void runChildren(final RunNotifier notifier) {
        getFilteredChildren().parallelStream().forEach(each -> {
            System.out.println(each.getName());
                fScheduler.schedule(() -> ParallelJUnitTestRunner.this.runChild(each, notifier));});
        fScheduler.finished();
    }

    private List<FrameworkMethod> getFilteredChildren() {
        if (fFilteredChildren == null) {
            fFilteredChildren = new ArrayList<>(getChildren());
        }
        return fFilteredChildren;
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        for (Iterator<FrameworkMethod> iterator = getFilteredChildren().iterator(); iterator.hasNext(); ) {
            FrameworkMethod each = iterator.next();
            if (shouldRun(filter, each)) {
                try {
                    filter.apply(each);
                } catch (NoTestsRemainException e) {
                    iterator.remove();
                }
            } else {
                iterator.remove();
            }
        }
        if (getFilteredChildren().isEmpty()) {
            throw new NoTestsRemainException();
        }
    }

    private boolean shouldRun(Filter filter, FrameworkMethod each) {
        return filter.shouldRun(describeChild(each));
    }

    @Override
    public void sort(Sorter sorter) {
        fSorter = sorter;
        getFilteredChildren().forEach(this::sortChild);
        Collections.sort(getFilteredChildren(), comparator());
    }

    private void sortChild(FrameworkMethod child) {
        fSorter.apply(child);
    }

    private Comparator<? super FrameworkMethod> comparator() {
        return (o1, o2) -> fSorter.compare(describeChild(o1), describeChild(o2));
    }

    @Override
    public Description getDescription() {
        Description description = Description.createSuiteDescription(getName(),
                getRunnerAnnotations());
        for (FrameworkMethod child : getFilteredChildren()) {
            description.addChild(describeChild(child));
        }
        return description;
    }
}
