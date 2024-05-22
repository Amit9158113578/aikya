package com.idep.professions.exception;

public class ExecutionTerminator extends Exception{


	private static final long serialVersionUID = 4981806037235185216L;

	public ExecutionTerminator()
	{
		
	}
	
	@Override
	   public String toString() { 
	      return ("Custom Exception at PB request process  ");
	   }
	
	
}
