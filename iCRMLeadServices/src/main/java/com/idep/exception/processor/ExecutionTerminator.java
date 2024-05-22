package com.idep.exception.processor;

public class ExecutionTerminator
  extends Exception
{
  private static final long serialVersionUID = 7471773386290473810L;
  
  public String toString()
  {
    return "Execution Terminated!";
  }
}
