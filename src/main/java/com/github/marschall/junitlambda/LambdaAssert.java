package com.github.marschall.junitlambda;

import static org.junit.Assert.fail;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.concurrent.Callable;

public final class LambdaAssert {

  private static final MethodHandle EAT_EXCEPTION;
  private static final MethodHandle CALLABLE_CALL;
  private static final MethodHandle FAIL_NOT_RAISED;

  static {
    try {
      Lookup lookup = MethodHandles.lookup();
      EAT_EXCEPTION = lookup.findStatic(LambdaAssert.class, "eatException", MethodType.methodType(Object.class, Exception.class));
      FAIL_NOT_RAISED = lookup.findStatic(LambdaAssert.class, "failNotRaised", MethodType.methodType(Void.TYPE, String.class, Class.class));

      Lookup publicLookup = MethodHandles.publicLookup();
      CALLABLE_CALL = publicLookup.findVirtual(Callable.class, "call", MethodType.methodType(Object.class));
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("could not initialize LambdaAssert", e);
    }
  }

  private LambdaAssert() {
    throw new AssertionError("not instantiable");
  }

  public static void shouldRaise(Callable<?> function, Class<? extends Exception> expected) {
    MethodHandle call = CALLABLE_CALL.bindTo(function);
    //    MethodHandle failNotRaised = MethodHandles.insertArguments(FAIL_NOT_RAISED, 0, null, expected);
    MethodHandle verification = MethodHandles.catchException(call, expected, EAT_EXCEPTION);
    try {
      verification.invokeWithArguments();
    } catch (Throwable e) {
      throw new AssertionError("unexpected exception: " + e.getClass() + " expected: " + expected, e);
    }
  }

  private static void failNotRaised(String message, Class<? extends Exception> expected) {
    fail(formatNotRaised(message, expected, null));
  }

  private static Object eatException(Exception exception) {
    return null;
  }

  private static void exceptionCaught(String message, Class<? extends Exception> expected, Class<? extends Exception> actual) {
    fail(formatNotRaised(message, expected, actual));
  }

  private static String formatNotRaised(String message, Class<? extends Exception> expected, Class<? extends Exception> actual) {
    String formatted = "";
    if (message != null && !message.equals("")) {
      formatted = message + " ";
    }
    if (actual != null) {
      return formatted + "should have thrown: " + expected + " but did throw: " + actual;
    } else {
      return formatted + "should have thrown: " + expected + " but did not throw anything";
    }
  }

}
