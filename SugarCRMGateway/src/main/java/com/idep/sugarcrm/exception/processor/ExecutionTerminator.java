package com.idep.sugarcrm.exception.processor;
	
	public class ExecutionTerminator extends Exception{
		
		
		private static final long serialVersionUID = 7471773386290473810L;
		

		public ExecutionTerminator()
		{
			
		}
		
		@Override
		   public String toString() { 
		      return ("Execution Terminated!");
		   }
	}


