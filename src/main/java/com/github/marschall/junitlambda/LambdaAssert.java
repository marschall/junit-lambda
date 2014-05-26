package com.github.marschall.junitlambda;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.concurrent.Callable;
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

    private static final String FAILED_MSG = "Test failed";

    private static final MethodHandle EAT_EXCEPTION;
    private static final MethodHandle CALL_PROTECTED_CALLABLE;
    private static final MethodHandle CALL_PROTECTED_BLOCK;

    private final static String PATTERN = "%s [for item %s]";

    static {
        try {
            Lookup lookup = MethodHandles.lookup();
            EAT_EXCEPTION = lookup.findStatic(RaiseHandler.class, "eatException",
                    MethodType.methodType(Void.TYPE, Throwable.class));
            CALL_PROTECTED_CALLABLE = lookup.findStatic(RaiseHandler.class, "callProtected",
                    MethodType.methodType(Void.TYPE, String.class, Callable.class, Class.class));
            CALL_PROTECTED_BLOCK = lookup.findStatic(RaiseHandler.class, "callProtected",
                    MethodType.methodType(Void.TYPE, String.class, Block.class, Class.class));
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
    public static void assertVoidRaises(String message, Block block, Class<? extends Throwable> expected) {
        if (AssertionError.class.isAssignableFrom(expected)) {
            // needed to work around that an AssertionError is raised if no exception is raised
            boolean raised = true;
            try {
                block.value();
                raised = false;
            } catch (AssertionError e) {
                // we expect this
            } catch (Exception e) {
                RaiseHandler.exceptionCaught(null, expected, e);
            }
            if (!raised) {
                RaiseHandler.failNotRaised(null, expected);
            }
            return;
        }

        MethodHandle call = MethodHandles.insertArguments(CALL_PROTECTED_BLOCK, 0, message, block, expected);
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
    public static void assertVoidRaises(Block block, Class<? extends Throwable> expected) {
        assertVoidRaises(null, block, expected);
    }

    /**
     * Asserts that the evaluation of a block throws an exception. If it
     * does not an {@link AssertionError} is thrown with the given message.
     *
     * @param message  the identifying message for the {@link AssertionError}
     *                 ({@code null} okay)
     * @param callable block of code to be executed
     * @param expected expected exception type
     */
    public static void assertRaises(String message, Callable<?> callable, Class<? extends Throwable> expected) {
        if (AssertionError.class.isAssignableFrom(expected)) {
            // needed to work around that an AssertionError is raised if no exception is raised
            boolean raised = true;
            try {
                callable.call();
                raised = false;
            } catch (AssertionError e) {
                // we expect this
            } catch (Exception e) {
                RaiseHandler.exceptionCaught(null, expected, e);
            }
            if (!raised) {
                RaiseHandler.failNotRaised(null, expected);
            }
            return;
        }

        MethodHandle call = MethodHandles.insertArguments(CALL_PROTECTED_CALLABLE, 0, message, callable, expected);
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
     * @param callable block of code to be executed
     * @param expected expected exception type
     */
    public static void assertRaises(Callable<?> callable, Class<? extends Throwable> expected) {
        assertRaises(null, callable, expected);
    }

    /**
     * Asserts that a given {@link java.util.function.Predicate} is fulfilled for every member of the given
     * {@link java.lang.Iterable}.
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
     * Asserts that a given {@link java.util.function.Predicate} is fulfilled for every member of the given
     * {@link java.lang.Iterable}.
     *
     * @param iterable  the iterable of which all elements must fulfill the given predicate
     * @param predicate the predicate to be checked
     * @param <T>       the type of elements in the iterable
     */
    public static <T> void assertForAll(Iterable<T> iterable, Predicate<T> predicate) {
        assertForAll("", iterable, predicate);
    }

    /**
     * Asserts that a given {@link java.util.function.Predicate} is fulfilled for every member of the given
     * {@link java.util.Map}
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
     * <p>
     * <pre>
     *   assertThat(&quot;Why isn't zero the same as one?&quot;, 0, $(&quot;equals one&quot;, n -> n == 1)); // fails:
     *     // failure message:
     *     // Why isn't zero the same as one?
     *     // Input: 0
     *     // Predicate: equals one
     *   assertThat(&quot;One equals one.&quot;, 1, $(&quot;equals one&quot;, n -> n == 1)); // passes
     * </pre>
     *
     * @param msg       the identifying message for the {@link AssertionError} (<code>null</code> okay)
     * @param input     the value with which the predicate shall be tested
     * @param predicate the predicate to test upon the given input value
     * @param <T>       the static type accepted by the predicate
     * @see com.github.marschall.junitlambda.LambdaAssert#$$(String, java.util.function.Predicate)
     */
    public static <T> void assertThat(String msg, T input, Predicate<T> predicate) {
        if (!predicate.test(input)) {
            StringBuilder builder = new StringBuilder();
            if (msg != null && !"".equals(msg)) {
                builder.append(msg);
            } else {
                builder.append(FAILED_MSG);
            }
            builder.append("\nInput: ");
            builder.append(input);
            // TODO: ideally print string representation of predicate
            if (predicate instanceof NamedPredicate) {
                builder.append("\nPredicate: ");
                builder.append(predicate);
            }
            throw new AssertionError(builder.toString());
        }
    }

    /**
     * Asserts that <code>actual</code> satisfies the condition specified by
     * <code>predicate</code>. If not, an {@link AssertionError} is thrown with
     * the reason and information about the matcher and failing value. Example:
     * <p>
     * <pre>
     *   assertThat(&quot;Why isn't zero the same as one?&quot;, 0, $(&quot;equals one&quot;, n -> n == 1)); // fails:
     *     // failure message:
     *     // Why isn't zero the same as one?
     *     // Input: 0
     *     // Predicate: equals one
     *   assertThat(&quot;One equals one.&quot;, 1, $(&quot;equals one&quot;, n -> n == 1)); // passes
     * </pre>
     *
     * @param input     the value with which the predicate shall be tested
     * @param predicate the predicate to test upon the given input value
     * @param <T>       the static type accepted by the predicate
     * @see com.github.marschall.junitlambda.LambdaAssert#$$(String, java.util.function.Predicate)
     */
    public static <T> void assertThat(T input, Predicate<T> predicate) {
        assertThat(null, input, predicate);
    }

    /**
     * TODO AC: JavaDoc
     *
     * @param message
     * @param callable
     */
    public static void assertThat(String message, Callable<Boolean> callable) throws AssertionError {
        try {
            assertTrue(message, callable.call());
        } catch(Exception e) {
            throw new AssertionError(message != null ? message : "Error in Test", e);
        }
    }

    /**
     * TODO AC: JavaDoc
     *
     * @param callable
     */
    public static void assertThat(Callable<Boolean> callable) throws AssertionError {
        assertThat(null, callable);
    }

    /**
     * Runs a block which is expected to fail, throwing an {@link java.lang.AssertionError}. JUnit assertions do this
     * when they fail.
     *
     * @param message the identifying message for the {@link AssertionError} ({@code null} okay)
     * @param block   block of code to be executed
     */
    public static void assertFaiure(String message, Block block) {
        boolean fail = false;
        try {
            block.value();
            fail = true;
        } catch (AssertionError e) {
            RaiseHandler.eatException(e);
        } catch (Exception e) {
            throw new AssertionError(message == null ? "Expression did not fail in the way expected" : message, e);
        }
        if (fail) {
            fail(message);
        }
    }

    /**
     * Runs a block which is expected to fail, throwing an {@link java.lang.AssertionError}. JUnit assertions do this
     * when they fail.
     *
     * @param block block of code to be executed
     */
    public static void assertFaiure(Block block) {
        assertFaiure(null, block);
    }

    /**
     * Factory method to create instances of {@link com.github.marschall.junitlambda.LambdaAssert.NamedPredicate}s
     * which wrap around the given {@link java.util.function.Predicate} while adding a short human readable description
     * of said predicate.
     *
     * @param description a short description of the given predicate. Can be a string representation, e.g.
     *                    <code>"n -> n == 1"</code> or a more verbose explanation, e.g. <code>"equals one"</code>.
     * @param predicate   the predicate which should be wrapped
     * @param <T>         the static type accepted by the predicate
     * @return a self describing named predicate
     * @see com.github.marschall.junitlambda.LambdaAssert#assertThat(String, Object, java.util.function.Predicate)
     */
    public static <T> Predicate<T> $$(String description, Predicate<T> predicate) {
        return new NamedPredicate<>(description, predicate);
    }

    /**
     * A utility class for bundeling various functions required for the
     * {@link com.github.marschall.junitlambda.LambdaAssert#assertRaises(String, java.util.concurrent.Callable, Class)} and
     * {@link com.github.marschall.junitlambda.LambdaAssert#assertRaises(java.util.concurrent.Callable, Class)} functions.
     */

    private static class RaiseHandler {

        private RaiseHandler() {
            throw new AssertionError("not instantiable");
        }

        /**
         * Called by {@link Lookup#findStatic(Class, String, java.lang.invoke.MethodType)}.<p/>
         * Executes the {@link com.github.marschall.junitlambda.Block#value()} function of a given Block expression. If this
         * doesn't raise an exception, an AssertionError is thrown.
         *
         * @param message  the identifying message for the {@link AssertionError} ({@code null} okay)
         * @param callable the {@link com.github.marschall.junitlambda.Block} expression which should be executed
         * @param expected the expected Exception
         * @throws Exception the Exception which is thrown if the given Block expression works as intended
         */
        @SuppressWarnings("unused")
        static void callProtected(String message, Callable callable, Class<? extends Throwable> expected)
                throws Exception {
            callable.call();
            failNotRaised(message, expected);
        }

        /**
         * Called by {@link Lookup#findStatic(Class, String, java.lang.invoke.MethodType)}.<p/>
         * Executes the {@link com.github.marschall.junitlambda.Block#value()} function of a given Block expression. If this
         * doesn't raise an exception, an AssertionError is thrown.
         *
         * @param message  the identifying message for the {@link AssertionError} ({@code null} okay)
         * @param block    the {@link com.github.marschall.junitlambda.Block} expression which should be executed
         * @param expected the expected Exception
         * @throws Exception the Exception which is thrown if the given Block expression works as intended
         */
        @SuppressWarnings("unused")
        static void callProtected(String message, Block block, Class<? extends Throwable> expected)
                throws Exception {
            block.value();
            failNotRaised(message, expected);
        }

        /**
         * Fails when an expected Exception was not raised while giving some information on what went wrong.
         *
         * @param message  the identifying message for the {@link AssertionError} ({@code null} okay)
         * @param expected the expected Exception
         */
        private static void failNotRaised(String message, Class<? extends Throwable> expected) {
            fail(formatNotRaised(message, expected, null));
        }

        /**
         * Called by {@link Lookup#findStatic(Class, String, java.lang.invoke.MethodType)}.<p/>
         * Completely ignores the given exception as it is expected to be thrown.
         *
         * @param exception the thrown exception. Is expected and therefore ignored.
         */
        @SuppressWarnings("unused")
        static void eatException(Throwable exception) {
            // expected
        }

        /**
         * Called when an exception other than the expected one was caught.
         *
         * @param message  the identifying message for the {@link AssertionError} ({@code null} okay)
         * @param expected the expected Exception
         * @param actual   the actually caught Exception
         */
        private static void exceptionCaught(String message, Class<? extends Throwable> expected, Throwable actual) {
            fail(formatNotRaised(message, expected, actual));
        }

        /**
         * Formats an error message when an expected Exception was not raised.
         *
         * @param message  the identifying message for the {@link AssertionError} ({@code null} okay)
         * @param expected the expected Exception
         * @param actual   the actually caught Exception
         * @return an error message with details about what went wrong
         */
        private static String formatNotRaised(String message, Class<? extends Throwable> expected, Throwable actual) {
            String formatted = "";
            if (message != null && !message.equals("")) {
                formatted = message + " ";
            }
            if (actual != null) {
                return String.format("%sshould have thrown: %s but did throw: %s", formatted, expected, actual.getClass());
            } else {
                return String.format("%sshould have thrown: %s but did not throw anything", formatted, expected);
            }
        }
    }

    /**
     * A small wrapper class for {@link java.util.function.Predicate} which adds a human readable description.
     *
     * @param <T> the static type accepted by the predicate
     */
    public static class NamedPredicate<T> implements Predicate<T> {

        final String description;
        final Predicate<T> predicate;

        /**
         * Constructor.
         *
         * @param description a short description of the given predicate. Can be a string representation, e.g.
         *                    <code>"n -> n == 1"</code> or a more verbose explanation, e.g. <code>"equals one"</code>.
         * @param predicate   the predicate which should be wrapped
         */
        public NamedPredicate(String description, Predicate<T> predicate) {
            this.predicate = predicate;
            this.description = description;
        }

        @Override
        public boolean test(T t) {
            return predicate.test(t);
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
