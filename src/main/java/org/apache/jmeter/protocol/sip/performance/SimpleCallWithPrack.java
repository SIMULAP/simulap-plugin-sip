package org.apache.jmeter.protocol.sip.performance;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipListener;
import javax.sip.TimeoutEvent;
import javax.sip.Transaction;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.RSeqHeader;
import javax.sip.header.RouteHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.jmeter.protocol.sip.config.SipNodeContext;
import org.apache.jmeter.protocol.sip.config.SipResponseTransaction;
import org.apache.jmeter.protocol.sip.utils.SipCounters;
import org.apache.jmeter.protocol.sip.utils.SipString;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.javasimon.SimonManager;

import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.stack.SIPClientTransactionImpl;
import gov.nist.javax.sip.stack.SIPServerTransactionImpl;

public class SimpleCallWithPrack implements SipListener {
	
	private SipNodeContext sipNodeCtx;
	private static final Logger _logger = LoggingManager.getLoggerForClass();

	
	public SimpleCallWithPrack (SipNodeContext sipNodeC) {
		this.sipNodeCtx = sipNodeC;
	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processIOException(IOExceptionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processRequest(RequestEvent arg0) {
		//printQueueSize();
		if (_logger.isDebugEnabled()) _logger.debug("processRequestPerformance: " + arg0.getRequest().getMethod() + " identified by " + this.sipNodeCtx.getSipNode().getIdentificationHeader() + " = " + arg0.getRequest().getHeader(this.sipNodeCtx.getSipNode().getIdentificationHeader()));

		ServerTransaction st = null;
		//		ToHeader toH = (ToHeader) arg0.getRequest().getHeader(this.sipNode.getIdentificationHeader());//"To");
		//	String id = SipString.cleanString(toH.getAddress().toString());
		String id = sipNodeCtx.getIdHeader(arg0.getRequest());
		try {
			if (arg0.getServerTransaction() == null) {
				if (_logger.isDebugEnabled()) _logger.debug("processRequestPerformance: no server transaction for id " + id);
				st = sipNodeCtx.getSipProvider().getNewServerTransaction(arg0.getRequest());
			} else {
				st = arg0.getServerTransaction();
			}
			//			ArrayBlockingQueue<SipRequestTransaction> reqQueue = checkRequestQueue(id + "_" + arg0.getRequest().getMethod());


			//			SipRequestTransaction reqTrans = new SipRequestTransaction((SIPServerTransactionImpl)st, arg0.getRequest());

			//			reqQueue.add(reqTrans);

			//			msgRequestsQueues.put(id, reqQueue);

			Request rcvReq = arg0.getRequest();			

			if (Request.ACK.equals(rcvReq.getMethod())) {
				if (_logger.isDebugEnabled()) _logger.debug("processRequestPerformance: ACK recevied. Do nothing");
			} else if (Request.INVITE.equals(rcvReq.getMethod())) {
				// Create the response
				//Response theResponse = this.sipNodeCtx.getMessageFactory().createResponse(183 , arg0.getRequest());
				
				SIPServerTransactionImpl aReq = (SIPServerTransactionImpl) arg0.getServerTransaction();
				if (aReq == null) {
					aReq = (SIPServerTransactionImpl) st;
				}
				
				Dialog newDialog = sipNodeCtx.getSipProvider().getNewDialog(aReq);
                Response theResponse = newDialog.createReliableProvisionalResponse(183);//theSessionData.getTheDialog().createReliableProvisionalResponse(respCode );
                
				this.sipNodeCtx.getDialogListPerf().putIfAbsent("INVITE_"+rcvReq.getHeader("Call-ID"), rcvReq);

				// Add Contact header (mandatory)
				Address address = this.sipNodeCtx.getAddressFactory().createAddress(SipString.extractAddress("sip:" +sipNodeCtx.getSipNode().getLocalIP()+":"+sipNodeCtx.getSipNode().getLocalPort()));
				_logger.error(sipNodeCtx.getSipNode().getSipNodeName() + "Set Contact header with : " + "sip:" +sipNodeCtx.getSipNode().getLocalIP()+":"+sipNodeCtx.getSipNode().getLocalPort());				
				// "${b_name}" <sip:${b_username}_${idkey}@${b_ip}:${b_port}>
				ContactHeader autoContact = this.sipNodeCtx.getHeaderFactory().createContactHeader(address);
				theResponse.removeHeader("Contact");
				theResponse.setHeader(autoContact);

				RSeqHeader rseqHeader = null;
				try {
					rseqHeader = this.sipNodeCtx.getHeaderFactory().createRSeqHeader(1);
					theResponse.removeHeader("RSeq");
					theResponse.setHeader(rseqHeader);
				} catch (NumberFormatException nfe) {
					_logger.debug("Rseq header tag not set. Input badly formated ");
				}
				
				

				theResponse.setHeader(this.sipNodeCtx.getHeaderFactory().createHeader("Require", "100rel"));


				st.sendResponse(theResponse);
				/*				if (Request.BYE.equals(rcvReq.getMethod())) {
					clearQueue(id + "_" + arg0.getRequest().getMethod());
				}
				 */

				//Dialog newDialog = this.sipNodeCtx.getSipProvider().getNewDialog(st);
				newDialog.setApplicationData(arg0.getRequest());
				st.getDialog().setApplicationData(arg0.getRequest());

				SimonManager.getCounter(
						SipCounters.SIP_NODE_OUTBOUND_ANSWER.toString() + SipCounters.SUCCESS_SUFFIX.toString() + "."
								+ rcvReq.getMethod()
								+ "." + sipNodeCtx.getSipNode().getSipNodeName().replace(".", "_")
						).increase();
			} else if (Request.PRACK.equals(rcvReq.getMethod())) {
				// Create the response to PRACK
				Response theResponse = this.sipNodeCtx.getMessageFactory().createResponse(200 , arg0.getRequest());

				// Add Contact header (mandatory)
				Address address = this.sipNodeCtx.getAddressFactory().createAddress(SipString.extractAddress("sip:" +sipNodeCtx.getSipNode().getLocalIP()+":"+sipNodeCtx.getSipNode().getLocalPort()));
				ContactHeader autoContact = this.sipNodeCtx.getHeaderFactory().createContactHeader(address);
				theResponse.setHeader(autoContact);


				st.sendResponse(theResponse);
				/*				if (Request.BYE.equals(rcvReq.getMethod())) {
					clearQueue(id + "_" + arg0.getRequest().getMethod());
				}
				 */
				SimonManager.getCounter(
						SipCounters.SIP_NODE_OUTBOUND_ANSWER.toString() + SipCounters.SUCCESS_SUFFIX.toString() + "."
								+ rcvReq.getMethod()
								+ "." + sipNodeCtx.getSipNode().getSipNodeName().replace(".", "_")
						).increase();
				// Create the response to INVITE

				Request invReq = this.sipNodeCtx.getDialogListPerf().remove("INVITE_"+rcvReq.getHeader("Call-ID"));

				Response theResponseInvite = this.sipNodeCtx.getMessageFactory().createResponse(200 , invReq);
				//				Response theResponseInvite = messageFactory.createResponse(200 , (Request) arg0.getServerTransaction().getApplicationData());

				//				List<Via> vialist = new ArrayList<Via>();
				//				vialist.add((Via)rcvReq.getHeader("Via"));
				//				_logger.error("create 200 for Invite "+ ":" + (CallIdHeader)rcvReq.getHeader("Call-ID") + ":" + (CSeqHeader)headerFactory.createCSeqHeader(1, "INVITE") + ":" + (FromHeader)rcvReq.getHeader("From")+ ":" + (ToHeader)rcvReq.getHeader("To")+ ":" + vialist.size() + ":" + (MaxForwardsHeader)rcvReq.getHeader("Max-Forwards"));
				//				Response theResponseInvite = messageFactory.createResponse(200, (CallIdHeader)rcvReq.getHeader("Call-ID"), (CSeqHeader)headerFactory.createCSeqHeader(1, "INVITE"), (FromHeader)rcvReq.getHeader("From"), (ToHeader)rcvReq.getHeader("To"), vialist, (MaxForwardsHeader)rcvReq.getHeader("Max-Forwards"));
				//Response theResponseInvite = messageFactory.createResponse("200 OK");
				//				theResponseInvite.setReasonPhrase("OK");

				// Add Contact header (mandatory)
				Address address2 = this.sipNodeCtx.getAddressFactory().createAddress(SipString.extractAddress("sip:" +sipNodeCtx.getSipNode().getLocalIP()+":"+sipNodeCtx.getSipNode().getLocalPort()));
				ContactHeader autoContact2 = this.sipNodeCtx.getHeaderFactory().createContactHeader(address2);
				theResponseInvite.setHeader(autoContact2);


				this.sipNodeCtx.getSipProvider().sendResponse(theResponseInvite);

			} else {
				// Create the response
				Response theResponse = this.sipNodeCtx.getMessageFactory().createResponse(200 , arg0.getRequest());

				// Add Contact header (mandatory)
				Address address = this.sipNodeCtx.getAddressFactory().createAddress(SipString.extractAddress("sip:" +sipNodeCtx.getSipNode().getLocalIP()+":"+sipNodeCtx.getSipNode().getLocalPort()));
				ContactHeader autoContact = this.sipNodeCtx.getHeaderFactory().createContactHeader(address);
				theResponse.setHeader(autoContact);


				st.sendResponse(theResponse);
				/*				if (Request.BYE.equals(rcvReq.getMethod())) {
					clearQueue(id + "_" + arg0.getRequest().getMethod());
				}
				 */
				SimonManager.getCounter(
						SipCounters.SIP_NODE_OUTBOUND_ANSWER.toString() + SipCounters.SUCCESS_SUFFIX.toString() + "."
								+ rcvReq.getMethod()
								+ "." + this.sipNodeCtx.getSipNode().getSipNodeName().replace(".", "_")
						).increase();

			}

		} catch (IllegalStateException ise) {
			_logger.error("processRequestPerformance: IllegalStateException for id " + id, ise);
			SimonManager.getCounter(
					SipCounters.SIP_NODE_OUTBOUND_ANSWER.toString() + SipCounters.ERROR_SUFFIX.toString() + "."
							+ arg0.getRequest().getMethod()
							+ "." + sipNodeCtx.getSipNode().getSipNodeName().replace(".", "_")
					).increase();

		} catch (IllegalArgumentException iae) {
			_logger.error("processRequestPerformance: IllegalArgumentException for id " + id, iae);
			SimonManager.getCounter(
					SipCounters.SIP_NODE_OUTBOUND_ANSWER.toString() + SipCounters.ERROR_SUFFIX.toString() + "."
							+ arg0.getRequest().getMethod()
							+ "." + sipNodeCtx.getSipNode().getSipNodeName().replace(" ", "_")
					).increase();
		} catch (Throwable t) {
			_logger.error("processRequestPerformance: Throwable for id " + id, t);
			SimonManager.getCounter(
					SipCounters.SIP_NODE_OUTBOUND_ANSWER.toString() + SipCounters.ERROR_SUFFIX.toString() + "."
							+ arg0.getRequest().getMethod()
							+ "." + sipNodeCtx.getSipNode().getSipNodeName().replace(".", "_")
					).increase();
		}
		
	}

	@Override
	public void processResponse(ResponseEvent responseReceivedEvent) {
		//printQueueSize();
		Response response = (Response) responseReceivedEvent.getResponse();
		if (_logger.isDebugEnabled()) _logger.debug("processResponsePerformance: " + response.getStatusCode());
		CSeqHeader cseH = (CSeqHeader) response.getHeader("CSeq");
		String method = cseH.getMethod();

		String dialId = SipString.cleanString(responseReceivedEvent.getResponse().getHeader("Call-ID").toString().substring("Call-ID".length() + 2)); //ClientTransaction().getDialog().getDialogId();
		//		ArrayBlockingQueue<SipResponseTransaction> respQueue;
		/*		try {
			respQueue = checkResponseQueue(dialId+"_" + response.getStatusCode() + "_" + method);
		} catch (Exception e) {
			_logger.error("processResponsePerformance: error in checkResponseQueue for id = " + dialId,e);
			return;
		}
		 */		

		// Check last received response is different
		/*		if (respQueue != null && respQueue.peek() != null) {
			SipResponseTransaction clientTrans = respQueue.peek();
			Response lastStoredResp = clientTrans.getTheResponse();//clientTrans.getTheTransaction().getLastResponse();
			if (lastStoredResp != null) {
				if ((lastStoredResp.getStatusCode() == response.getStatusCode()) &&
						(lastStoredResp.getHeader("Call-ID").toString().equals(response.getHeader("Call-ID").toString())) &&
						(lastStoredResp.getHeader("From").toString().equals(response.getHeader("From").toString())) &&
						(lastStoredResp.getHeader("To").toString().equals(response.getHeader("To").toString()))) {
					_logger.debug("response already received: should be retransmission " + lastStoredResp.getStatusCode() + ":" + response.getStatusCode());
					return;
				}
			}
		}
		 */		
		
		SIPClientTransactionImpl ct = (SIPClientTransactionImpl) responseReceivedEvent.getClientTransaction();

		SipResponseTransaction respTrans = new SipResponseTransaction(ct, response);

		if (ct==null) {
			if (_logger.isDebugEnabled()) _logger.debug("No transaction put in SipResponseTransaction");
		}

		//		respQueue.add(respTrans);
		//		_logger.debug("processResponsePerformance: put in responsesQeueues for id = " + dialId +". queue size =" + respQueue.size());

		CSeqHeader cs = (CSeqHeader) response.getHeader("CSeq");
		if (Request.INVITE.equals(cs.getMethod())) {
			// _logger.debug("processResponsePerformance: " + response.getStatusCode() + " ! Invite ");
			_logger.error("processResponsePerformance: " + response.getStatusCode() + " ! Invite ");
			int respCode = response.getStatusCode();
			if (respCode > 180 && respCode <200) {
				// early media.
				// Send Prack
				//_logger.debug("processResponsePerformance: " + response.getStatusCode() + " ! send Prack ");
				_logger.error("processResponsePerformance: " + response.getStatusCode() + " ! send Prack ");
				try {
					// Create PRACK
					Request	prackRequest = ct.getDialog().createPrack(response);
					SIPRequest req = (SIPRequest) responseReceivedEvent.getClientTransaction().getRequest();
					prackRequest.setRequestURI(req.getRequestURI());

					ContactHeader contact = (ContactHeader) response.getHeader("Contact");
					Address routeAddr = contact.getAddress();
					RouteHeader routeH = this.sipNodeCtx.getHeaderFactory().createRouteHeader(routeAddr );
					prackRequest.setHeader(routeH);

					// Create the client transaction.
					ClientTransaction ctprack = this.sipNodeCtx.getSipProvider().getNewClientTransaction(prackRequest);

					// send PRACK out.
					ct.getDialog().sendRequest(ctprack);

					SimonManager.getCounter(
							SipCounters.SIP_NODE_OUTBOUND_REQUEST.toString() + SipCounters.SUCCESS_SUFFIX.toString() + "."
									+ "PRACK"
									+ "." + this.sipNodeCtx.getSipNode().getSipNodeName().replace(".", "_")
							).increase();

				} catch (SipException e) {
					SimonManager.getCounter(
							SipCounters.SIP_NODE_OUTBOUND_REQUEST.toString() + SipCounters.ERROR_SUFFIX.toString() + "."
									+ "PRACK"
									+ "." + this.sipNodeCtx.getSipNode().getSipNodeName().replace(".", "_")
							).increase();
					_logger.error("processResponsePerformance: " + response.getStatusCode() + " ! send Prack exception " , e);
					e.printStackTrace();					
				}
			} else if (respCode >=200) {
				if (_logger.isDebugEnabled()) _logger.debug("processResponsePerformance: " + response.getStatusCode() + " ! send Ack ");
				_logger.error("processResponsePerformance: " + response.getStatusCode() + " ! send Ack ");
				// Send ACK in case of final answer (for now 3xx redirect not processed)
				try {
					Request ackRequest = ct.createAck();
					// send ACK out.
					Dialog theDial = null;
					if (ct.getDialog() == null) {
						theDial = this.sipNodeCtx.getSipProvider().getNewDialog(ct);

					} else {
						theDial = ct.getDialog();
					}
					theDial.sendAck(ackRequest);

					SimonManager.getCounter(
							SipCounters.SIP_NODE_OUTBOUND_REQUEST.toString() + SipCounters.SUCCESS_SUFFIX.toString() + "."
									+ "ACK"
									+ "." + this.sipNodeCtx.getSipNode().getSipNodeName().replace(".", "_")
							).increase();

				} catch (SipException e) {
					// TODO Auto-generated catch block
					SimonManager.getCounter(
							SipCounters.SIP_NODE_OUTBOUND_REQUEST.toString() + SipCounters.ERROR_SUFFIX.toString() + "."
									+ "ACK"
									+ "." + this.sipNodeCtx.getSipNode().getSipNodeName().replace(".", "_")
							).increase();

					// e.printStackTrace();
				}
			}
		} else if (Request.BYE.equals(cs.getMethod())) {
			//clearQueue(dialId+"_" + response.getStatusCode() + "_" + method);
			_logger.error("processResponsePerformance: " + response.getStatusCode() + " ! BYE ");
		} else {
			if (_logger.isDebugEnabled()) _logger.debug("processResponsePerformance: " + response.getStatusCode() + " ! Not an Invite, nor a BYE ");
		}		
	}

	@Override
	public void processTimeout(TimeoutEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
