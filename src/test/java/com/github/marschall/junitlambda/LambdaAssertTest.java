package com.github.marschall.junitlambda;

import static com.github.marschall.junitlambda.LambdaAssert.assertRaises;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class LambdaAssertTest {
  
  @Test(expected = AssertionError.class)
  public void doesNotRaiseRuntime() {
    assertRaises(new Block() {
      
      public void value() {
      }
    }, RuntimeException.class);
  }
  
  @Test(expected = AssertionError.class)
  public void doesRaiseWrongRuntime() {
    assertRaises(new Block() {
      
      @Override
      public void value() throws Exception {
        throw new NullPointerException();
      }
    }, RuntimeException.class);
  }
  
  @Test(expected = AssertionError.class)
  public void doesRaiseCheckedInsteadOfRuntime() {
    assertRaises(new Block() {
      
      public void value() throws IOException {
        throw new IOException();
      }
    }, RuntimeException.class);
  }

  @Test
  public void doesRaiseRuntimeExact() {
    assertRaises(new Block() {

      public void value() {
        throw new RuntimeException();
      }
    }, RuntimeException.class);
  }
  
  @Test
  public void doesRaiseRuntimeSubclass() {
    assertRaises(new Block() {
      
      public void value() {
        throw new IndexOutOfBoundsException();
      }
    }, RuntimeException.class);
  }
  
  @Test
  public void doesRaiseCheckedExact() {
    assertRaises(new Block() {
      
      public void value() throws IOException {
        throw new IOException();
      }
    }, IOException.class);
  }
  
  @Test
  public void doesRaiseCheckedSubclass() {
    assertRaises(new Block() {
      
      public void value() throws IOException {
        throw new FileNotFoundException();
      }
    }, IOException.class);
  }
  
  @Test
  public void assertionErrorRaised() {
    assertRaises(new Block() {
      
      public void value() {
        throw new AssertionError();
      }
    }, AssertionError.class);
  }
  
  @Test(expected = AssertionError.class)
  public void assertionErrorNotRaised() {
    assertRaises(new Block() {
      
      public void value() {
      }
    }, AssertionError.class);
  }

}
