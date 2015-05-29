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
  private final Matcher<T>[] moreMatchers;

  private ThrowsException(Class<T> expected, Matcher<T> exceptionMatcher, Matcher<T>[] moreMatchers) {
    this.expected = expected;
    this.exceptionMatcher = exceptionMatcher;
    this.moreMatchers = moreMatchers;
  }

  @Factory
  public static <T extends Throwable> Matcher<Block> throwsException(Class<T> expected) {
    return new ThrowsException<>(expected, null, null);
  }

  @Factory
  public static <T extends Throwable> TypeSafeMatcher<Block> throwsException(Class<T> expected, Matcher<T> exceptionMatcher) {
    return new ThrowsException<>(expected, exceptionMatcher, null);
  }

  @SafeVarargs
  @Factory
  public static <T extends Throwable> TypeSafeMatcher<Block> throwsException(Class<T> expected, Matcher<T> exceptionMatcher, Matcher<T>... moreMatchers) {
    return new ThrowsException<>(expected, exceptionMatcher, moreMatchers);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("throws exception ");
    description.appendValue(this.expected);
    if (this.exceptionMatcher != null) {
      description.appendText(" matching ");
      description.appendDescriptionOf(exceptionMatcher);
      if (this.moreMatchers != null && this.moreMatchers.length > 0) {
        for (Matcher<T> matcher : moreMatchers) {
          description.appendText(" and ");
          description.appendDescriptionOf(matcher);
        }

      }
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
      if (!this.exceptionMatcher.matches(exception)) {
        return false;
      }
      if (this.moreMatchers != null && this.moreMatchers.length > 0) {
        for (Matcher<T> matcher : this.moreMatchers) {
          if (matcher.matches(exception)) {
            return false;
          }
        }
      }
      return true;
    } else {
      return true;
    }
  }

}
