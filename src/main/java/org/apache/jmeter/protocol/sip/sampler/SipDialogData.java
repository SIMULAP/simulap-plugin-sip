//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package org.apache.jmeter.protocol.sip.sampler;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.ObjectInUseException;
import javax.sip.SipException;
import javax.sip.Transaction;
import javax.sip.TransactionUnavailableException;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.collections.map.HashedMap;
import org.apache.jmeter.protocol.sip.config.SipNodeContext;
import org.apache.jmeter.protocol.sip.config.SipTransactionData;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.javasimon.Split;

public class SipDialogData {

	private static final Logger _logger = LoggingManager.getLoggerForClass();

	String theCallId = null;
	Dialog theDialog = null;
	Request lastRequest = null;
	Response lastResponse = null;
	String previousTransaction = null;
	private Split responseTimeSplit = null ;
	private SipNodeContext theSipNodeContext = null;
	
	public SipDialogData(SipNodeContext sipNodeContext) {
		this.theSipNodeContext = sipNodeContext;
	}
	
	public String getPreviousTransaction() {
		return previousTransaction;
	}
	public void setPreviousTransaction(String previousTransaction) {
		this.previousTransaction = previousTransaction;
	}

	Map<String, SipTransactionData> theTransactions = null;
	Map<String, Dialog> theDialogs = new HashedMap();;
	
	public String getTheCallId() {
		return theCallId;
	}
	public void setTheCallId(String theCallId) {
		if (_logger.isDebugEnabled()) _logger.debug("setTheCallId : " + theCallId);
		this.theCallId = theCallId;
	}
	public Dialog getTheDialog() {
		return theDialog;
	}
	public void setTheDialog(Dialog theDialog) {
		if (theDialog == null) {
			if (_logger.isDebugEnabled()) _logger.debug("setTheDialog : Dialog is NULL ");
		}
		this.theDialog = theDialog;
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
		_logger.info("setLastResponse " +lastResponse.getStatusCode());// + ":" +  theCallId + ":" + theDialog.getDialogId() );
		this.lastResponse = lastResponse;
	}

	public SipTransactionData getATransaction(String transactionId) {
		if (_logger.isDebugEnabled()) _logger.debug("getATransaction " + transactionId);
		if (theTransactions == null) {
			theTransactions = new HashedMap();
		}
		SipTransactionData theTrans = theTransactions.get(transactionId);
		if (theTrans != null) {
			return theTrans;
		} else {
			theTrans = new SipTransactionData();
			theTransactions.put(transactionId, theTrans);
			return theTrans;
		}
	}
	
	public void setATransaction(String transactionId, SipTransactionData aTransaction) {
		if (_logger.isDebugEnabled()) _logger.debug("setATransaction " + transactionId);
		if (theTransactions == null) {
			theTransactions = new HashedMap();
		}
		theTransactions.put(transactionId, aTransaction);
	}	

	
	public Dialog getADialog(String transactionId) {
		if (_logger.isDebugEnabled()) _logger.debug("getADialog " + transactionId);
		Dialog theDial = theDialogs.get(transactionId);
		return theDial;
	}
	
	public void setADialog(String transactionId, Dialog aDialog) {
		if (_logger.isDebugEnabled()) _logger.debug("setADialog " + transactionId);
		theDialogs.put(transactionId, aDialog);
	}	

	
	public void clearSipDialogs() {
		if (_logger.isDebugEnabled()) _logger.debug("clearSipDialogs for callId = " + theCallId);
		for (SipTransactionData sipTransactionData : theTransactions.values()) {
			try {
				if (sipTransactionData != null && sipTransactionData.getTheTransaction() != null) {
					sipTransactionData.getTheTransaction().terminate();
				}
			} catch (ObjectInUseException e) {
				_logger.debug("clearSipDialogs ObjectInUseException ", e);
			}
		}
		if (theDialog != null) {
			endCall();
			try {
				if (_logger.isDebugEnabled()) _logger.debug("theDialog.terminateOnBye " + theDialog.getDialogId());
				theDialog.terminateOnBye(true);
			} catch (SipException e) {
				_logger.debug("theDialog.terminateOnBye ", e);
			}
			if (_logger.isDebugEnabled()) _logger.debug("theDialog.delete " + theDialog.getDialogId());
			theDialog.delete();
		}

	}
	
	private void endCall() {
		if (theDialog != null) {
			DialogState dialSt = theDialog.getState();
			if (DialogState.CONFIRMED.equals(dialSt)) {
				// Create the request
	            Request request;
				try {
					request = theDialog.createRequest("BYE");
	            // Create the client transaction.	            
	            ClientTransaction ct;
					ct = theSipNodeContext.getSipProvider().getNewClientTransaction(request);
	            // send the request out.
					theDialog.sendRequest(ct);
				}catch (TransactionUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SipException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			 
			}
				
		}
		
		
	}
	
    public Split getResponseTimeSplit()
    {
        return responseTimeSplit;
    }

    public void setResponseTimeSplit(Split responseTimeSplit)
    {
        this.responseTimeSplit = responseTimeSplit;
    }

}
