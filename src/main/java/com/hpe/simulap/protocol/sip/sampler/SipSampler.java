//Â© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package com.hpe.simulap.protocol.sip.sampler;

import gov.nist.javax.sip.ClientTransactionExt;
import gov.nist.javax.sip.Utils;
////import gov.nist.javax.sip.address.AddressFactoryEx;
////import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.parser.ViaParser;
import gov.nist.javax.sip.stack.SIPServerTransactionImpl;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RAckHeader;
import javax.sip.header.RSeqHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import org.apache.commons.lang3.CharEncoding;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterVariables;
//import org.apache.jmeter.threads.JMeterVariables;
import org.javasimon.SimonManager;
//import org.javasimon.Split;

import com.hpe.simulap.protocol.sip.config.SipNodeContext;
import com.hpe.simulap.protocol.sip.config.SipNodeElement;
import com.hpe.simulap.protocol.sip.config.SipRequestTransaction;
import com.hpe.simulap.protocol.sip.config.SipResponseTransaction;
import com.hpe.simulap.protocol.sip.config.SipTransactionData;
import com.hpe.simulap.protocol.sip.utils.DialogCreatingRequests;
import com.hpe.simulap.protocol.sip.utils.ListHeaders;
import com.hpe.simulap.protocol.sip.utils.SipCounters;
import com.hpe.simulap.protocol.sip.utils.SipString;

