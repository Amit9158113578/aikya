 package com.idep.proposal.exception.processor;
 
 public class ExecutionTerminator extends Exception {
   private static final long serialVersionUID = 2188072814264815354L;
   
   public String execution(ExtendedJsonNode response) throws Exception {
     try {
       return response.toString();
     } catch (Exception e) {
       return response.toString();
     } 
   }
   
   public String toString() {
     return "Custom Exception Called:";
   }
 }


