//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package org.apache.jmeter.protocol.sip.config;

import gov.nist.javax.sip.stack.SIPClientTransactionImpl;

import java.util.Date;

import javax.sip.message.Response;

public class SipResponseTransaction {

	SIPClientTransactionImpl theTransaction = null;
	Response theResponse = null;
	Date creationDate;
	long creationTime;

	public SipResponseTransaction(SIPClientTransactionImpl theTransaction,
			Response theResponse) {
		super();
		this.theTransaction = theTransaction;
		this.theResponse = theResponse;
		this.creationDate = new Date();
		this.creationTime = System.currentTimeMillis();
	}

	public SIPClientTransactionImpl getTheTransaction() {
		return theTransaction;
	}
	
	public void setTheTransaction(SIPClientTransactionImpl theTransaction) {
		this.theTransaction = theTransaction;
	}
	
	public Response getTheResponse() {
		return theResponse;
	}
	
	public void setTheResponse(Response theResponse) {
		this.theResponse = theResponse;
	}
	
}
