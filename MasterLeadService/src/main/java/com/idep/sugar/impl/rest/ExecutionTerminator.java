package com.idep.sugar.impl.rest;



public class ExecutionTerminator extends Exception{
	

	private static final long serialVersionUID = -5603467918384824508L;

	public ExecutionTerminator()
	{
		
	}
	
	@Override
	   public String toString() { 
	      return ("Custom Exception at Lead Creation Mobile or Email Not found ");
	   }
}
