//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package com.hpe.simulap.protocol.sip.utils;

public class UnicityHeaderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5088467244343366667L;

	public UnicityHeaderException(String msg)
	{
		super(msg);
	}
	    
	public UnicityHeaderException(String msg, Throwable cause)
	{
		super(msg);
		initCause(cause);
	}

}
