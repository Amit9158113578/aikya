package com.idep.proposal.exception.processor;

public class ExecutionTerminator extends Exception{
	
	public static final long serialVersionUID = -4192267514550615282L;

	public ExecutionTerminator()
	{
		
	}
	
	@Override
	   public String toString() { 
	      return ("Custom Exception at Travel  ");
	   }
}