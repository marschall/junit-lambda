package com.github.marschall.junitlambda;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import static org.junit.Assert.fail;

/**
 * TODO AC: JavaDoc
 *
 * @author Philippe Marschall
 * @author Alasdair Collinson
 */
public final class LambdaAssert {

    private static final MethodHandle EAT_EXCEPTION;
    private static final MethodHandle CALL_PROTECTED;

    static {
        try {
            Lookup lookup = MethodHandles.lookup();
            EAT_EXCEPTION = lookup.findStatic(LambdaAssert.class, "eatException", MethodType.methodType(Void.TYPE, Throwable.class));
            CALL_PROTECTED = lookup.findStatic(LambdaAssert.class, "callProtected", MethodType.methodType(Void.TYPE, String.class, Block.class, Class.class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("could not initialize LambdaAssert", e);
        }
    }

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
     * @param exception
     */
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

}
