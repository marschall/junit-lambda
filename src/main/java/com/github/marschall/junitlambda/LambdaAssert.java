package com.github.marschall.junitlambda;

import static org.junit.Assert.fail;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

public final class LambdaAssert {

  private static final MethodHandle EAT_EXCEPTION;
  private static final MethodHandle CALL_PROTECTED;

  static {
    try {
      Lookup lookup = MethodHandles.lookup();
      EAT_EXCEPTION = lookup.findStatic(LambdaAssert.class, "eatException", MethodType.methodType(Void.TYPE, Throwable.class));
      CALL_PROTECTED = lookup.findStatic(LambdaAssert.class, "callProtected", MethodType.methodType(Void.TYPE, Block.class, Class.class));
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("could not initialize LambdaAssert", e);
    }
  }

  private LambdaAssert() {
    throw new AssertionError("not instantiable");
  }

  public static void shouldRaise(Block block, Class<? extends Throwable> expected) {
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
    
    MethodHandle call = MethodHandles.insertArguments(CALL_PROTECTED, 0, block, expected);
    MethodHandle verification = MethodHandles.catchException(call, expected, EAT_EXCEPTION);
    try {
      verification.invokeWithArguments();
    } catch (Throwable e) {
      throw new AssertionError("unexpected exception: " + e.getClass() + " expected: " + expected, e);
    }
  }
  
  private static void callProtected(Block block, Class<? extends Throwable> expected) throws Exception {
    block.value();
    failNotRaised(null, expected);
  }

  private static void failNotRaised(String message, Class<? extends Throwable> expected) {
    fail(formatNotRaised(message, expected, null));
  }

  private static void eatException(Throwable exception) {
    // expected
  }

  private static void exceptionCaught(String message, Class<? extends Throwable> expected, Throwable actual) {
    fail(formatNotRaised(message, expected, actual));
  }

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
