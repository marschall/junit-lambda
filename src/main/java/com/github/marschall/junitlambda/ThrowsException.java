package com.github.marschall.junitlambda;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public final class ThrowsException<T extends Throwable> extends TypeSafeMatcher<Block> {

  private static final MethodHandle CATCH_EXCEPTION;
  private static final MethodHandle CALL_PROTECTED;

  static {
    try {
      Lookup lookup = MethodHandles.lookup();
      Class<?> refc = ThrowsException.class;
      Class<?> specialCaller = refc;
      CATCH_EXCEPTION = lookup.findSpecial(refc, "catchException", MethodType.methodType(Boolean.TYPE, Throwable.class), specialCaller);
      CALL_PROTECTED = lookup.findSpecial(refc, "callProtected", MethodType.methodType(Boolean.TYPE, Block.class), specialCaller);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("could not initialize LambdaAssert", e);
    }
  }

  private final Matcher<T> exceptionMatcher;
  private final Class<T> expected;

  private ThrowsException(Class<T> expected, Matcher<T> exceptionMatcher) {
    this.expected = expected;
    this.exceptionMatcher = exceptionMatcher;
  }

  @Factory
  public static <T extends Throwable> Matcher<Block> throwsException(Class<T> expected) {
    return new ThrowsException<>(expected, null);
  }
  
  @Factory
  public static <T extends Throwable> TypeSafeMatcher<Block> throwsException(Class<T> expected, Matcher<T> exceptionMatcher) {
    return new ThrowsException<>(expected, exceptionMatcher);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("throws exception");
    description.appendValue(this.expected);
    if (this.exceptionMatcher != null) {
      description.appendText("matching");
      description.appendDescriptionOf(exceptionMatcher);
    }
  }

  @Override
  protected boolean matchesSafely(Block block) {
    MethodHandle call = MethodHandles.insertArguments(CALL_PROTECTED, 0, this, block);
    MethodHandle catchException = MethodHandles.insertArguments(CATCH_EXCEPTION, 0, this);
    MethodHandle verification = MethodHandles.catchException(call, expected, catchException);
    try {
      return (boolean) verification.invokeWithArguments();
    } catch (Throwable e) {
      return false;
    }
  }

  private boolean callProtected(Block block) throws Exception {
    block.value();
    return false;
  }

  private boolean catchException(Throwable exception) {
    if (this.exceptionMatcher != null) {
      return this.exceptionMatcher.matches(exception);
    } else {
      return true;
    }
  }

}
