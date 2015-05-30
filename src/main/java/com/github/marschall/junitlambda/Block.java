package com.github.marschall.junitlambda;

/**
 * A piece of code to be executed.
 */
public interface Block {

  /**
   * Evaluate the code.
   *
   * @throws Exception if the code throws an exception
   */
  public void value() throws Exception;

}
