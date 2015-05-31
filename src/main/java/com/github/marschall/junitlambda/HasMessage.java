package com.github.marschall.junitlambda;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Tests if the argument is an exception that has a cause.
 */
public final class HasMessage<T extends Throwable> extends TypeSafeMatcher<T> {

  private final Matcher<String> messageMatcher;

  private HasMessage(Matcher<String> messageMatcher) {
    this.messageMatcher = messageMatcher;
  }

  /**
   * Creates a matcher that matches if the examined {@link Throwable} has a message that
   * is matched by the specified <code>messageMatcher</code>.
   *
   * <p>For example:
   * <pre><code>assertThat(anException, hasCause(startsWith("IOException occurred")));</code></pre>
   *
   * @param messageMatcher
   *     the matcher to apply to the examined exception message
   * @param <T> the expected throwable type
   * @return the matcher
   */
  @Factory
  public static <T extends Throwable> Matcher<T> hasMessage(Matcher<String> messageMatcher) {
    return new HasMessage<>(messageMatcher);
  }

  /**
   * Creates a matcher that matches if the examined {@link Throwable} has a message that
   * is equal to the given <code>message</code>.
   *
   * <p>For example:
   * <pre><code>assertThat(anException, hasMessage("file not writable")));</code></pre>
   *
   * @param message
   *     the message that has to be equal to the exception message
   * @param <T> the expected throwable type
   * @return the matcher
   */
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
