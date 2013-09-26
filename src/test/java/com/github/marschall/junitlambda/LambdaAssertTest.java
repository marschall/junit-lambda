package com.github.marschall.junitlambda;

import static com.github.marschall.junitlambda.LambdaAssert.shouldRaise;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.junit.Test;

public class LambdaAssertTest {

  @Test
  public void doesRaiseRuntimeExact() {
    shouldRaise(new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        throw new RuntimeException();
      }
    }, RuntimeException.class);
  }
  
  @Test
  public void doesRaiseRuntimeSubclass() {
    shouldRaise(new Callable<Void>() {
      
      @Override
      public Void call() throws Exception {
        throw new IndexOutOfBoundsException();
      }
    }, RuntimeException.class);
  }
  
  @Test
  public void doesRaiseCheckedExact() {
    shouldRaise(new Callable<Void>() {
      
      @Override
      public Void call() throws Exception {
        throw new IOException();
      }
    }, IOException.class);
  }
  
  @Test
  public void doesRaiseCheckedSubclass() {
    shouldRaise(new Callable<Void>() {
      
      @Override
      public Void call() throws Exception {
        throw new FileNotFoundException();
      }
    }, IOException.class);
  }

}
