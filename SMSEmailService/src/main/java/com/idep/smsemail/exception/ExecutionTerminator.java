package com.idep.smsemail.exception;

public class ExecutionTerminator extends  Exception{
	
	

	/**
	 * @author pravin.jakhi
	 * Custom Exception created to terminate process flow.
 * whenever exception occurs during proposal submission process we will throw this exception.
 * blueprint.xml contains configuration related to custom exception
	 * 
	 */
	private static final long serialVersionUID = -3736775114128780152L;

	public ExecutionTerminator()
	{
		
	}
	
	@Override
	   public String toString() { 
	      return ("SMS not sent , Process terminated  : ");
	   }
}
