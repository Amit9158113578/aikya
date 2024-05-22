package com.idep.policyrenew.exception.processor;

public class ExecutionTerminator extends Exception{

	private static final long serialVersionUID = 2188072814264815354L;

	public ExecutionTerminator()
	{
		
	}
	
	@Override
	   public String toString() { 
	      return ("Custom Exception called");
	   }
}

