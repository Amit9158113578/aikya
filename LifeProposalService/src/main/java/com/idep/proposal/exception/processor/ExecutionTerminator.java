package com.idep.proposal.exception.processor;

/**
 * 
 * @author yogesh.shisode
 * Custom Exception created to terminate process flow.
 * whenever exception occurs during proposal submission process we will throw this exception.
 * blueprint.xml contains configuration related to custom exception
 * 
 */
public class ExecutionTerminator extends Exception{
	private static final long serialVersionUID = 2188072814264815354L;
	String excpmsg;
	
	public ExecutionTerminator(){}
	public ExecutionTerminator(String response){
		excpmsg=response;
	}
	
	@Override
	public String toString(){ 
		return ("Exception MSG : " + excpmsg);
	}
}