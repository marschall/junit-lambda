package com.github.marschall.junitlambda;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO AC: JavaDoc
 *
 * @author Alasdair Collinson
 */
public class ParallelJUnitTestRunner extends BlockJUnit4ClassRunner {

    private List<FrameworkMethod> fFilteredChildren = null;

    private RunnerScheduler fScheduler = new RunnerScheduler() {
        public void schedule(Runnable childStatement) {
            childStatement.run();
        }

        public void finished() {
            // do nothing
        }
    };

    /**
     * TODO AC: JavaDoc
     *
     * @param klass
     * @throws InitializationError
     */
    public ParallelJUnitTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
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
        getFilteredChildren().parallelStream().forEach(each ->
                fScheduler.schedule(() -> ParallelJUnitTestRunner.this.runChild(each, notifier)));
        fScheduler.finished();
    }

    private List<FrameworkMethod> getFilteredChildren() {
        if (fFilteredChildren == null) {
            fFilteredChildren = new ArrayList<>(getChildren());
        }
        return fFilteredChildren;
    }
}
