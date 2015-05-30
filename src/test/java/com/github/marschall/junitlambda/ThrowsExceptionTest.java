package com.github.marschall.junitlambda;

import static com.github.marschall.junitlambda.HasCause.hasCause;
import static com.github.marschall.junitlambda.HasMessage.hasMessage;
import static com.github.marschall.junitlambda.ThrowsException.throwsException;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.hamcrest.Matcher;
import org.junit.Test;

public class ThrowsExceptionTest {

  @Test
  public void positiveMatch() {
    assertThat(() -> { throw new NullPointerException();}, throwsException(NullPointerException.class));
  }

  @Test
  public void negativeMatch() {
    assertThat(() -> { throw new NullPointerException();}, not(throwsException(IllegalArgumentException.class)));
  }

  @Test
  public void positiveMessageMatch() {
    assertThat(() -> { throw new IllegalArgumentException("invalid");}, throwsException(IllegalArgumentException.class, hasMessage("invalid")));
  }

  @Test
  public void negativeMessageMatch() {
    assertThat(() -> { throw new IllegalArgumentException("invalid");}, not(throwsException(IllegalArgumentException.class, hasMessage("valid"))));
  }

  @Test
  public void positiveCauseMatch() {
    assertThat(this::throwExceptionWithCause, throwsException(RuntimeException.class, hasCause(IOException.class)));
  }

  @Test
  public void multipleMatchersPositive() {
    assertThat(this::throwExceptionWithCause, not(throwsException(RuntimeException.class, hasCause(IOException.class), hasMessage("io exception occurred"))));
  }

  @Test
  public void multipleMatchersNegative() {
    assertThat(this::throwExceptionWithCause, throwsException(RuntimeException.class, hasCause(IOException.class), hasMessage("invalid")));
  }

  @Test
  public void documentation() {
    assertThat(() -> Long.parseLong("foo"), throwsException(NumberFormatException.class));
    assertThat(() -> Long.parseLong("foo"), throwsException(NumberFormatException.class, hasMessage("For input string: \"foo\"")));
    Matcher<NumberFormatException> noCause = (Matcher) nullValue();
    assertThat(() -> Long.parseLong("foo"), throwsException(NumberFormatException.class, hasMessage("For input string: \"foo\""), hasCause(noCause)));
  }

  private void throwExceptionWithCause() {
    try {
      this.throwIoException();
    } catch (IOException e) {
      throw new RuntimeException("io exception occurred", e);
    }
  }

  private void throwIoException() throws IOException {
    throw new IOException("space exceeded");
  }

}
