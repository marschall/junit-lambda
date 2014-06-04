package com.github.marschall.junitlambda;

import com.github.marschall.junitlambda.annotations.LastTest;
import com.github.marschall.junitlambda.annotations.FirstTest;
import com.github.marschall.junitlambda.annotations.ParallelTesting;
import com.github.marschall.junitlambda.runner.Java8JUnitTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static com.github.marschall.junitlambda.LambdaAssert.$$;
import static com.github.marschall.junitlambda.LambdaAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests to determine whether the parallel testing works as intended.
 *
 * @author Alasdair Collinson
 */
@RunWith(Java8JUnitTestRunner.class)
@ParallelTesting
public class ParallelTestingTest {
    private static final Logger LOG = LoggerFactory.getLogger(ParallelTestingTest.class);

    private static final Object lock = new Object();

    private static Set<Integer> testsRunning = new HashSet<>(),
            testsRun = new HashSet<>();

    private void test(int seconds) {
        LOG.trace("Test {} starting", seconds);
        markAsRunning(seconds);
        waitFor(seconds);
        LOG.trace("Test {}, currently running: {}", seconds, testsRunning.toArray());
        assertTrue(canMarkAsDone(seconds));
        LOG.trace("Test {} ending, currently active: {}", seconds, testsRunning);
    }

    private static void waitFor(int seconds) {
        final long startedAt = System.currentTimeMillis();
        final long waitFor = seconds * 1000L;
        long currentTime;
        do {
            currentTime = System.currentTimeMillis();
        } while (currentTime - startedAt > waitFor);
    }

    private static boolean canMarkAsDone(int id) {
        synchronized (lock) {
            if (testsRun.contains(id)) {
                return false;
            } else {
                testsRun.add(id);
                testsRunning.remove(id);
                return true;
            }
        }
    }

    private static void markAsRunning(int id) {
        synchronized (lock) {
            testsRunning.add(id);
        }
    }

    @FirstTest
    public void before() {
        LOG.trace("Running first test");
        assertThat(testsRun, $$("Set empty", s -> s.size() == 0));
        LOG.trace("Finished first test");
    }

    @Test
    public void test1() {
        test(1);
    }

    @Test
    public void test2() {
        test(2);
    }

    @Test
    public void test3() {
        test(3);
    }

    @Test
    public void test4() {
        test(4);
    }

    @LastTest
    public void after() {
        LOG.trace("Running final test");
        assertThat(testsRun, $$("4 tests run", s -> s.size() == 4));
        LOG.trace("Finished final test");
    }
}
