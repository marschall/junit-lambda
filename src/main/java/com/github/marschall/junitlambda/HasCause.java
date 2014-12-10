package com.github.marschall.junitlambda;

import static org.hamcrest.CoreMatchers.isA;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public final class HasCause<T extends Throwable> extends TypeSafeMatcher<T> {
  
  private final Matcher<? extends Throwable> causeMatcher;

  private HasCause(Matcher<? extends Throwable> causeMatcher) {
    this.causeMatcher = causeMatcher;
  }

  @Factory
  public static <T extends Throwable, C extends Throwable> Matcher<T> hasCause(Matcher<C> causeMatcher) {
    return new HasCause<>(causeMatcher);
  }

  @Factory
  public static <T extends Throwable> Matcher<T> hasCause(Class<? extends Throwable> cause) {
    return new HasCause<>(isA(cause));
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("has cause");
    description.appendDescriptionOf(this.causeMatcher);
  }

  @Override
  protected boolean matchesSafely(T item) {
    return this.causeMatcher.matches(item.getCause());
  }

}
