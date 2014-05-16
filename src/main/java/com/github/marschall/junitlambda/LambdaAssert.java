package com.github.marschall.junitlambda;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A set of Assertions useful for writing tests with lambda expressions. Only failed assertions
 * are recorded. These methods can be used directly:
 * <code>LambdaAssert.assertRaises(...)</code>, however, they read better if they
 * are referenced through static import:<br/>
 * <p>
 * <pre>
 * import static com.github.marschall.junitlambda.LambdaAssert.*;
 *    ...
 *    assertRaises(...);
 * </pre>
 *
 * @author Philippe Marschall
 * @author Alasdair Collinson
 * @see org.junit.Assert
 * @see java.lang.AssertionError
 */
public final class LambdaAssert {

    private static final MethodHandle EAT_EXCEPTION;
    private static final MethodHandle CALL_PROTECTED;

    private final static String PATTERN = "%s [for item %s]";

    static {
        try {
            Lookup lookup = MethodHandles.lookup();
            EAT_EXCEPTION = lookup.findStatic(LambdaAssert.class, "eatException", MethodType.methodType(Void.TYPE, Throwable.class));
            CALL_PROTECTED = lookup.findStatic(LambdaAssert.class, "callProtected", MethodType.methodType(Void.TYPE, String.class, Block.class, Class.class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("could not initialize LambdaAssert", e);
        }
    }

    /**
     * Protect constructor since it is a static only class
     */
    private LambdaAssert() {
        throw new AssertionError("not instantiable");
    }

    /**
     * Asserts that the evaluation of a block throws an exception. If it
     * does not an {@link AssertionError} is thrown with the given message.
     *
     * @param message  the identifying message for the {@link AssertionError}
     *                 ({@code null} okay)
     * @param block    block of code to be executed
     * @param expected expected exception type
     */
    public static void assertRaises(String message, Block block, Class<? extends Throwable> expected) {
        if (AssertionError.class.isAssignableFrom(expected)) {
            // needed to work around that an AssertionError is raised if no exception is raised
            boolean raised = true;
            try {
                block.value();
                raised = false;
            } catch (AssertionError e) {
                // we expect this
            } catch (Exception e) {
                exceptionCaught(null, expected, e);
            }
            if (!raised) {
                failNotRaised(null, expected);
            }
            return;
        }

        MethodHandle call = MethodHandles.insertArguments(CALL_PROTECTED, 0, message, block, expected);
        MethodHandle verification = MethodHandles.catchException(call, expected, EAT_EXCEPTION);
        try {
            verification.invokeWithArguments();
        } catch (Throwable e) {
            throw new AssertionError("unexpected exception: " + e.getClass() + " expected: " + expected, e);
        }
    }

    /**
     * Asserts that the evaluation of a block throws an exception. If it
     * does not an {@link AssertionError} is thrown.
     *
     * @param block    block of code to be executed
     * @param expected expected exception type
     */
    public static void assertRaises(Block block, Class<? extends Throwable> expected) {
        assertRaises(null, block, expected);
    }

    /**
     * Called by {@link Lookup#findStatic(Class, String, java.lang.invoke.MethodType)}.
     * TODO AC
     *
     * @param message  TODO AC
     * @param block    TODO AC
     * @param expected TODO AC
     * @throws Exception TODO AC
     */
    @SuppressWarnings("unused")
    private static void callProtected(String message, Block block, Class<? extends Throwable> expected) throws Exception {
        block.value();
        failNotRaised(message, expected);
    }

    /**
     * TODO AC
     *
     * @param message  TODO AC
     * @param expected TODO AC
     */
    private static void failNotRaised(String message, Class<? extends Throwable> expected) {
        fail(formatNotRaised(message, expected, null));
    }

    /**
     * Called by {@link Lookup#findStatic(Class, String, java.lang.invoke.MethodType)}.
     * TODO AC
     *
     * @param exception TODO AC
     */
    @SuppressWarnings("unused")
    private static void eatException(Throwable exception) {
        // expected
    }

    /**
     * TODO AC
     *
     * @param message  TODO AC
     * @param expected TODO AC
     * @param actual   TODO AC
     */
    private static void exceptionCaught(String message, Class<? extends Throwable> expected, Throwable actual) {
        fail(formatNotRaised(message, expected, actual));
    }

    /**
     * TODO AC
     *
     * @param message  TODO AC
     * @param expected TODO AC
     * @param actual   TODO AC
     * @return TODO AC
     */
    private static String formatNotRaised(String message, Class<? extends Throwable> expected, Throwable actual) {
        String formatted = "";
        if (message != null && !message.equals("")) {
            formatted = message + " ";
        }
        if (actual != null) {
            return formatted + "should have thrown: " + expected + " but did throw: " + actual.getClass();
        } else {
            return formatted + "should have thrown: " + expected + " but did not throw anything";
        }
    }

    /**
     * Asserts that a given {@link java.util.function.Predicate} is fulfilled for every member of the given {@link java.lang.Iterable}.
     *
     * @param msg       the identifying message for the {@link AssertionError} (<code>null</code> okay)
     * @param iterable  the iterable of which all elements must fulfill the given predicate
     * @param predicate the predicate to be checked
     * @param <T>       the type of elements in the iterable
     */
    public static <T> void assertForAll(String msg, Iterable<T> iterable, Predicate<T> predicate) {
        iterable.forEach(item -> assertTrue(String.format(PATTERN, msg, iterable.toString()), predicate.test(item)));
    }

    /**
     * Asserts that a given {@link java.util.function.Predicate} is fulfilled for every member of the given {@link java.lang.Iterable}.
     *
     * @param iterable  the iterable of which all elements must fulfill the given predicate
     * @param predicate the predicate to be checked
     * @param <T>       the type of elements in the iterable
     */
    public static <T> void assertForAll(Iterable<T> iterable, Predicate<T> predicate) {
        assertForAll("", iterable, predicate);
    }

    /**
     * Asserts that a given {@link java.util.function.Predicate} is fulfilled for every member of the given {@link java.util.Map}
     *
     * @param msg       the identifying message for the {@link AssertionError} (<code>null</code> okay)
     * @param map       the map of which all elements must fulfill the given predicate
     * @param predicate the predicate to be checked
     * @param <T>       the type of elements in the map
     */
    public static <T> void assertForAll(String msg, Map<?, T> map, Predicate<T> predicate) {
        map.forEach((k, e) -> assertTrue(String.format(PATTERN, msg, map.toString()), predicate.test(e)));
    }

    /**
     * An assertion for all Elements of a Map
     *
     * @param map       the map of which all elements must fulfill the given predicate
     * @param predicate the predicate to be checked
     * @param <T>       the type of elements in the map
     */
    public static <T> void assertForAll(Map<?, T> map, Predicate<T> predicate) {
        assertForAll("", map, predicate);
    }

    /**
     * Asserts that <code>actual</code> satisfies the condition specified by
     * <code>predicate</code>. If not, an {@link AssertionError} is thrown with
     * the reason and information about the matcher and failing value. Example:
     *
     * <pre>
     *   assertThat(&quot;Help! Integers don't work&quot;, 0, n -> n == 1); // fails:
     *     // failure message:
     *     // Help! Integers don't work
     *     // expected: is &lt;1&gt;
     *     // got value: &lt;0&gt;
     *   assertThat(&quot;Zero is one&quot;, 0, is(not(1))) // passes
     * </pre>
     *
     * @param msg TODO AC
     * @param actual TODO AC
     * @param predicate TODO AC
     * @param <T> TODO AC
     */
    public static <T> void assertThat(String msg, T actual, Predicate<T> predicate) {
        if(!predicate.test(actual)) {
            StringBuilder builder = new StringBuilder();
            builder.append(msg);
            builder.append("\nExpected: ");
            builder.append(actual);
            // TODO: ideally print string representation of predicate
            throw new AssertionError(builder.toString());
        }
//        Assert.assertThat(msg, actual, new BaseMatcher<T>() {
//            @SuppressWarnings("unchecked")
//            @Override
//            public boolean matches(Object item) {
//                return predicate.test((T) item);
//            }
//
//            @Override
//            public void describeTo(Description description) {
//                // TODO: Improve readability
//                // currently prints as 'Expected: com.something.Whatever$$Lambda$1/1234567890@1b23456f'
//                description.appendValue(predicate);
//            }
//        });
    }

    public static <T> void assertThat(T actual, Predicate<T> predicate) {
        assertThat(null, actual, predicate);
    }
}
