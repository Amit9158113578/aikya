package com.idep.garage.util;

/**
 * 
 * @author sandeep.jadhav
 * Custom Exception created to terminate process flow.
 * whenever exception occurs during proposal submission process we will throw this exception.
 * blueprint.xml contains configuration related to custom exception
 * 
 */
public class ExecutionTerminator extends Exception{
	
	private static final long serialVersionUID = 2188072814264815354L;
	String excpmsg;

	public ExecutionTerminator()
	{
		
	}
	
	@Override
	   public String toString() { 
	      return ("Exception MSG : "+excpmsg);
	   }
}
