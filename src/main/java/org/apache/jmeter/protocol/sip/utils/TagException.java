//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package org.apache.jmeter.protocol.sip.utils;

public class TagException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1584163666686979889L;

	public TagException(String msg)
    {
        super(msg);
    }
    
    public TagException(String msg, Throwable cause)
    {
       super(msg);
       initCause(cause);
    }

}
