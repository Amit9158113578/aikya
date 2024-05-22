package com.idep.healthquote.exception.processor;

/**
 * 
 * @author Pravin.Jakhi
 * Custom Exception created to terminate process flow.
 * whenever exception occurs during proposal submission process we will throw this exception.
 * blueprint.xml contains configuration related to custom exception
 * 
 */
public class RequestValidationTerminator extends Exception{
	
	private static final long serialVersionUID = -3007032375335229689L;
	String excpmsg;

	public RequestValidationTerminator()
	{
		
	}
	
	@Override
	   public String toString() { 
	      return ("Exception MSG : "+excpmsg);
	   }
}
