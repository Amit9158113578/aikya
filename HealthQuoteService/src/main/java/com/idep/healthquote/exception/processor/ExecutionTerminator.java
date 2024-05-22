package com.idep.healthquote.exception.processor;

/**
 * 
 * @author sandeep.jadhav
 * Custom Exception created to terminate process flow.
 * whenever exception occurs during proposal submission process we will throw this exception.
 * blueprint.xml contains configuration related to custom exception
 * 
 */
public class ExecutionTerminator extends Exception{
	
	private static final long serialVersionUID = 4699053295142375032L;
	String excpmsg;

	public ExecutionTerminator()
	{
		
	}
	
	@Override
	   public String toString() { 
	      return ("Exception MSG : "+excpmsg);
	   }
}
