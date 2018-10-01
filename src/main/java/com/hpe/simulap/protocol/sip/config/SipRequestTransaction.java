//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package com.hpe.simulap.protocol.sip.config;

import gov.nist.javax.sip.stack.SIPServerTransactionImpl;

import java.util.Date;

import javax.sip.message.Request;

public class SipRequestTransaction {

	SIPServerTransactionImpl theTransaction = null;
	Request theRequest = null;
	Date creationDate;
	long creationTime;
	
	public SipRequestTransaction(SIPServerTransactionImpl theTransaction,
			Request theRequest) {
		super();
		this.theTransaction = theTransaction;
		this.theRequest = theRequest;
		this.creationDate = new Date();
		this.creationTime = System.currentTimeMillis();
	}

	public SIPServerTransactionImpl getTheTransaction() {
		return theTransaction;
	}
	
	public void setTheTransaction(SIPServerTransactionImpl theTransaction) {
		this.theTransaction = theTransaction;
	}
	
	public Request getTheRequest() {
		return theRequest;
	}
	
	public void setTheRequest(Request theRequest) {
		this.theRequest = theRequest;
	}
	
}
