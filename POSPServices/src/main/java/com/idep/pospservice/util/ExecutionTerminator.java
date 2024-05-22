package com.idep.pospservice.util;


public class ExecutionTerminator extends Exception
{
	private static final long serialVersionUID = 2188072814264815354L;
	String excpmsg;

	public String toString()
	{
		return "Exception MSG : " + this.excpmsg;
	}
}
