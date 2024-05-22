package com.idep.summaryUtils;

public class ExecutionTerminator
  extends Exception
{
  public static final long serialVersionUID = -4192267512550615282L;
  String excpmsg;
  
  public String toString()
  {
    return "Exception MSG : " + this.excpmsg;
  }
}
