package com.github.marschall.junitlambda;

import static org.hamcrest.CoreMatchers.instanceOf;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Tests if the argument is an exception that has a cause.
 */
public final class HasCause<T extends Throwable> extends TypeSafeMatcher<T> {

  private final Matcher<? extends Throwable> causeMatcher;

  private HasCause(Matcher<? extends Throwable> causeMatcher) {
    this.causeMatcher = causeMatcher;
  }

  /**
   * Creates a matcher that matches if the examined {@link Throwable} has a cause that
   * is matched by the specified <code>causeMatcher</code>.
   *
   * <p>For example:
   * <pre><code>assertThat(anException, hasCause(instanceOf(IOException.class)));</code></pre>
   *
   * @param causeMatcher
   *     the matcher to apply to the examined exception cause
   */
  @Factory
  public static <T extends Throwable, C extends Throwable> Matcher<T> hasCause(Matcher<C> causeMatcher) {
    return new HasCause<>(causeMatcher);
  }

  /**
   * Creates a matcher that matches if the examined {@link Throwable} has a cause that
   * is an instance of the given <code>causeClass</code>.
   *
   * <p>For example:
   * <pre><code>assertThat(anException, hasCause(IOException.class)));</code></pre>
   *
   * @param causeClass
   *     the class the exception cause has to be an instance of
   */
  @Factory
  public static <T extends Throwable> Matcher<T> hasCause(Class<? extends Throwable> causeClass) {
    return new HasCause<>(instanceOf(causeClass));
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
