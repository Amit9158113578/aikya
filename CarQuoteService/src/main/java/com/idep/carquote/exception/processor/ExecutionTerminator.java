 package com.idep.carquote.exception.processor;
 
 public class ExecutionTerminator extends Exception {
   public static final long serialVersionUID = -4192267514550615282L;
   String excpmsg;
   
   public String toString() {
     return "Exception MSG : " + this.excpmsg;
   }
 }


