package com.github.marschall.junitlambda;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static com.github.marschall.junitlambda.LambdaAssert.assertRaises;

/**
 * TODO AC: JavaDoc
 *
 * @author Philippe Marschall
 * @author Alasdair Collinson
 */
public class LambdaAssertTest {
  
  @Test(expected = AssertionError.class)
  public void doesNotRaiseRuntime() {
    assertRaises(() -> { }, RuntimeException.class);
  }
  
  @Test(expected = AssertionError.class)
  public void doesRaiseWrongRuntime() {
    assertRaises(() -> { throw new NullPointerException();}, IllegalArgumentException.class);
  }
  
  @Test(expected = AssertionError.class)
  public void doesRaiseCheckedInsteadOfRuntime() {
    assertRaises(() -> { throw new IOException();}, RuntimeException.class);
  }

  @Test
  public void doesRaiseRuntimeExact() {
    assertRaises(() -> { throw new RuntimeException(); }, RuntimeException.class);
  }
  
  @Test
  public void doesRaiseRuntimeSubclass() {
    assertRaises(() -> { throw new IndexOutOfBoundsException();}, RuntimeException.class);
  }
  
  @Test
  public void doesRaiseCheckedExact() {
    assertRaises(() -> { throw new IOException();}, IOException.class);
  }
  
  @Test
  public void doesRaiseCheckedSubclass() {
    assertRaises(() -> { throw new FileNotFoundException();}, IOException.class);
  }
  
  @Test
  public void assertionErrorRaised() {
    assertRaises(() -> { throw new AssertionError();}, AssertionError.class);
  }
  
  @Test(expected = AssertionError.class)
  public void assertionErrorNotRaised() {
    assertRaises(() -> { }, AssertionError.class);
  }

}
