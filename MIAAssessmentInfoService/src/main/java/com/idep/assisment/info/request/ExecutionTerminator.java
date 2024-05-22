package com.idep.assisment.info.request;



public class ExecutionTerminator extends Exception{
	

	private static final long serialVersionUID = -5603467918384824508L;

	public ExecutionTerminator()
	{
		
	}
	
	@Override
	   public String toString() { 
	      return ("Custom Exception at Mia Assessment Service Question Id not having response....");
	   }
}
