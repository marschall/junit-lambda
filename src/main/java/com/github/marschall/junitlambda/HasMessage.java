package com.github.marschall.junitlambda;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import static org.hamcrest.CoreMatchers.equalTo;

public final class HasMessage<T extends Throwable> extends TypeSafeMatcher<T> {

  private final Matcher<String> messageMatcher;

  private HasMessage(Matcher<String> messageMatcher) {
    this.messageMatcher = messageMatcher;
  }

  @Factory
  public static <T extends Throwable> Matcher<T> hasMessage(Matcher<String> messageMatcher) {
    return new HasMessage<>(messageMatcher);
  }

  @Factory
  public static <T extends Throwable> Matcher<T> hasMessage(String message) {
    return new HasMessage<>(equalTo(message));
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("has message");
    description.appendDescriptionOf(this.messageMatcher);
  }

  @Override
  protected boolean matchesSafely(T throwable) {
    return this.messageMatcher.matches(throwable.getMessage());
  }

}
