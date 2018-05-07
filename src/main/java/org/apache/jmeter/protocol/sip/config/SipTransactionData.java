//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package org.apache.jmeter.protocol.sip.config;

import javax.sip.Transaction;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class SipTransactionData {

	Transaction theTransaction = null;
	Request lastRequest = null;
	Response lastResponse = null;

	public Transaction getTheTransaction() {
		return theTransaction;
	}
	public void setTheTransaction(Transaction theTransaction) {
		this.theTransaction = theTransaction;
	}
	public Request getLastRequest() {
		return lastRequest;
	}
	public void setLastRequest(Request lastRequest) {
		this.lastRequest = lastRequest;
	}
	public Response getLastResponse() {
		return lastResponse;
	}
	public void setLastResponse(Response lastResponse) {
		this.lastResponse = lastResponse;
	}

	
}
