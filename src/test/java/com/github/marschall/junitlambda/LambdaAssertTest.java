package com.github.marschall.junitlambda;

import static com.github.marschall.junitlambda.LambdaAssert.shouldRaise;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class LambdaAssertTest {
  
  @Test(expected = AssertionError.class)
  public void doesNotRaiseRuntime() {
    shouldRaise(new Block() {
      
      public void value() {
      }
    }, RuntimeException.class);
  }
  
  @Test(expected = AssertionError.class)
  public void doesRaiseWrongRuntime() {
    shouldRaise(new Block() {
      
      @Override
      public void value() throws Exception {
        throw new NullPointerException();
      }
    }, RuntimeException.class);
  }
  
  @Test(expected = AssertionError.class)
  public void doesRaiseCheckedInsteadOfRuntime() {
    shouldRaise(new Block() {
      
      public void value() throws IOException {
        throw new IOException();
      }
    }, RuntimeException.class);
  }

  @Test
  public void doesRaiseRuntimeExact() {
    shouldRaise(new Block() {

      public void value() {
        throw new RuntimeException();
      }
    }, RuntimeException.class);
  }
  
  @Test
  public void doesRaiseRuntimeSubclass() {
    shouldRaise(new Block() {
      
      public void value() {
        throw new IndexOutOfBoundsException();
      }
    }, RuntimeException.class);
  }
  
  @Test
  public void doesRaiseCheckedExact() {
    shouldRaise(new Block() {
      
      public void value() throws IOException {
        throw new IOException();
      }
    }, IOException.class);
  }
  
  @Test
  public void doesRaiseCheckedSubclass() {
    shouldRaise(new Block() {
      
      public void value() throws IOException {
        throw new FileNotFoundException();
      }
    }, IOException.class);
  }
  
  @Test
  public void assertionErrorRaised() {
    shouldRaise(new Block() {
      
      public void value() {
        throw new AssertionError();
      }
    }, AssertionError.class);
  }
  
  @Test(expected = AssertionError.class)
  public void assertionErrorNotRaised() {
    shouldRaise(new Block() {
      
      public void value() {
      }
    }, AssertionError.class);
  }

}
