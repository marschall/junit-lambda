package com.github.marschall.junitlambda;

import java.util.concurrent.Callable;

public final class LambdaAssert {

  private LambdaAssert() {
    throw new AssertionError("not instantiable");
  }
  
  public static void shouldRaise(Callable<?> function, Class<? extends Throwable> exception) {
    
  }

}