import java.security.SecureRandom;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SipSampler extends AbstractSampler implements TestStateListener, TestIterationListener {

    /**
     * 
     */
    private static final long serialVersionUID = 9000864455912232864L;
    private static final String MISSING = "NODEF:";
    private SipNodeContext sipNodeContext = null;
//    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd:HH:mm:ss.SSS");
    private String sipNodeName = null;
    private String direction = null;
    private boolean isRequest;
    private String dialogueNb = null;
    private String transactionNb = null;
    private String relatedTransactionNb = null;
    private boolean reset = false;
    private boolean isOptional = false;
    private boolean ignoreRetransmission = false;
    private ListHeaders headersMap = new ListHeaders();
    private String identification = null;
    private String trafficType = null;
    //private SipFactory sipFactory = null;
    private HeaderFactory headerFactory = null;
    ////private AddressFactoryEx addressFactory = null;
    private AddressFactory addressFactory = null;
    private MessageFactory messageFactory = null;
    private ListeningPoint udpListeningPoint = null;
    private SipProvider sipProvider = null;

    private SipDialogData theSessionData = null;

    private static final Logger _logger = LoggerFactory.getLogger(SipSampler.class);
    private long timeout = 5000;
    private SecureRandom random = new SecureRandom();

    @Override
    public void testEnded() {
        _logger.debug("testEnded.");
        //		sipNodeContext.getSipStack().stop();
    }

    @Override
    public void testEnded(String arg0) {
        _logger.debug("testEnded arg.");
    }

    @Override
    public void testStarted() {
        _logger.debug("testStarted.");
    }

    @Override
    public void testStarted(String arg0) {
        _logger.debug("testStarted arg.");
    }

    @Override
    public void testIterationStart(LoopIterationEvent arg0) {
        _logger.debug("testIterationStart.");
    }

    private void readSessionData() {
        int threadnb = getThreadContext().getThreadNum();
        if (identification == null || "".equals(identification)) {
            theSessionData = sipNodeContext.readSessionData("SipSessionData." + sipNodeName +"." +dialogueNb + "." + threadnb);
        } else {
            theSessionData = sipNodeContext.readSessionData( "SipSessionData." + sipNodeName +"." + identification +"." +dialogueNb);
        }
        if (theSessionData == null) {
            theSessionData = new SipDialogData(sipNodeContext);
        }
    }

    private void resetSessionData() {
        int threadnb = getThreadContext().getThreadNum();
        if (identification == null || "".equals(identification)) {
            theSessionData = sipNodeContext.readSessionData( "SipSessionData." + sipNodeName +"." +dialogueNb + "." + threadnb);
        } else {
            theSessionData = sipNodeContext.readSessionData( "SipSessionData." + sipNodeName +"." + identification +"." +dialogueNb);
        }

        if (theSessionData != null) {
            theSessionData.clearSipDialogs();
            theSessionData = null;
            if (identification == null || "".equals(identification)) {
            	sipNodeContext.resetSessionData("SipSessionData." + sipNodeName +"." +dialogueNb + "." + threadnb);
            } else {
            	sipNodeContext.resetSessionData("SipSessionData." + sipNodeName +"." + identification +"." +dialogueNb);
            }
        }
        if (sipNodeContext == null) {
            sipNodeContext = getSipNodeContext(getPropertyAsString("sip.node.name"));
        }
        if (sipNodeContext !=null) {
            sipNodeContext.resetQueues(dialogueNb);            	
        }

    }

    private void storeSessionData() {
        int threadnb = getThreadContext().getThreadNum();

        if (identification == null || "".equals(identification)) {
        	sipNodeContext.storeSessionData("SipSessionData." + sipNodeName +"." +dialogueNb + "." + threadnb, theSessionData);
        } else {
        	sipNodeContext.storeSessionData( "SipSessionData." + sipNodeName +"." + identification +"." +dialogueNb, theSessionData);
        }
    }

    
    private void readSessionDataFirst() {
        JMeterVariables variables = getThreadContext().getVariables();
        int threadnb = getThreadContext().getThreadNum();
        if (identification == null || "".equals(identification)) {
            theSessionData = (SipDialogData) variables.getObject( "SipSessionData." + sipNodeName +"." +dialogueNb + "." + threadnb);
        } else {
            theSessionData = (SipDialogData) variables.getObject( "SipSessionData." + sipNodeName +"." + identification +"." +dialogueNb);
        }
        if (theSessionData == null) {
            theSessionData = new SipDialogData(sipNodeContext);
        }
    }

    private void resetSessionDataFirst() {
        JMeterVariables variables = getThreadContext().getVariables();
        int threadnb = getThreadContext().getThreadNum();
        if (identification == null || "".equals(identification)) {
            theSessionData = (SipDialogData) variables.getObject( "SipSessionData." + sipNodeName +"." +dialogueNb + "." + threadnb);
        } else {
            theSessionData = (SipDialogData) variables.getObject( "SipSessionData." + sipNodeName +"." + identification +"." +dialogueNb);
        }

        if (theSessionData != null) {
            theSessionData.clearSipDialogs();
            theSessionData = null;
            if (identification == null || "".equals(identification)) {
                variables.remove("SipSessionData." + sipNodeName +"." +getPropertyAsString("sip.message.dialnb") + "." + threadnb);
            } else {
                variables.remove( "SipSessionData." + sipNodeName +"." + identification +"." +getPropertyAsString("sip.message.dialnb"));
            }
        }
        if (sipNodeContext == null) {
            sipNodeContext = getSipNodeContext(getPropertyAsString("sip.node.name"));
        }
        if (sipNodeContext !=null) {
            sipNodeContext.resetQueues(dialogueNb);            	
        }

    }

    private void resetHeaderMap() {
        headersMap = new ListHeaders();
        int i =0;
        while (getPropertyAsString("sip.header.name." + i) != null && !"".equals(getPropertyAsString("sip.header.name." + i))) {
            headersMap.addHeader(getPropertyAsString("sip.header.name." + i),getPropertyAsString("sip.header.value." + i));
            i++;
        }
    }

    private void addHeaders (Message request) throws ParseException {

        if (sipNodeContext.isProxy()) {
            request.removeHeader("Route");
        }

        for (java.util.Map.Entry<String, List<String>> entry : headersMap.entrySet())
        {
            _logger.info("add header {} : {}", entry.getKey(), entry.getValue());
            for (String oneVal : entry.getValue()) {
                if (entry.getKey().equals("Max-Forwards")) {
                    request.removeHeader("Max-Forwards");
                }
                if ( ! MISSING.equals(oneVal)) {
                    request.addHeader(headerFactory.createHeader(entry.getKey(), oneVal));
                }
            }
        }
    }


    private SipNodeContext getSipNodeContext(String sipNodeName) {
        if (sipNodeName != null) {
            JMeterVariables variables = getThreadContext().getVariables();

            if (variables != null) {
                return (SipNodeContext) variables.getObject(sipNodeName);
            }
        }	
        return null;
    }

    private void readFactories() throws Exception {
        headerFactory = sipNodeContext.getHeaderFactory();
        addressFactory = sipNodeContext.getAddressFactory();
        messageFactory = sipNodeContext.getMessageFactory();
        udpListeningPoint = sipNodeContext.getSipListening();
        sipProvider = sipNodeContext.getSipProvider();		
    }

    @Override
    public SampleResult sample(Entry arg0) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.sampleStart();
        res.setDataType(SampleResult.TEXT);
        res.setDataEncoding(System.getProperty("file.encoding"));

        sipNodeName = getPropertyAsString("sip.node.name");
        sipNodeContext = getSipNodeContext(sipNodeName);

        if (sipNodeContext == null) {
        	if (_logger.isDebugEnabled()) _logger.debug("sipNodeContext is null");
            res.setResponseMessage("SIP node context not available ");
            res.setSuccessful(false);
            res.sampleEnd();
            return res;
        }

        if (!sipNodeContext.isNodeReady()) {
            if (_logger.isDebugEnabled()) _logger.debug("sipNode not ready : {}", sipNodeContext.getNotReadyReason());
            res.setResponseMessage("SIP node not ready : " + sipNodeContext.getNotReadyReason());
            res.setSuccessful(false);
            res.sampleEnd();
            return res;
        }		

        direction = getPropertyAsString("sip.message.direction");
        isRequest = "request".equals(getPropertyAsString("sip.message.type"));
        dialogueNb = getPropertyAsString("sip.message.dialnb");
        transactionNb = getPropertyAsString("sip.message.transnb");
        relatedTransactionNb = getPropertyAsString("sip.message.relatedtransnb");
        reset = getPropertyAsBoolean("sip.message.reset");

        if (_logger.isDebugEnabled()) _logger.debug("sampler sample {} start for node name {} : {} : {} : {} :: {} : {}", new Object[]{ getName(), sipNodeName, direction, isRequest, dialogueNb, transactionNb});

        resetHeaderMap();
        if (sipNodeContext == null) {
            sipNodeContext = getSipNodeContext(getPropertyAsString("sip.node.name"));
        }

        trafficType = sipNodeContext.getSipNode().getTraficType();
        if (_logger.isDebugEnabled()) _logger.debug("sipNodeContext.getSipNode().getTraficType() {}", trafficType);
        if (SipNodeElement.PERFORMANCE_TRAFIC.equals(trafficType)) {
            List<String> idHeaders = headersMap.getHeaders(sipNodeContext.getSipNode().getIdentificationHeader());
            if (idHeaders.size() == 1) {
                identification = idHeaders.get(0);
            }
        }
        if (_logger.isDebugEnabled()) _logger.debug("identification {} : {}", identification, sipNodeContext.getSipNode().getIdentificationHeader());

        if (reset) {
            resetSessionData();
        }		
        readSessionData();
        isRequest = "request".equals(getPropertyAsString("sip.message.type"));

        if (_logger.isDebugEnabled()) _logger.debug("sample: {} : {} :: {} : {}: {}", new Object[]{ direction, isRequest, dialogueNb, transactionNb, relatedTransactionNb});
    	long start = dateToString();
        if ("send".equals(direction)) {
            sendMessage(res);
        } else if ("receive".equals(direction)) {
            receiveMessage(res);
        } else {
            if (_logger.isDebugEnabled()) _logger.debug("sample unknown {}", direction);
        }
        if (_logger.isDebugEnabled()) {long diff = dateToString() - start;
	if (diff > 10 && _logger.isWarnEnabled()) _logger.warn("sampler sample for node name  {} : {} : {} :: {} {} -> RUN took {}", new Object[]{ sipNodeName, direction, isRequest, dialogueNb, transactionNb, diff}); }
        res.sampleEnd();
//        if (reset) {
//            resetSessionData();
//        }		

        return res;
    }
	private long dateToString() {
		return System.currentTimeMillis();
	}


    private void sendMessage(SampleResult res) {
    	if (_logger.isDebugEnabled()) _logger.debug("sendMessage");
        try {
            readFactories();
        } catch (Exception ex) {
            res.setResponseMessage("Failed to read factories : " + ex.getMessage());
            res.setSuccessful(false);
        }

        if (isRequest) {
            String command = getPropertyAsString("sip.message.command");
            sendRequestMessage(res, command);
        } else {
            sendAnswerMessage(res);

            if (res.isSuccessful()) {
                SimonManager.getCounter(
                        SipCounters.SIP_NODE_OUTBOUND_ANSWER.toString() + SipCounters.SUCCESS_SUFFIX.toString() + "."
                                + getPropertyAsString("sip.message.response.code")
                                + "." + sipNodeContext.getSipNode().getSipNodeName().replace(".", "_")
                        ).increase();
            } else {
                SimonManager.getCounter(
                        SipCounters.SIP_NODE_OUTBOUND_ANSWER.toString() + SipCounters.ERROR_SUFFIX.toString() + "."
                                + getPropertyAsString("sip.message.response.code")
                                + "." + sipNodeContext.getSipNode().getSipNodeName().replace(".", "_")
                        ).increase();
            }

        }
    }

    private void sendRequestMessage(SampleResult res, String command) {

        if ("INVITE".equals(command)) {
            if (theSessionData.getTheDialog() == null) {
                sendRequestNewDialog(res, command);
            } else {
                //// NEW
                sendRequestInDialog(res, command);//sendReInvite(res,command);
            }
        } else if ("ACK".equals(command)) {
            sendAckRequest(res);
        } else if ("REGISTER".equals(command)) {
            sendRequestNewDialog(res, command);
        } else if ("BYE".equals(command)) {
            //// NEW
            sendRequestInDialog(res, command);//sendByeRequest(res, command);
        } else if ("PRACK".equals(command)) {
            sendPrackRequest(res, command);
        } else if ("INFO".equals(command)) {
            sendRequestInDialog(res, command);
        } else if ("UPDATE".equals(command)) {
            sendRequestInDialog(res, command);
        } else if ("OPTIONS".equals(command)) {
            sendRequestInDialog(res, command);
        } else if ("NOTIFY".equals(command)) {
                if (theSessionData.getTheDialog() == null) {
                    sendRequestNewDialog(res, command);
                } else {
                    sendRequestInDialog(res, command);
                }
        } else if ("SUBSCRIBE".equals(command)) {
            sendRequestNewDialog(res, command);
        } else if ("MESSAGE".equals(command)) {
            sendRequestInDialog(res, command);
        } else if ("REFER".equals(command)) {
                if (theSessionData.getTheDialog() == null) {
                    sendRequestNewDialog(res, command);
                } else {
                    sendRequestInDialog(res, command);
                }
        } else if ("PUBLISH".equals(command)) {
            sendRequestNewDialog(res, command);
        } else if ("CANCEL".equals(command)) {
            sendCancelRequest(res, command);
        } else {
            res.setResponseMessage("sendRequestMessage unsupported command : " + command);
            res.setSuccessful(false);
        }

        if (res.isSuccessful()) {
            SimonManager.getCounter(
                    SipCounters.SIP_NODE_OUTBOUND_REQUEST.toString() + SipCounters.SUCCESS_SUFFIX.toString() + "."
                            + command
                            + "." + sipNodeContext.getSipNode().getSipNodeName().replace(".", "_")
                    ).increase();
        } else {
            SimonManager.getCounter(
                    SipCounters.SIP_NODE_OUTBOUND_REQUEST.toString() + SipCounters.ERROR_SUFFIX.toString() + "."
                            + command
                            + "." + sipNodeContext.getSipNode().getSipNodeName().replace(".", "_")
                    ).increase();			
        }
    }

    private void sendRequestNewDialog(SampleResult res, String command) {
	if (_logger.isDebugEnabled()) _logger.debug("sendRequestNewDialog {}", command);
        String result = "";
        //SipURI requestURI = null;
        URI requestURI = null;
        try {
            // Request URI
            String reqUri = extractOneValue(headersMap.removeHeaders("Request-URI"));
            if (reqUri == null || "".equals(reqUri)) {
            	if (_logger.isDebugEnabled()) _logger.debug("Request-URI header not set.");
                result = result + "Request-URI header not set.\n";
            } else {
                //requestURI = createReqURI(reqUri);
                requestURI = createReqURITelOrSip(reqUri);
                if (requestURI == null) {
                	if (_logger.isDebugEnabled()) _logger.debug("Request-URI badly formated.");
                    result = result + "Request-URI badly formated.\n";        			
                }
            }

            // From header
            FromHeader fromHeader = null;
            String from = extractOneValue(headersMap.removeHeaders("From"));
            String tag;
            if (from != null) {
                fromHeader = createFromHeader(from);
                tag = fromHeader.getTag();
                if (tag == null || "".equals(tag)) {
                    //tag = new Integer((int) (Math.random() * 10000)).toString();
                    fromHeader.setTag(nextTag());//tag);
                }

            } else {
                result = result + "From header not set.\n";
            }

            // To header
            ToHeader toHeader = null;
            String to = extractOneValue(headersMap.removeHeaders("To"));
            if (to != null) {
                toHeader = createToHeader(to);
            } else {
                result = result + "To header not set.\n";
            }

            // Via headers
            ArrayList<ViaHeader> viaHeaders = createViaHeader();
            if (viaHeaders == null) {
                viaHeaders = new ArrayList<ViaHeader>();
            }
            // Cseq header
            String cseqString = extractOneValue(headersMap.removeHeaders("CSeq"));
            CSeqHeader cSeqHeader = null;
            if (cseqString != null) {
                String[] cseqparts = GuiHeadersParser.decodeCseq(cseqString);
                if (cseqparts.length == 2) {
                    cSeqHeader = headerFactory.createCSeqHeader(Long.parseLong(cseqparts[0]),
                            cseqparts[1]);        			
                } else if (cseqparts.length == 1) {
                    cSeqHeader = headerFactory.createCSeqHeader(Long.parseLong(cseqparts[0]),
                            command);
                } else {
                    result = result + "Cseq header not set. Badly decoded.\n";
                }
            } else {
                cSeqHeader = headerFactory.createCSeqHeader(1L, command);
            }

            // MaxForwards header
            String maxF = extractOneValue(headersMap.removeHeaders("Max-Forwards"));//headersMap.remove("Max-Forwards");
            MaxForwardsHeader maxForwards = null;
            if (maxF != null) {
                maxForwards = headerFactory
                        .createMaxForwardsHeader(Integer.parseInt(maxF));
            } else {
            	if (_logger.isDebugEnabled()) _logger.debug("MaxForwards header not set. Set to default value 10");
                maxForwards = headerFactory.createMaxForwardsHeader(10);
            }

            // CallId header
            CallIdHeader callIdHeader = null;            
            String callidStored = theSessionData.getTheCallId();
            String callid = extractOneValue(headersMap.removeHeaders("Call-ID")); 
            if (_logger.isDebugEnabled()) _logger.debug("callId {} : {}", callid, callidStored);
            if (callidStored != null && !"".equals(callidStored)) {
                callIdHeader = headerFactory.createCallIdHeader(callidStored);
                callid = callidStored;
            } else if (callid == null || "".equals(callid)) {
                callIdHeader = sipProvider.getNewCallId();
                callid = callIdHeader.getCallId();
            } else {
                callIdHeader = headerFactory.createCallIdHeader(callid);
            }

            // Check mandatory headers are set properly
            if (!"".equals(result)) {
		if (_logger.isDebugEnabled()) _logger.debug("Send message error. Missing parameters : {}", result);
                res.setResponseMessage("Send message error. Missing parameters : " + result);
                res.setSuccessful(false);
                return;
            }

            // Create the request
            Request request = messageFactory.createRequest(requestURI,
                    command, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);

            // Contact header
            ContactHeader contactHeader = null;
            String contact = extractOneValue(headersMap.removeHeaders("Contact"));//headersMap.remove("Contact");
            if (contact != null) {
                Address address = addressFactory.createAddress(contact);
                contactHeader = headerFactory.createContactHeader(address);
                request.addHeader(contactHeader);
            } else {
            	if (_logger.isDebugEnabled()) _logger.debug("contact header not set.");
            }            

            // Body part
            String body = getPropertyAsString("sip.body.text");
            if (body != null  && !"".equals(body)) {
                // Create ContentTypeHeader
                String contType = extractOneValue(headersMap.removeHeaders("Content-Type"));
                String ct1 = "application";
                String ct2 = "sdp";
                if (contType != null) {
                    ct1 = contType.substring(0, contType.indexOf("/"));
                    ct2 = contType.substring(contType.indexOf("/") + 1);
                }

                ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader(ct1, ct2);

                request.setContent(body.getBytes(), contentTypeHeader);//request.setContent(contents, contentTypeHeader);        		
            }

            // Event header for the subscription.
            String event = extractOneValue(headersMap.removeHeaders("Event"));
            if (event != null) {
		if (_logger.isDebugEnabled()) _logger.debug("Event header set to {}", event);
                EventHeader eventHeader = headerFactory.createEventHeader(event);
                request.addHeader(eventHeader);
            } else {
            	if (_logger.isDebugEnabled()) _logger.debug("Event header not set.");
            }

            // add all other headers
            addHeaders(request);

            // Create the client transaction.
            ClientTransactionExt inviteTid = (ClientTransactionExt) sipProvider.getNewClientTransaction(request);
            inviteTid.setNotifyOnRetransmit(false);


            if (inviteTid.getDialog() == null) {
            	if (_logger.isDebugEnabled()) _logger.debug("inviteTid.getDialog() is NULL");
            }

            Dialog theDial = null;
            if (inviteTid.getDialog() == null && DialogCreatingRequests.isCreating(command)) {
                theDial = sipProvider.getNewDialog(inviteTid);			
            } else {
                theDial = inviteTid.getDialog();
            }

            // send the request out.
            if (_logger.isDebugEnabled()) _logger.debug("sends {}", command);
            inviteTid.sendRequest();
            
            if ("INVITE".equals(command)) {
//            	if ( _logger.isWarnEnabled()) _logger.warn("SND INVITE : " + request.getHeader("Call-ID").toString().substring("Call-ID: ".length()).replaceAll("\\r|\\n", "")+ ", From = " + request.getHeader("From").toString().replaceAll("\\r|\\n", "")+ ", From = " + request.getHeader("To").toString().replaceAll("\\r|\\n", "") + "," + LocalDateTime.now().format(formatter));
    		}


            if (theDial == null) {
            	if (_logger.isDebugEnabled()) _logger.debug("theDialog is NULL");
            }

            startAndSetSplitInClientTransaction(command, inviteTid);

            // Store available data about the session
            SipTransactionData theTransData = theSessionData.getATransaction(transactionNb);
            theTransData.setTheTransaction(inviteTid);
            theTransData.setLastRequest(request);
            theSessionData.setLastRequest(request);
            if (_logger.isDebugEnabled()) _logger.debug("setTheCallId with {}", callid);
            theSessionData.setTheCallId(callid);

            theSessionData.setTheDialog(theDial);//inviteTid.getDialog());
            theSessionData.setPreviousTransaction(transactionNb);
            storeSessionData();

            // set sampler result
            res.setResponseMessage(inviteTid.getRequest().toString());
            res.setSuccessful(true);

        } catch (Exception ex) {
            _logger.error("send message exception : {}", ex);
            res.setResponseMessage("send message exception : " + ex.getMessage());
            res.setSuccessful(false);
        }
    }


    private void sendPrackRequest(SampleResult res, String command){
    	if (_logger.isDebugEnabled()) _logger.debug("sendPrackRequestMessage");
        try {

            if (_logger.isDebugEnabled()) {
                _logger.debug("sendPrackRequestMessage based on {} : {}", theSessionData.getLastResponse().getStatusCode(),
                        theSessionData.getLastResponse().getHeader("RSeq"));
            }
            // Create PRACK
            Response lastResp = null;
            if (relatedTransactionNb != null && !"".equals(relatedTransactionNb) && !"None".equals(relatedTransactionNb)) {
                lastResp = theSessionData.getATransaction(relatedTransactionNb).getLastResponse(); 
            } else {
                lastResp = theSessionData.getLastResponse();
            }

            Request	prackRequest = theSessionData.getTheDialog().createPrack(lastResp);

            // Request URI
            SipURI requestURI = null;
            String reqUri = extractOneValue(headersMap.removeHeaders("Request-URI"));
            if (reqUri != null) {
		if (_logger.isDebugEnabled()) _logger.debug("Request-URI header set : {}", reqUri);
                String[] req = GuiHeadersParser.decodeReqUri(reqUri);
                requestURI = addressFactory.createSipURI(req[0], req[1] );
                prackRequest.setRequestURI(requestURI);
            }

            RAckHeader rackHeader = null;
            String rack = extractOneValue(headersMap.removeHeaders("RAck"));
            if (rack != null) {
	            String[] racksplit = rack.split(" ");
	            if (rack != null && racksplit != null && racksplit.length == 3) {
	            	try {
	                rackHeader = headerFactory.createRAckHeader(Integer.parseInt(racksplit[0]), Integer.parseInt(racksplit[1]), racksplit[2]);
	                prackRequest.removeHeader("RAck");
	                prackRequest.setHeader(rackHeader);
			if (_logger.isDebugEnabled()) _logger.debug("Rack header tag set : {}", rack);
	            	} catch (NumberFormatException nfe) {
				if (_logger.isDebugEnabled()) _logger.debug("Rack header tag not set. Input badly formated : {} : {}", rack, nfe);
	            	} catch (InvalidArgumentException iae) {
				if (_logger.isDebugEnabled()) _logger.debug("Rack header tag not set. Invalid argument : {} : {}", rack, iae);
			}
	            }
            }

            // add other headers
            addHeaders(prackRequest);

            // Body part
            String body = getPropertyAsString("sip.body.text");
            if (body != null  && !"".equals(body)) {
                // ContentType header
                String contType = extractOneValue(headersMap.removeHeaders("Content-Type"));
                String ct1 = "application";
                String ct2 = "sdp";
                if (contType != null) {
                    ct1 = contType.substring(0, contType.indexOf("/"));
                    ct2 = contType.substring(contType.indexOf("/") + 1);
                }	                
                ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader(ct1, ct2);
                prackRequest.setContent(body.getBytes(), contentTypeHeader);
            }


            // Create the client transaction.
            ClientTransaction ct = sipProvider.getNewClientTransaction(prackRequest);

            // send PRACK out.
            theSessionData.getTheDialog().sendRequest(ct);

            startAndSetSplitInClientTransaction(command, ct);
            // Store available data about the session
            SipTransactionData theTransData = theSessionData.getATransaction(transactionNb);
            theTransData.setTheTransaction(ct);
            theTransData.setLastRequest(prackRequest);
            String callid = ((CallIdHeader) prackRequest.getHeader("Call-ID")).getCallId();
            theSessionData.setLastRequest(prackRequest);
            theSessionData.setTheCallId(callid );
            Dialog theDial = null;
            if (ct.getDialog() == null) {
                theDial = sipProvider.getNewDialog(ct);

            } else {
                theDial = ct.getDialog();
            }
            theSessionData.setTheDialog(theDial);
            storeSessionData();

            // set sampler result
            res.setResponseMessage(prackRequest.toString());
            res.setSuccessful(true);
        } catch (SipException e) {
            _logger.error("send Prack SipException : {}", e);
            res.setResponseMessage("send Prack SipException : "+ e.getMessage());
            res.setSuccessful(false);
        } catch (ParseException e) {
            _logger.error("send Prack ParseException : {}", e);
            res.setResponseMessage("send Prack ParseException : "+e.getMessage());
            res.setSuccessful(false);
        }

    }

    private void sendAckRequest(SampleResult res) {
    	if (_logger.isDebugEnabled()) _logger.debug("sendAckRequest");

        try {
            ClientTransaction inviteTid = (ClientTransaction) theSessionData.getATransaction(transactionNb).getTheTransaction();

            // Check if last return code was an error one. (in such case, the ACK is sent back automatically by the stack)
            if (theSessionData.getATransaction(transactionNb).getLastResponse().getStatusCode() >= 300) {
                if (_logger.isDebugEnabled()) _logger.debug("sendAckRequest: not needed as last answer was {}", theSessionData.getATransaction(transactionNb).getLastResponse().getStatusCode());
                res.setResponseMessage("sendAckRequest: not needed as last answer was " + theSessionData.getATransaction(transactionNb).getLastResponse().getStatusCode());
                res.setSuccessful(true);
                return;
            }

            if (inviteTid != null) {
                Dialog dialog = theSessionData.getTheDialog();
                Request ackRequest = null;
                try {
                    // CSeq header
                    String cseqString = extractOneValue(headersMap.removeHeaders("CSeq"));
                    if (cseqString != null) {
                        String[] cseqparts = GuiHeadersParser.decodeCseq(cseqString);
                        if (cseqparts.length == 2) {
                            // Create ACK
                            ackRequest = dialog.createAck( Long.parseLong(cseqparts[0]));
                        } else {
                        	if (_logger.isDebugEnabled()) _logger.debug("Cannot creat ACK. Cseq header badly decoded");
                            res.setResponseMessage("Cannot creat ACK. Cseq header badly decoded");
                            res.setSuccessful(false);
                            return;
                        }
                    } else {
                    	if (_logger.isInfoEnabled()) _logger.info("CSeq header not set.");
                        Response response = theSessionData.getATransaction(transactionNb).getLastResponse();
                        if (response == null) {
                            response = theSessionData.getLastResponse();
                        }
                        if (response == null) {
                        	if (_logger.isDebugEnabled()) _logger.debug("Cannot creat ACK. CSeq header not set. Response is NULL");
                            res.setResponseMessage("Cannot creat ACK. CSeq header not set. Response is NULL");
                            res.setSuccessful(false);
                            return;
                        } else if (response.getHeader(CSeqHeader.NAME) == null) {
                        	if (_logger.isDebugEnabled()) _logger.debug("Cannot creat ACK. CSeq header not set. Response.getheader(CSeq) is NULL");
                            res.setResponseMessage("Cannot creat ACK. CSeq header not set. Response CSeq header is NULL");
                            res.setSuccessful(false);
                            return;
                        } else if (dialog == null) {
			    if (_logger.isDebugEnabled()) _logger.debug("Cannot creat ACK. CSeq header not set. Dialog is NULL and {}", inviteTid.getDialog());
                            res.setResponseMessage("Cannot creat ACK. CSeq header not set. Dialog is NULL");
                            res.setSuccessful(false);
                            return;				      			
                        }
                        ackRequest = dialog.createAck( ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getSeqNumber());
                    }
                    
                    //// DAV: To and From headers to force if related transaction not enough ! create Client transaction before or after ////
                    // From header
                    FromHeader fromHeader = null;
                    String from = extractOneValue(headersMap.removeHeaders("From"));
                    String tag;
                    if (from != null) {
                        fromHeader = createFromHeader(from);
                        tag = fromHeader.getTag();
                        if (tag == null || "".equals(tag)) {
                            //tag = new Integer((int) (Math.random() * 10000)).toString();
                            fromHeader.setTag(nextTag());//tag);
                        }
                        ackRequest.removeHeader("From");
                        ackRequest.setHeader(fromHeader);
                    } 
                    // To header
                    ToHeader toHeader = null;
                    String to = extractOneValue(headersMap.removeHeaders("To"));
                    if (to != null) {
                        toHeader = createToHeader(to);
                        ackRequest.removeHeader("To");
                        ackRequest.setHeader(toHeader);
                    } 


                    // Request URI
                    SipURI requestURI = null;
                    String reqUri = extractOneValue(headersMap.removeHeaders("Request-URI", 1));//headersMap.remove("Request-URI");	        	
                    if (reqUri != null) {
						if (_logger.isDebugEnabled()) _logger.debug("Request-URI header set : {}", reqUri);
                        String[] req = GuiHeadersParser.decodeReqUri(reqUri);
                        requestURI = addressFactory.createSipURI(req[0], req[1] );
                        ackRequest.setRequestURI(requestURI);
                    }
				 // add other headers
                    addHeaders(ackRequest);
                    
                    // Route header
                    ArrayList<RouteHeader> routeHeaders = createRouteHeader();
                    if (routeHeaders != null) {
                    	ackRequest.removeHeader("Route");
                        for (RouteHeader routeHeader : routeHeaders) {
                        	ackRequest.addHeader(routeHeader);
                        }
                    }

                    // Body part
                    String body = getPropertyAsString("sip.body.text");
                    if (body != null  && !"".equals(body)) {
                        // ContentType header
                        String contType = extractOneValue(headersMap.removeHeaders("Content-Type"));
                        String ct1 = "application";
                        String ct2 = "sdp";
                        if (contType != null) {
                            ct1 = contType.substring(0, contType.indexOf("/"));
                            ct2 = contType.substring(contType.indexOf("/") + 1);
                        }	                
                        ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader(ct1, ct2);
                        ackRequest.setContent(body.getBytes(), contentTypeHeader);
                    }

                    // send ACK out.
                    dialog.sendAck(ackRequest);

                    // set sampler result
                    res.setResponseMessage(ackRequest.toString());
                    res.setSuccessful(true);
                } catch (InvalidArgumentException ex) {
                    _logger.debug("Cannot send Ack, InvalidArgumentException : {}", ex);
                    res.setResponseMessage("Cannot send Ack, InvalidArgumentException :" + ex.toString());
                    res.setSuccessful(false);
                } catch (SipException e) {
                    _logger.debug("Cannot send Ack, SipException : {}", e);
                    res.setResponseMessage("Cannot send Ack, SipException :" + e.toString());
                    res.setSuccessful(false);
                } catch (ParseException e) {
                    _logger.debug("Cannot send Ack, ParseException : {}", e);
                    res.setResponseMessage("Cannot send Ack, ParseException :" + e.toString());
                    res.setSuccessful(false);
                }
            } else {
            	if (_logger.isDebugEnabled()) _logger.debug("Cannot send Ack, no transaction stored");
                res.setResponseMessage("Cannot send Ack, no transaction stored");
                res.setSuccessful(false);
            }
        } catch (Exception e) {
            _logger.debug("send Ack exception : {}", e);
            res.setResponseMessage("send Ack exception " + e.toString());
            res.setSuccessful(false);
        }

    }

    private void sendRequestInDialog(SampleResult res, String command) {
	if (_logger.isDebugEnabled()) _logger.debug("sendRequestInDialog {}", command);
        String result = "";
        try {        	
            // Get the Transaction.
            Transaction inviteTid = theSessionData.getATransaction(transactionNb).getTheTransaction();
           
            if (inviteTid == null) {
                if (_logger.isInfoEnabled())
                {
                    _logger.info("transaction is null");
                }
            }
         // Forking case: if relatedTransactionNb != null
            Dialog relatedDialog = null;
            if (relatedTransactionNb != null && !"".equals(relatedTransactionNb) && !"None".equals(relatedTransactionNb)) {
            //if (!"None".equals(relatedTransactionNb)) {
		if (_logger.isInfoEnabled()) _logger.info("sendRequestInDialog with  relatedTransactionNb = {}", relatedTransactionNb);
            	relatedDialog = theSessionData.getADialog(relatedTransactionNb);
            } 
            // Get the Dialog.
            Dialog theDialog = (relatedDialog!= null) ? relatedDialog :theSessionData.getTheDialog();         
            if (theDialog == null) {
                if (_logger.isDebugEnabled()) _logger.debug("dialog is null");
            }
            Dialog dialog = (theDialog!= null) ? theDialog : inviteTid.getDialog();            
            if (dialog == null) {
                if (_logger.isDebugEnabled()) _logger.debug("dialog is null");
                res.setResponseMessage("dialog is null, cannot create " + command);
                res.setSuccessful(false);
                return;
            }

            // Create the request
            Request request = null;
            if (relatedTransactionNb != null && !"".equals(relatedTransactionNb) && !"None".equals(relatedTransactionNb)) {
                URI requestURI = null;
                    // Request URI
                    String reqUri = extractOneValue(headersMap.removeHeaders("Request-URI"));
                    if (reqUri == null || "".equals(reqUri)) {
                    	if (_logger.isDebugEnabled()) _logger.debug("Request-URI header not set.");
                        result = result + "Request-URI header not set.\n";
                    } else {
                        //requestURI = createReqURI(reqUri);
                        requestURI = createReqURITelOrSip(reqUri);
                        if (requestURI == null) {
                        	if (_logger.isDebugEnabled()) _logger.debug("Request-URI badly formated.");
                            result = result + "Request-URI badly formated.\n";        			
                        }
                    }

                    // From header
                    FromHeader fromHeader = null;
                    String from = extractOneValue(headersMap.removeHeaders("From"));
                    String tag;
                    if (from != null) {
                        fromHeader = createFromHeader(from);
                        tag = fromHeader.getTag();
                        if (tag == null || "".equals(tag)) {
                            //tag = new Integer((int) (Math.random() * 10000)).toString();
                            fromHeader.setTag(nextTag());//tag);
                        }

                    } else {
                        result = result + "From header not set.\n";
                    }

                    // To header
                    ToHeader toHeader = null;
                    String to = extractOneValue(headersMap.removeHeaders("To"));
                    if (to != null) {
                        toHeader = createToHeader(to);
                    } else {
                        result = result + "To header not set.\n";
                    }

                    // Via headers
                    ArrayList<ViaHeader> viaHeaders = createViaHeader();
                    if (viaHeaders == null) {
                        viaHeaders = new ArrayList<ViaHeader>();
                    }
                    // Cseq header
                    String cseqString = extractOneValue(headersMap.removeHeaders("CSeq"));
                    CSeqHeader cSeqHeader = null;
                    if (cseqString != null) {
                        String[] cseqparts = GuiHeadersParser.decodeCseq(cseqString);
                        if (cseqparts.length == 2) {
                            cSeqHeader = headerFactory.createCSeqHeader(Long.parseLong(cseqparts[0]),
                                    cseqparts[1]);        			
                        } else if (cseqparts.length == 1) {
                            cSeqHeader = headerFactory.createCSeqHeader(Long.parseLong(cseqparts[0]),
                                    command);
                        } else {
                            result = result + "Cseq header not set. Badly decoded.\n";
                        }
                    } else {
                        cSeqHeader = headerFactory.createCSeqHeader(1L, command);
                    }

                    // MaxForwards header
                    String maxF = extractOneValue(headersMap.removeHeaders("Max-Forwards"));//headersMap.remove("Max-Forwards");
                    MaxForwardsHeader maxForwards = null;
                    if (maxF != null) {
                        maxForwards = headerFactory
                                .createMaxForwardsHeader(Integer.parseInt(maxF));
                    } else {
                    	if (_logger.isDebugEnabled()) _logger.debug("MaxForwards header not set. Set to default value 10");
                        maxForwards = headerFactory.createMaxForwardsHeader(10);
                    }

                    // CallId header
                    CallIdHeader callIdHeader = null;            
                    String callidStored = theSessionData.getTheCallId();
                    String callid = extractOneValue(headersMap.removeHeaders("Call-ID")); 
                    if (_logger.isDebugEnabled()) _logger.debug("callId {} : {}", callid, callidStored);
                    if (callidStored != null && !"".equals(callidStored)) {
                        callIdHeader = headerFactory.createCallIdHeader(callidStored);
                        callid = callidStored;
                    } else if (callid == null || "".equals(callid)) {
                        callIdHeader = sipProvider.getNewCallId();
                        callid = callIdHeader.getCallId();
                    } else {
                        callIdHeader = headerFactory.createCallIdHeader(callid);
                    }

                    // Check mandatory headers are set properly
                    if (!"".equals(result)) {
			if (_logger.isDebugEnabled()) _logger.debug("Send message error. Missing parameters : {}", result);
                        res.setResponseMessage("Send message error. Missing parameters : " + result);
                        res.setSuccessful(false);
                        return;
                    }

                    // Create the request
                    request = messageFactory.createRequest(requestURI,
                        command, callIdHeader, cSeqHeader, fromHeader,
                        toHeader, viaHeaders, maxForwards);

            } else {
                request = dialog.createRequest(command);
            
                //// DAV: To and From headers to force if related transaction not enough ! create Client transaction before or after ////
            // From header
            FromHeader fromHeader = null;
            String from = extractOneValue(headersMap.removeHeaders("From"));
            String tag;
            if (from != null) {
                fromHeader = createFromHeader(from);
                request.removeHeader("From");
                request.setHeader(fromHeader);
            } 
            // To header
            ToHeader toHeader = null;
            String to = extractOneValue(headersMap.removeHeaders("To"));
            if (to != null) {
                toHeader = createToHeader(to);
                request.removeHeader("To");
                request.setHeader(toHeader);
            } 


            // CSeq header
            String cseqString = extractOneValue(headersMap.removeHeaders("CSeq", 1));
            if (_logger.isDebugEnabled()) _logger.debug("Cseq string value = {}", cseqString);
            CSeqHeader cSeqHeader = null;
            if (cseqString != null) {
                String[] cseqparts = GuiHeadersParser.decodeCseq(cseqString);
                if (cseqparts.length == 2) {
                    cSeqHeader = headerFactory.createCSeqHeader(Long.parseLong(cseqparts[0]),
                            cseqparts[1]);        			
                } else {
                	if (_logger.isDebugEnabled()) _logger.debug("Cseq header not set. Badly decoded");
                }
            } else {
            	if (_logger.isDebugEnabled()) _logger.debug("Cseq header not set. No value");
            }            
            if (cSeqHeader != null) {
                request.setHeader(cSeqHeader);
            }

            // Request URI
            SipURI requestURI = null;
            String reqUri = extractOneValue(headersMap.removeHeaders("Request-URI"));//headersMap.remove("Request-URI");
            if (reqUri != null) {
		if (_logger.isDebugEnabled()) _logger.debug("Request-URI header set : {}", reqUri);
                String[] req = GuiHeadersParser.decodeReqUri(reqUri);
                requestURI = addressFactory.createSipURI(req[0], req[1] );
                request.setRequestURI(requestURI);
            }

            // Via headers
            ArrayList<ViaHeader> viaHeaders = createViaHeader();
            if (viaHeaders == null) {
                viaHeaders = new ArrayList<ViaHeader>();
            }
        }
            // Body part
            String body = getPropertyAsString("sip.body.text");
            if (body != null  && !"".equals(body)) {
                // ContentTypeHeader
                String contType = extractOneValue(headersMap.removeHeaders("Content-Type"));
                String ct1 = "application";
                String ct2 = "sdp";
                if (contType != null) {
                    ct1 = contType.substring(0, contType.indexOf("/"));
                    ct2 = contType.substring(contType.indexOf("/") + 1);
                }                
                ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader(ct1, ct2);
                request.setContent(body.getBytes(), contentTypeHeader);
            }

            // Contact header
            ContactHeader contactHeader = null;
            String contact = extractOneValue(headersMap.removeHeaders("Contact"));//headersMap.remove("Contact");
            if (contact != null) {
            	if (_logger.isDebugEnabled()) _logger.debug("contact header set by scenario.");
                Address address = addressFactory.createAddress(contact);
                contactHeader = headerFactory.createContactHeader(address);
                request.removeHeader("Contact");
                request.addHeader(contactHeader);
            } else {
            	if (_logger.isDebugEnabled()) _logger.debug("contact header automatically set.");
            }            

            // add other headers
            addHeaders(request);
            // Route header
            ArrayList<RouteHeader> routeHeaders = createRouteHeader();
            if (routeHeaders != null) {
            	request.removeHeader("Route");
                for (RouteHeader routeHeader : routeHeaders) {
                	request.addHeader(routeHeader);
                }
            }
            ClientTransaction ct = null;


            // send the request out.
            if (relatedTransactionNb != null && !"".equals(relatedTransactionNb) && !"None".equals(relatedTransactionNb)) {
            	sipProvider.sendRequest(request);
            } else {
                // Create the client transaction.
                ct = sipProvider.getNewClientTransaction(request);
            	dialog.sendRequest(ct);
            }

            startAndSetSplitInClientTransaction(command, ct);
            //// NEW
            // Store available data about the session
            SipTransactionData theTransData = theSessionData.getATransaction(transactionNb);
            theTransData.setTheTransaction(ct);
            theTransData.setLastRequest(request);
            String callid = ((CallIdHeader) request.getHeader("Call-ID")).getCallId();
            theSessionData.setLastRequest(request);
            theSessionData.setTheCallId(callid );
            if (relatedTransactionNb != null && !"".equals(relatedTransactionNb) && !"None".equals(relatedTransactionNb)) {
           	
            } else {
            	theSessionData.setTheDialog(ct.getDialog());
            }
            theSessionData.setPreviousTransaction(transactionNb);
            storeSessionData();

            // set sampler result
            res.setResponseMessage(request.toString());
            res.setSuccessful(true);
        } catch (Exception ex) {
            _logger.error("Send message in dialog exception : {} : {}", theSessionData.getTheCallId() , ex);
            res.setResponseMessage("Send message in dialog exception : " + ex.toString());
            res.setSuccessful(false);
        }
    }

    private void sendCancelRequest(SampleResult res, String command) {
    	if (_logger.isDebugEnabled()) _logger.debug("sendCancelRequest");

        try {

            // Get the client transaction.
            ClientTransaction inviteTid ;
            if (relatedTransactionNb != null && !"".equals(relatedTransactionNb) && !"None".equals(relatedTransactionNb) && theSessionData.getATransaction(relatedTransactionNb) != null) {
                inviteTid = (ClientTransaction)theSessionData.getATransaction(relatedTransactionNb).getTheTransaction();
            } else {
                inviteTid = (ClientTransaction)theSessionData.getATransaction(theSessionData.getPreviousTransaction()).getTheTransaction();
            }

            // Create the request
            Request cancelRequest = inviteTid.createCancel();

            // CSeq header
            String cseqString = extractOneValue(headersMap.removeHeaders("CSeq"));
            CSeqHeader cSeqHeader = null;
            if (cseqString != null) {
                String[] cseqparts = GuiHeadersParser.decodeCseq(cseqString);
                if (cseqparts.length == 2) {
                    cSeqHeader = headerFactory.createCSeqHeader(Long.parseLong(cseqparts[0]),
                            cseqparts[1]);        			
                } else {
                	if (_logger.isDebugEnabled()) _logger.debug("Cseq header not set. Badly decoded");
                }
            } else {
            	if (_logger.isDebugEnabled()) _logger.debug("Cseq header not set. No value");
            }
            if (cSeqHeader != null) {
                cancelRequest.setHeader(cSeqHeader);
            }

            // To header
            String toString = extractOneValue(headersMap.removeHeaders("To"));
            if (toString != null && !"".equals(toString)) {
                ToHeader toH = createToHeader(toString);
                // cancelRequest.removeHeader("To");
                cancelRequest.setHeader(toH);
            }


            // Request URI
            SipURI requestURI = null;
            String reqUri = extractOneValue(headersMap.removeHeaders("Request-URI"));//headersMap.remove("Request-URI");
            if (reqUri != null) {
		if (_logger.isDebugEnabled()) _logger.debug("Request-URI header set : {}", reqUri);
                String[] req = GuiHeadersParser.decodeReqUri(reqUri);
                requestURI = addressFactory.createSipURI(req[0], req[1] );
                cancelRequest.setRequestURI(requestURI);
            }

            // Create the client transaction.
            ClientTransaction ct = sipProvider.getNewClientTransaction(cancelRequest);

            // add other headers
            addHeaders(cancelRequest);

            // send CANCEL out.
            ct.sendRequest();


            startAndSetSplitInClientTransaction(command, ct);

            /// =====
            // Store available data about the session
            SipTransactionData theTransData = theSessionData.getATransaction(transactionNb);
            theTransData.setTheTransaction(ct);
            theTransData.setLastRequest(cancelRequest);
            String callid = ((CallIdHeader) cancelRequest.getHeader("Call-ID")).getCallId();
            theSessionData.setLastRequest(cancelRequest);
            theSessionData.setTheCallId(callid );
            Dialog theDial = null;
            if (ct.getDialog() == null) {
                theDial = sipProvider.getNewDialog(ct);

            } else {
                theDial = ct.getDialog();
            }
            theSessionData.setTheDialog(theDial); //ct.getDialog());
            theSessionData.setPreviousTransaction(transactionNb);
            storeSessionData();

            /// =====

            // set sampler result
            res.setResponseMessage(cancelRequest.toString());
            res.setSuccessful(true);

        } catch (Exception ex) {
            _logger.debug("Send message exception : {}", ex);
            res.setResponseMessage("Send message exception : " + ex.toString());
            res.setSuccessful(false);
        }
    }

    private void sendAnswerMessage(SampleResult res) {
    	if (_logger.isDebugEnabled()) _logger.debug("sendAnswerMessage");

        // Get the Transaction.
        SIPServerTransactionImpl aReq = (SIPServerTransactionImpl) theSessionData.getATransaction(transactionNb).getTheTransaction();
        if (aReq == null) {
        	if (_logger.isDebugEnabled()) _logger.debug("sendAnswerMessage error : no stored SIPServerTransactionImpl");
        	
        	if (relatedTransactionNb != null && !"".equals(relatedTransactionNb)) {
        		aReq = (SIPServerTransactionImpl) theSessionData.getATransaction(relatedTransactionNb).getTheTransaction();
                if (aReq == null) {
                	if (_logger.isDebugEnabled()) _logger.debug("sendAnswerMessage error : no stored SIPServerTransactionImpl");
                    res.setResponseMessage("sendAnswerMessage error : no stored SIPServerTransactionImpl");
                    res.setSuccessful(false);
                    return;
                }
        	}
        	
        }
        
        String prevReqDialId = null;

        try {

            // Get the Request to answer.
            Request previousRequest = (Request) aReq.getRequest();
            
            if (previousRequest !=null && previousRequest.getHeader("Call-ID") != null) {
            	prevReqDialId = previousRequest.getHeader("Call-ID").toString();
            }

            // Get the Response code.
            int respCode = Integer.parseInt(getPropertyAsString("sip.message.response.code"));

            List<String> requireH = headersMap.getHeaders("Require");
            Boolean isReliable = false;
            if (requireH != null) {
                for (String string : requireH) {
                    if (string.contains("100rel")) {
                        isReliable = true;
                    }
                }
            }

            if (isReliable && respCode >= 180 && respCode < 200) {
                // Reliable response

                // Create the response
            	Dialog newDialog = sipProvider.getNewDialog(aReq);
                Response sessionProgress = newDialog.createReliableProvisionalResponse(respCode);

                // To header
                ToHeader toHeader = (ToHeader) sessionProgress.getHeader("To");
                String to = extractOneValue(headersMap.removeHeaders("To"));
                if (to != null) {
                    toHeader = createToHeader(to);
                }

                if (toHeader.getTag() == null || "".equals(toHeader.getTag())) {
                	if (newDialog.getLocalTag() != null) {
                    	toHeader.setTag(newDialog.getLocalTag());                		
                	} else {
	                	// Generate a new To tag
	                	toHeader.setTag(Utils.getInstance().generateTag());
                	}
                }
                
                sessionProgress.setHeader(toHeader);

                ArrayList<ViaHeader> viaHeaders = createViaHeader();
                if (viaHeaders != null) {
                    for (ViaHeader viaHeader : viaHeaders) {
                        sessionProgress.addHeader(viaHeader);
                    }
                }

                // Body part
                String body = getPropertyAsString("sip.body.text");
                if (body != null && !"".equals(body)) {
                    // ContentType header
                    String contType = extractOneValue(headersMap.removeHeaders("Content-Type"));
                    String ct1 = "application";
                    String ct2 = "sdp";
                    if (contType != null) {
                        ct1 = contType.substring(0, contType.indexOf("/"));
                        ct2 = contType.substring(contType.indexOf("/") + 1);
                    }	                
                    ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader(ct1, ct2);
                    sessionProgress.setContent(body.getBytes(), contentTypeHeader);
                }


                //	        	_logger.debug("RSeq header before addHeaders :" + sessionProgress.getHeader("RSeq"));

                sessionProgress.removeHeader("Require");

                RSeqHeader rseqHeader = null;
                String rseq = extractOneValue(headersMap.removeHeaders("RSeq"));
                if (rseq != null) {
                	try {
                    rseqHeader = headerFactory.createRSeqHeader(extractFirstInt(rseq));
                    sessionProgress.removeHeader("RSeq");
                    sessionProgress.setHeader(rseqHeader);
		    if (_logger.isDebugEnabled()) _logger.debug("Rseq header tag set : {}", rseq);
                	} catch (NumberFormatException nfe) {
						_logger.debug("Rseq header tag not set. Input badly formated : {}", rseq);
                	}
                }
                
                // add other headers
                addHeaders(sessionProgress);

                // send the answer out.
                // It is essential to use this API here!
                theSessionData.getTheDialog().sendReliableProvisionalResponse(sessionProgress);//(Response) sessionProgress.clone());

                // set sampler result
                res.setResponseMessage(sessionProgress.toString());
                res.setSuccessful(true);

            } else {
                // Non Reliable response

                // Create the response
                Response theResponse = messageFactory.createResponse(respCode, previousRequest);

                // To header
                ToHeader toHeader = (ToHeader) theResponse.getHeader("To");
                String to = extractOneValue(headersMap.removeHeaders("To"));
                if (to != null) {
                    String tag = SipString.extractTag(to);
                    if (tag != null) {
                        toHeader.setTag(tag);
                    } else {
                    	if (_logger.isDebugEnabled()) _logger.debug("To header tag not set.");
                    }
                }	        	
                theResponse.setHeader(toHeader);

                List<ViaHeader> viaHeaders = createViaHeader();
                if (viaHeaders != null) {
                    for (ViaHeader viaHeader : viaHeaders) {
                        theResponse.addHeader(viaHeader);
                    }
                }

                // Body part
                String body = getPropertyAsString("sip.body.text");
                if (body != null  && !"".equals(body)) {
                    // Create ContentType header
                    String contType = extractOneValue(headersMap.removeHeaders("Content-Type"));
                    String ct1 = "application";
                    String ct2 = "sdp";
                    if (contType != null) {
                        ct1 = contType.substring(0, contType.indexOf("/"));
                        ct2 = contType.substring(contType.indexOf("/") + 1);
                    }	                
                    ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader(ct1, ct2);
                    theResponse.setContent(body.getBytes(), contentTypeHeader);        		

                }

                // add other headers
                addHeaders(theResponse);

                // send the answer out.
                try {
                    aReq.sendResponse(theResponse);
                } catch (SipException e) {
		    _logger.debug("Send response message exception : {}", e);
		    _logger.debug("Send response message exception : Try to create a new transaction");
                    // try to clone the request, then create a new ServerTransaction
                    SIPRequest clonedRequest = (SIPRequest) previousRequest.clone();

                    ServerTransaction st = (ServerTransaction) clonedRequest.getTransaction(); //sipProvider.getNewServerTransaction(clonedRequest);
                    if (st == null) {
                    	st = (ServerTransaction) ((SIPRequest)previousRequest).getTransaction();
                    	if (st == null) {
                    		st = sipProvider.getNewServerTransaction(clonedRequest);
                    	}
                    }
                    Response theClonedResponse = messageFactory.createResponse(respCode, clonedRequest);

                    theClonedResponse.setHeader(toHeader);

                    if (viaHeaders != null) {
                        for (ViaHeader viaHeader : viaHeaders) {
                            theClonedResponse.addHeader(viaHeader);
                        }
                    }

                    // add other headers
                    addHeaders(theClonedResponse);

                    // send the answer out.
                	if (st.getDialog() == null) {
                		if (_logger.isDebugEnabled()) _logger.debug("st.enableRetransmissionAlerts()");
                        st.enableRetransmissionAlerts();
                	}
                    st.sendResponse(theClonedResponse);
                    // End of tests to send forked answers

                }

                // set sampler result
                res.setResponseMessage(theResponse.toString());
                res.setSuccessful(true);
            }
        } catch (Exception ex) {
        	String dialId = null;
        	if (aReq.getDialog() != null) {
        		dialId =  aReq.getDialog().getDialogId();
        	}
		if (_logger.isDebugEnabled()) _logger.debug("Send response message exception for {} : {} : {} : {}", new Object[]{ getName(), dialId, prevReqDialId, ex});
            res.setResponseMessage("Send response message exception for " + getName() +":" + dialId +":"+ prevReqDialId + " : " + ex.toString());
            res.setSuccessful(false);
        }
    }
    
    private int extractFirstInt (String input) {
    	String[] insplit = input.split(" ");
    	return Integer.parseInt(insplit[0]);
    }


    private void receiveMessage(SampleResult res) {

        try {
            readFactories();
        } catch (Exception ex) {
            res.setResponseMessage("Failed to read factories : " + ex.getMessage());
            res.setSuccessful(false);
        }

        // Is the message reception optional ?
        isOptional = getPropertyAsBoolean("sip.message.optional");
        ignoreRetransmission = getPropertyAsBoolean("sip.message.ignoreRetransmission");

        // Read the timeout for the message reception
        timeout = getPropertyAsLong("sip.message.timeout");
        if (timeout > 0) {
            // Do nothing keep this timeout for reception
        } else if (sipNodeContext.getSipNode().getQueueTimeout() != null) {
            try {
            	timeout = Long.parseLong(sipNodeContext.getSipNode().getQueueTimeout());
            } catch (NumberFormatException nfe) {
            	timeout = 5000L;
            }
        } 
        if (timeout == 0L) {
        	timeout = 5000L;
        }

        if (_logger.isDebugEnabled()) _logger.debug("samplerTimeout = {}", timeout);

        if (isRequest) {
            receiveRequestMessage(res);
        } else {
            receiveAnswerMessage(res);
        }
    }

    private void receiveRequestMessage(SampleResult res) {	

        try {

	    if (_logger.isDebugEnabled()) _logger.debug("receiveRequestMessage idHeader name = {}", this.sipNodeContext.getSipNode().getIdentificationHeader());
            // Get identification header
            String id = SipString.cleanString(extractOneValue(headersMap.removeHeaders(this.sipNodeContext.getSipNode().getIdentificationHeader())));
            if (_logger.isDebugEnabled()) _logger.debug("receiveRequestMessage idHeader name = {} , and value ={}", this.sipNodeContext.getSipNode().getIdentificationHeader(), id);
            id = SipString.extractAddress(id);

            
            // Get request queue
            ArrayBlockingQueue<SipRequestTransaction> reqQueue = sipNodeContext.checkRequestQueue(id + "_" + getProperty("sip.message.command").getStringValue());

            if (reqQueue == null) {
		if (_logger.isDebugEnabled()) _logger.debug("req queue not created for id {}",id );
                res.setResponseMessage("req queue not created for id "+id +".");
                res.setSuccessful(false);						
            } else {
                // Wait for a message
            	long start = dateToString();
                SipRequestTransaction aReq = reqQueue.poll(timeout, TimeUnit.MILLISECONDS);
		if (_logger.isErrorEnabled()) { long diff = dateToString() - start;
                if (diff > 5 && _logger.isWarnEnabled()) _logger.warn("reqQueue.poll for id {}_{} -> POLL took {}", id, getProperty("sip.message.command").getStringValue(), diff); }

                if ( aReq == null){
                    // No message received
			if (_logger.isDebugEnabled()) _logger.debug("incoming request for id {} not received within timeout {}", id, timeout);
                    if (isOptional) {
                        res.setResponseMessage("Optional: no message received for id "+id + " within timeout "+timeout);
                        res.setSuccessful(true);
                    } else {
                        res.setResponseMessage("failed: no message received for id "+id + " within timeout "+timeout);
                        res.setSuccessful(false);						
                    }
                    return;
                }
                else {
                    // Message received

                    // Get the request
                    Request rcvReq = aReq.getTheRequest();

                    // Get Call-ID
                    String callid = rcvReq.getHeader("Call-ID").toString().substring("Call-ID".length() + 2);
                    callid = SipString.cleanString(callid);
                    if (_logger.isDebugEnabled()) _logger.debug("incoming request callid {}.", callid);
                    
                    if ("INVITE".equals(rcvReq.getMethod())) {
//                    	if ( _logger.isWarnEnabled()) _logger.warn("RCV INVITE : " + rcvReq.getHeader("Call-ID").toString().substring("Call-ID: ".length()).replaceAll("\\r|\\n", "")+ ", From = " + rcvReq.getHeader("From").toString().replaceAll("\\r|\\n", "")+ ", From = " + rcvReq.getHeader("To").toString().replaceAll("\\r|\\n", "") + "," + LocalDateTime.now().format(formatter));
            		}

                    // Store available data about the session
                    SipTransactionData theTransData = theSessionData.getATransaction(transactionNb);
                    theTransData.setTheTransaction(aReq.getTheTransaction());
                    theTransData.setLastRequest(rcvReq);
                    theSessionData.setLastRequest(rcvReq);
                    theSessionData.setATransaction(transactionNb, theTransData);
                    theSessionData.setTheCallId(callid);
                    Dialog theDial = null;
		    if (_logger.isDebugEnabled()) _logger.debug("is Dialog exist for method {}, callid ={}", rcvReq.getMethod(), callid);
                    if (aReq.getTheTransaction() != null) {
	                    if (aReq.getTheTransaction().getDialog() == null  && DialogCreatingRequests.isCreating(rcvReq.getMethod())) {
	                    	if (_logger.isDebugEnabled()) _logger.debug("Dialog does not exist, try to create it");
	                        try {
	                            theDial = sipProvider.getNewDialog(aReq.getTheTransaction());
	                        } catch (SipException se) {
				    if (_logger.isDebugEnabled()) _logger.debug("Dialog does not exist, cannot create it : {}" , se);
	                        }					
	                    } else {
	                        theDial = aReq.getTheTransaction().getDialog();
	                    }
                    }
                    theSessionData.setTheDialog(theDial);//aReq.getTheTransaction().getDialog());
                    storeSessionData();

                    // Check received headers
                    checkReceivedRequest(aReq.getTheRequest(),res);
                }
            }
        } catch (Exception e) {
            if (_logger.isDebugEnabled()) _logger.debug("receiveRequestMessage exception : {}", e);
            res.setResponseMessage("receiveRequestMessage exception : " + e.toString());
            res.setSuccessful(false);
        }

    }

    private void receiveAnswerMessage(SampleResult res) {	

        try {

            // Get identification header
            String id = SipString.cleanString(extractOneValue(headersMap.removeHeaders(sipNodeContext.getSipNode().getIdentificationHeader())));	
            String callidStored = theSessionData.getTheCallId();
            if (callidStored != null && !"".equals(callidStored)) {
                id = callidStored;
            }
            if (_logger.isDebugEnabled()) _logger.debug("resp queue for id {}.", id);
            
            // Forking case: if relatedTransactionNb != null
            if (relatedTransactionNb != null && !"".equals(relatedTransactionNb) && !"None".equals(relatedTransactionNb)) {
		if (_logger.isDebugEnabled()) _logger.debug("receiveAnswerMessage with relatedTransactionNb =  {}.", relatedTransactionNb);
            	try {
            	theSessionData.setADialog(relatedTransactionNb, sipProvider.getNewDialog(theSessionData.getATransaction(relatedTransactionNb).getTheTransaction()));
            	} catch (Throwable t) {
			if (_logger.isDebugEnabled()) _logger.debug("receiveAnswerMessage with relatedTransactionNb = {}  throwable {}", relatedTransactionNb, t.getMessage());
            	}
            }            

            String method = theSessionData.getATransaction(transactionNb).getLastRequest().getMethod();//getTheTransaction().getRequest().getMethod();
            // Get response queue
            ArrayBlockingQueue<SipResponseTransaction> respQueue = sipNodeContext.checkResponseQueue(id+"_" + getProperty("sip.message.response.code").getStringValue() + "_" + method);


            if (respQueue == null) {
		if (_logger.isDebugEnabled()) _logger.debug("resp queue not created for id {}.", id);
                res.setResponseMessage("resp queue not created for id "+id +".");
                res.setSuccessful(false);						
            } else {
            	boolean retry = true;
            	
            	while (retry) {
            		retry = false;
                // Wait for a message
                SipResponseTransaction aResp = respQueue.poll(timeout, TimeUnit.MILLISECONDS);
                if ( aResp == null){
                    // No message received
                    if (_logger.isDebugEnabled()) _logger.debug("incoming response for id {} not received within timeout {}", id, timeout);
                    if (isOptional) {
                        res.setResponseMessage("Optional: no response message received for id "+id + " within timeout "+timeout);
                        res.setSuccessful(true);
                    } else {
                        res.setResponseMessage("failed: no response message received for id "+id + " within timeout "+timeout);
                        res.setSuccessful(false);						
                    }
                }
                else {
                    // Message received                	
			if (_logger.isDebugEnabled()) _logger.debug("receiveAnswerMessage: poll in responsesQeueues for id = {}. remaining queue size = {}", id, respQueue.size());

                    // Get the answer
                    Response rcvResp = aResp.getTheResponse();
                    if (aResp.getTheResponse()==null) {
                    	if (_logger.isDebugEnabled()) _logger.debug("No Response got for SipResponseTransaction");
                    }
                    
                    // Get Call-ID
                    CallIdHeader cidh = (CallIdHeader) rcvResp.getHeader("Call-ID");
                    String callid = SipString.cleanString(rcvResp.getHeader("Call-ID").toString().substring("Call-ID".length() + 2));

                    // Store available data about the session
                    SipTransactionData theTransData = theSessionData.getATransaction(transactionNb);
                    
                    if (ignoreRetransmission && isAlreadyReceived(rcvResp, theTransData.getLastResponse())) {
                    	retry = true;
                    } else {

                    //			        if (theTransData.getLastResponse() == null || theTransData.getLastResponse().getStatusCode() != rcvResp.getStatusCode()) {
                    if (aResp.getTheTransaction() != null) {
                    	theTransData.setTheTransaction(aResp.getTheTransaction());
                    }
                    theTransData.setLastResponse(rcvResp);
                    if (aResp.getTheTransaction()==null) {
                    	if (_logger.isDebugEnabled()) _logger.debug("No transaction got for SipResponseTransaction");
                    } else {
                        theSessionData.setTheDialog(aResp.getTheTransaction().getDialog());
                    }
                    if (_logger.isDebugEnabled()) _logger.debug("setTheCallId with {}", callid);
                    theSessionData.setTheCallId(callid);
                    CSeqHeader cseqH = (CSeqHeader) aResp.getTheResponse().getHeader("CSeq");
                    if (!Request.PRACK.equals( cseqH.getMethod())) {	//	aResp.getTheTransaction().getMethod())) {
			if (_logger.isDebugEnabled()) _logger.debug("setLastResponse for method {}", cseqH.getMethod() ); 	//aResp.getTheTransaction().getMethod());
                        theSessionData.setLastResponse(rcvResp);
                    }
                    storeSessionData();

                    // Check received headers
                    checkReceivedResponse(aResp.getTheResponse(),res);
                    }
                }
            }
            }
        } catch (Exception e) {
            _logger.debug("receiveAnswerMessage exception : {}", e);
            res.setResponseMessage("receiveAnswerMessage exception : " + e.toString());
            res.setSuccessful(false);
        }
    }
    
    private boolean isAlreadyReceived(Response rcvResp, Response lastStoredResp) {
	    	if (lastStoredResp != null) {
				if (lastStoredResp.toString().equals(rcvResp.toString())) {
					if (_logger.isDebugEnabled()) _logger.debug("response already received: should be retransmission {} : {}", lastStoredResp.toString(), rcvResp.toString());
					return true;
				} else {
					if (_logger.isDebugEnabled()) _logger.debug("response not already received: it seems no to be a retransmission {} : {}", lastStoredResp.toString(), rcvResp.toString());
					return false;
				}
			} else {
				if (_logger.isDebugEnabled()) _logger.debug("No lastStored response. response received: {}", rcvResp.toString());
			}
    	return false;
    }

    private void checkReceivedRequest(Request aReq, SampleResult res) {
    	if (_logger.isDebugEnabled()) _logger.debug("checkReceivedRequest ");
        String diff = "";

        // Check command name
        String expectedCmd = getProperty("sip.message.command").getStringValue();
        res.setSuccessful(true);
        Request rcvReq = aReq;
        res.setResponseMessage(rcvReq.toString());
        if (!aReq.getMethod().equals(expectedCmd)) {
            diff = diff + "Method check failed: " + expectedCmd +":" + aReq.getMethod() + ".\n";
            if (_logger.isDebugEnabled()) _logger.debug("checkReceivedMessage  Method check failed: {} : {}.", expectedCmd, aReq.getMethod());
            res.setSuccessful(false);
        }

        diff = checkHeaders(rcvReq, diff);

        // Is checking successful ?
        if ("".equals(diff)) {
            res.setResponseMessage("Success. received message : " + rcvReq.toString());        	
        } else {
            res.setResponseMessage("Failure. differences : \n" + diff);        	
            res.setSuccessful(false);
        }
    }


    private void checkReceivedResponse(Response rcvResp, SampleResult res) {
        if (_logger.isDebugEnabled()) _logger.debug("checkReceivedResponse");
        String diff = "";
        String expectedRespCode = getProperty("sip.message.response.code").getStringValue();
        res.setSuccessful(true);
        //    	Response rcvResp = aResp.getResponse();
        res.setResponseMessage(rcvResp.toString());
        if (rcvResp.getStatusCode() != Integer.parseInt(expectedRespCode)) {
            diff = diff + "Response code check failed: " + expectedRespCode +":" + rcvResp.getStatusCode() + ".\n";
            if (_logger.isDebugEnabled()) _logger.debug("checkReceivedResponse Response code check failed: {} : {}.", expectedRespCode, rcvResp.getStatusCode());
        }

        diff = checkHeaders(rcvResp, diff);

        if ("".equals(diff)) {
            res.setResponseMessage("Success. received response : " + rcvResp.toString());        	
        } else {
            res.setResponseMessage("Failure. differences : \n" + diff);        	
            res.setSuccessful(false);
        }

    }


    private String checkHeaders (Message rcvMsg, String diff) {
        resetHeaderMap();
        // Check headers
        for (java.util.Map.Entry<String, List<String>> entry : headersMap.entrySet())
        {
	    if (_logger.isDebugEnabled()) _logger.debug("checkReceivedMessage for key {} : {}", entry.getKey(), entry.getValue());


            if (entry.getValue() != null && entry.getValue().size() > 0) {

                if ("Request-URI".equals(entry.getKey())) {
                    if (rcvMsg instanceof Request) {
                        if (entry.getValue().size() != 1) {
                            // Error : two different values
                            diff = diff + entry.getKey() + " check failed: Request-URI has 1 value, not " + entry.getValue().size() + ".\n";
                            if (_logger.isDebugEnabled()) _logger.debug("checkReceivedMessage {} check failed: Request-URI has 1 value, not {}.",entry.getKey(),entry.getValue().size() );
                        } else {
                            String reqURIrcv = ((Request)rcvMsg).getRequestURI().toString();
                            String compVal = entry.getValue().get(0);
                            if (compVal.startsWith("STORE:")) {
                                // Store one value
                                storeVar(compVal, reqURIrcv ); //aH);
                            } else if (compVal.startsWith("PATTERN:")) {
                                // Check one value with a pattern
                                String eV = entry.getValue().get(0); //SipString.cleanString(entry.getKey() +":" + eVL.get(i));
                                String aH = SipString.cleanString(reqURIrcv);
                                if (!checkOneHeaderWithPattern(aH, eV)) { //eV.equals(aH)) {
                                    // Error : value does not match the pattern
                                    diff = diff + entry.getKey() + " check with pattern failed: " + eV +":" + aH + ".\n";
                                    if (_logger.isDebugEnabled()) _logger.debug("checkReceivedMessage {} check with pattern failed: {} : {}.",entry.getKey(), eV, aH );
                                }
                            } else  {
                                if (!checkOneHeader(reqURIrcv, compVal)) { //eV.equals(aH)) {
                                    // Error : two different values
                                    diff = diff + entry.getKey() + " check failed: " + reqURIrcv +":" + compVal + ".\n";
                                    if (_logger.isDebugEnabled()) _logger.debug("checkReceivedMessage {} check failed: {} : {}.", entry.getKey(), reqURIrcv, compVal);
                                }       
                            }
                        }
                    } else {
                        // The received message is not a request
                        diff = diff + entry.getKey() + " check failed: received message not a request. It has no Request-URI.\n";
                        if (_logger.isDebugEnabled()) _logger.debug("checkReceivedMessage {} check failed: not present in message. It has no Request-URI.", entry.getKey() );
                    }
                } else {	
                    // Check values for a header name
                    List<String> eVL = entry.getValue(); // List of values from the scenario
                    int i=0;

                    if (! rcvMsg.getHeaders(entry.getKey()).hasNext()) {
                        // No header in request
                        if ( ! ( eVL.size() == 1 && eVL.get(0) != null && eVL.get(0).startsWith("NODEF:") ))
                        {
                            // size of eVL>=1 or eVL[0] is NODEF:
                           
                            diff = diff + entry.getKey() + " check failed: not present in message.\n";
                            if (_logger.isDebugEnabled()) _logger.debug("checkReceivedMessage {} check failed: not present in message.",  entry.getKey() );  
                        }
                    } else {
                        for (Iterator<Header> iterator = rcvMsg.getHeaders(entry.getKey()); iterator.hasNext();) { // List of values from the Request

                            // Check scenario list is not empty
                            if (i >= eVL.size()) {
                                String valRcv = iterator.next().toString(); // to consum the data from the list

                            } else {

                                String eVLi = eVL.get(i);
                                String valRcv ;
                                valRcv = iterator.next().toString(); // to consum the data from the list
                                String aH = SipString.cleanString(valRcv);
                                if (eVLi.startsWith("STORE:")) {
                                    // Store one value
                                    storeVar(eVLi, valRcv ); //aH);
                                } else if (eVLi.startsWith("PATTERN:")) {
                                    // Check one value with a pattern
                                    String eV = eVL.get(i); //SipString.cleanString(entry.getKey() +":" + eVL.get(i));
                                    if (!checkOneHeaderWithPattern(aH, eV)) { //eV.equals(aH)) {
                                        // Error : value does not match the pattern
                                        diff = diff + entry.getKey() + " check with pattern failed: " + eV +":" + aH + ".\n";
                                        if (_logger.isDebugEnabled()) _logger.debug("checkReceivedMessage {}  check with pattern failed: {} : {}.", entry.getKey(),eV, aH );
                                    }
                                }

                                else {
                                    // Check one value
                                    if ( ! eVLi.startsWith("NODEF:"))
                                    {
                                        String eV = SipString.cleanString(entry.getKey() +":" + eVL.get(i));
                                        if (!checkOneHeader(aH, eV)) { //eV.equals(aH)) {
                                            // Error : two different values
                                            diff = diff + entry.getKey() + " check failed: " + eV +":" + aH + ".\n";
                                            if (_logger.isDebugEnabled()) _logger.debug("checkReceivedMessage {} check failed: {} : {}.", entry.getKey(), eV, aH );
                                        } 
                                    }
                                }
                                i++;
                            }

                        }

                        if (i < eVL.size()) {
                            // Error : more values to check remain
                            diff = diff + entry.getKey() + " check failed: more values to check remain : " + (eVL.size() - i) + ".\n";
                        }
                    } 
                }

            }
        }

        // Check body
        String bodyValueToCheck = getPropertyAsString("sip.body.text").replaceAll("[\n\r]", "");
        if (bodyValueToCheck != null && !"".equals(bodyValueToCheck)) {
            String bodyToValidate = null;
            if (rcvMsg != null && rcvMsg.getContent() != null) {
                try {
                    bodyToValidate = new String(rcvMsg.getRawContent(), CharEncoding.UTF_8);	//StandardCharsets.UTF_8);
                } catch (UnsupportedEncodingException e) {
			if (_logger.isDebugEnabled()) _logger.debug("checkHeaders: body decoding exception : {}", e);
                }
            }
            //replaceAll("[\n\r]", "");

            if (bodyValueToCheck.startsWith("STORE:")) {
                // Store one value
                storeVar(bodyValueToCheck, bodyToValidate );
            } else if (bodyValueToCheck.startsWith("PATTERN:")) {
                // Check one value with a pattern
                if (!checkOneHeaderWithPattern(bodyToValidate, bodyValueToCheck)) { //eV.equals(aH)) {
                    // Error : value does not match the pattern
                    diff = diff + " body check with pattern failed: " + bodyValueToCheck +":" + bodyToValidate + ".\n";
                    if (_logger.isDebugEnabled()) _logger.debug("checkReceivedMessage body check with pattern failed: {} : {}.", bodyValueToCheck, bodyToValidate);
                }
            } else {        	
                if (!bodyValueToCheck.replaceAll("[\n\r]", "").equals(bodyToValidate.replaceAll("[\n\r]", ""))){
                    diff = diff + "Body check failed: " + bodyValueToCheck +":" + bodyToValidate + ".\n";
                }
            }
        }        

        return diff;
    }

    private void storeVar(String eVLi, String aH) {
        String varName = eVLi.substring("STORE:".length());
        if (varName!= null && !"".equals(varName)) {
            JMeterVariables variables = getThreadContext().getVariables();
            variables.put(varName, SipString.getHeaderValue(aH).replaceAll("(\\r|\\n)", ""));
        }
    }

    private boolean checkOneHeader (String receivedValue, String comparisonValue) {
        String tagrcv = SipString.extractTag(receivedValue);
        String tagcomp = SipString.extractTag(comparisonValue);
        String valrcv = SipString.removeTag2(receivedValue, ";tag="+tagrcv);
        String valcomp = SipString.removeTag2(comparisonValue, ";tag="+tagcomp);
        if (_logger.isDebugEnabled()) _logger.debug("checkOneHeader {} : {} : {} : {}", new String[]{tagrcv, tagcomp, valrcv, valcomp});
        if (tagcomp != null && !tagcomp.equals(tagrcv)) {
            return false;
        }
        if (valcomp != null && !valcomp.equals(valrcv)) {
            return false;
        }
        return true;
    }

    private boolean checkOneHeaderWithPattern (String receivedValue, String pattern) {
        /* 
         * Pattern syntax definition
         * https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
         */
	if (_logger.isDebugEnabled()) _logger.debug("checkOneHeaderWithPattern {} with pattern : {}", receivedValue, pattern);
        String extractedPattern = pattern.substring("PATTERN:".length());
        if (extractedPattern == null || "".equals(extractedPattern)) {
            return false;
        }
        return Pattern.matches(extractedPattern, receivedValue);
    }

    private String extractOneValue(List<String> values) {
        if (values == null || values.size() > 1) {
            return null;
        }
        return values.get(0);
    }

    private ArrayList<ViaHeader> createViaHeader () throws ParseException, InvalidArgumentException {
        // Via headers
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        String transport = sipNodeContext.getSipNode().getLocalTransport();
        String ipAddress = udpListeningPoint.getIPAddress();
        int ipPort = sipProvider.getListeningPoint(transport ).getPort();
        String branch = null;
        List<String> viaString = headersMap.removeHeaders("Via");
        if (viaString == null || viaString.size() == 0) {
            return null;
        }
        for (String oneViaString : viaString) {
            if (oneViaString != null && !"".equals(oneViaString)) {
            	
                String[] via = GuiHeadersParser.decodeVia(oneViaString);
                if (via != null) {
                    _logger.debug("createViaHeader via  {} : {}", via[0], via[1]);
                    ipAddress = via[0];
                    ipPort = Integer.parseInt(via[1]);
                    if (via.length == 3) {
                        branch = via[2];
                    }
                    if (_logger.isDebugEnabled()) _logger.debug("createViaHeader add {} : {} : {} : {}", new Object[]{ ipAddress, ipPort, transport, branch});
                    ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress, ipPort, transport, branch);
                    
                    viaHeaders.add(viaHeader);
                }
                
            }
        }
        return viaHeaders;
    }

    private ArrayList<RouteHeader> createRouteHeader () throws ParseException, InvalidArgumentException {
        // Via headers
        ArrayList<RouteHeader> routeHeaders = new ArrayList<RouteHeader>();
        List<String> routeString = headersMap.removeHeaders("Route");
        if (routeString == null || routeString.size() == 0) {
            return null;
        }
        for (String oneRouteString : routeString) {
            if (oneRouteString != null && !"".equals(oneRouteString)) {
            	
                    _logger.debug("createRouteHeader one route {}", oneRouteString);
                    if (_logger.isDebugEnabled()) _logger.debug("createRouteHeader add {}", oneRouteString);
                    RouteHeader routeHeader = headerFactory.createRouteHeader(addressFactory.createAddress(oneRouteString));
                    
                    routeHeaders.add(routeHeader);
            }
        }
        return routeHeaders;
    }

    public ToHeader createToHeader (String toString) {
        ToHeader toHeader = null;

        if (toString != null) {
            Address address;
            try {
                String extTo = SipString.extractAddress(toString);
                if (_logger.isDebugEnabled()) _logger.debug("createToHeader extTo = {}", extTo);
                address = addressFactory.createAddress(extTo);
                toHeader = headerFactory.createToHeader(address, null);
                String[] params = SipString.extractParams(toString.substring(extTo.length()));
                if (params != null && params.length != 0) {
                    for (String oneParam : params) {
                        String var = SipString.extractNameFromParam(oneParam);
                        String val = SipString.extractValueFromParam(oneParam);
                        if (_logger.isDebugEnabled()) _logger.debug("createToHeader oneParam = {} : {}", var, val);

                        if ("tag".equals(var)) {
                            toHeader.setTag(val);
                        } else if (var != null && !"".equals(var)) {
                            toHeader.setParameter(var, val);
                        }
                    }
                }

            } catch (ParseException e) {
		if (_logger.isDebugEnabled()) _logger.debug("ParseException when creating To header : {}", e);
            }
        } else {
        	if (_logger.isDebugEnabled()) _logger.debug("To is null...");
        }
        return toHeader;

    }

    public FromHeader createFromHeader (String fromString) {
        FromHeader fromHeader = null;

        if (fromString != null) {
            Address address;
            try {
                String extFrom = SipString.extractAddress(fromString);
                address = addressFactory.createAddress(extFrom);
                fromHeader = headerFactory.createFromHeader(address, null);
                String[] params = SipString.extractParams(fromString.substring(extFrom.length()));
                if (params != null && params.length != 0) {
                    for (String oneParam : params) {
                        String var = SipString.extractNameFromParam(oneParam);
                        String val = SipString.extractValueFromParam(oneParam);

                        if ("tag".equals(var)) {
                            fromHeader.setTag(val);
                        } else if (var != null && !"".equals(var)) {
                            fromHeader.setParameter(var, val);
                        }
                    }
                }

            } catch (ParseException e) {
		if (_logger.isDebugEnabled()) _logger.debug("ParseException when creating From header : {}", e);
            }
        } else {
        	if (_logger.isDebugEnabled()) _logger.debug("From is null...");
        }
        return fromHeader;

    }

    public SipURI createReqURI (String reqUri) {
        SipURI requestURI = null;

        if (reqUri != null) {
            String[] req = GuiHeadersParser.decodeReqUri(reqUri);
            if (req != null && req.length == 2) {
                try {
                    requestURI = addressFactory.createSipURI(req[0], req[1] );
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
            	if (_logger.isDebugEnabled()) _logger.debug("Request-URI badly formated.");
            }
        } else {
        	if (_logger.isDebugEnabled()) _logger.debug("Request-URI header not set.");
        }
        return requestURI;
    }

    public URI createReqURITelOrSip (String reqUri) {
        URI requestURI = null;

        if (reqUri != null) {
            if (reqUri.startsWith("sip:") || reqUri.startsWith("sips:")) {
                return createReqURI(reqUri);
            } else if (reqUri.startsWith("tel:")) {
                try {
                    requestURI = addressFactory.createURI(reqUri);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }	  			
            } else {
            	if (_logger.isDebugEnabled()) _logger.debug("Request-URI type not supported.");	  			
            }
        } else {
        	if (_logger.isDebugEnabled()) _logger.debug("Request-URI header not set.");
        }
        return requestURI;
    }


    private void startAndSetSplitInClientTransaction(String command, ClientTransaction theTrans) {
        // create a Simon split for responsetime statisticts. Put it in the session application data
        //// Split split = SimonManager.getStopwatch(SipCounters.SIP_NODE_RESPONSE_TIME+"."+command+"."+sipNodeContext.getSipNode().getSipNodeName().replace(".", "_")).start();
//        theTrans.setApplicationData(sipNodeContext.getSplitAndStart(SipCounters.SIP_NODE_RESPONSE_TIME+"."+command+"."+sipNodeContext.getSipNode().getSipNodeName().replace(".", "_"))); //theSessionData.setResponseTimeSplit(split);
/*        JMeterVariables variables = getThreadContext().getVariables();
        StatisticsManager statManager = (StatisticsManager) variables.getObject("StatisticsManager");
        if (statManager != null) {
	        statManager.createAndStartSplit(SipCounters.SIP_NODE_RESPONSE_TIME+"."+command+"."+sipNodeContext.getSipNode().getSipNodeName().replace(".", "_"), theTrans.getDialog().getDialogId());
        } else {
        	_logger.error("No StatisticsManager available");
        }	        
*/
    }

	  public String nextTag() {
		  return new BigInteger(50, random).toString(32);
	  }
    
}
