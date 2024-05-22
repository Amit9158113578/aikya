package com.idep.lifequote.exception.processor;

/**
 * 
 * @author yogesh.shisode
 * Custom Exception created to terminate process flow.
 * whenever exception occurs during proposal submission process we will throw this exception.
 * blueprint.xml contains configuration related to custom exception
 */
public class ExecutionTerminator extends Exception{
	public static final long serialVersionUID = -4192267514550615282L;
	String excpmsg;

	public ExecutionTerminator(){
	}

	@Override
	public String toString(){ 
		return ("Exception MSG : " + excpmsg);
	}
}
