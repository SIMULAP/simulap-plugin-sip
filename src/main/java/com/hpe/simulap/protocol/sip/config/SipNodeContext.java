//Â© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package com.hpe.simulap.protocol.sip.config;

import gov.nist.javax.sip.ResponseEventExt;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.stack.MessageProcessor;
import gov.nist.javax.sip.stack.SIPClientTransactionImpl;
import gov.nist.javax.sip.stack.SIPServerTransactionImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TimerTask;
import java.util.TooManyListenersException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransportNotSupportedException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RSeqHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.javasimon.SimonManager;

import com.hpe.simulap.protocol.sip.performance.SimpleCallWithPrack;
import com.hpe.simulap.protocol.sip.sampler.GuiHeadersParser;
import com.hpe.simulap.protocol.sip.sampler.SipDialogData;
import com.hpe.simulap.protocol.sip.utils.SipCounters;
import com.hpe.simulap.protocol.sip.utils.SipServerLogger;
import com.hpe.simulap.protocol.sip.utils.SipStackLogger;
import com.hpe.simulap.protocol.sip.utils.SipString;


public class SipNodeContext implements SipListener {

	private SipNodeElement sipNode = null;
	private static final Logger _logger = LoggerFactory.getLogger(SipNodeContext.class);
	private boolean isNodeReady = false;
	private boolean isNonReliableResponseRetransmissionIgnored = true ;
	private boolean isReliableResponseRetransmissionIgnored = true ;
	private boolean isProxyEnabled = false;
	private String notReadyReason = null;
	private SipStack sipStack = null;
	private SipListener thisSipListener = null;
	private ListeningPoint sipListening = null;
	private ThreadPoolExecutor threadsPool = null;
	private SipProvider sipProvider = null;
	private SipFactory sipFactory = null;
	private HeaderFactory headerFactory = null;
	private AddressFactory addressFactory = null;
	private MessageFactory messageFactory = null;
	private Map<String, ArrayBlockingQueue<SipRequestTransaction>> msgRequestsQueues = new ConcurrentHashMap<String, ArrayBlockingQueue<SipRequestTransaction>>();
	private Map<String, ArrayBlockingQueue<SipResponseTransaction>> msgResponsesQueues = new ConcurrentHashMap<String, ArrayBlockingQueue<SipResponseTransaction>>();
	private List<StringLong> msgRequestsIdList = new ArrayList<StringLong>();
	private List<StringLong> msgResponsesIdList = new ArrayList<StringLong>();

	private Map<String, SipDialogData> dialogList = new ConcurrentHashMap<String, SipDialogData>();
	private List<StringLong> dialogsIdList = new ArrayList<StringLong>();

	private Map<String, Request> dialogListPerf = new ConcurrentHashMap<String, Request>();
	private SipListener perfListener = null;


	private int queueSize = 100;
	private ScheduledThreadPoolExecutor executor = null;
	private long cleanPeriod = 5000;
	private long cleanDialogPeriod = 5000;
	private boolean isAutomaticCleanup = false;

	public class StringLong {
		private String aString;
		private long aLong;
		public StringLong(String aString, long aLong) {
			super();
			this.aString = aString;
			this.aLong = aLong;
		}
		public String getaString() {
			return aString;
		}
		public void setaString(String aString) {
			this.aString = aString;
		}
		public long getaLong() {
			return aLong;
		}
		public void setaLong(long aLong) {
			this.aLong = aLong;
		}
	}

	private void printQueueSize() {
		_logger.debug("Queues size: Req = {} , Res = {} . Id List size: Req = {} , Res = {} . DialList = {} , DialIdList = {}", msgRequestsQueues.size(), msgResponsesQueues.size(),
				msgRequestsIdList.size(), msgResponsesIdList.size(), dialogList.size(), dialogsIdList.size());
	}

		private void printQueueTime() {
		_logger.debug("msgRequestsQueues size = {}", msgRequestsQueues.size());
		try {
		_logger.debug("msgRequestsIdList size = {}", msgRequestsIdList.size());
		if (msgRequestsIdList.size() >0) {
		for (StringLong stringLong : msgRequestsIdList) {
			_logger.debug("msgRequestsIdList elem : {} : {}", stringLong.getaString(), stringLong.getaLong());
		}
		}
		_logger.debug("msgResponsesIdList size = {}", msgResponsesIdList.size());
		if (msgResponsesIdList.size() > 0) {
		for (StringLong stringLong : msgResponsesIdList) {
			_logger.debug("msgResponsesIdList elem : {} : {} ", stringLong.getaString(), stringLong.getaLong());
		}
		}
		} catch (Throwable t) {
			_logger.error("printQueueSize exception {}", t);
		}
	}

	public SipNodeContext(SipNodeElement sipNode) {
		super();
		_logger.debug("SipNodeContext(SipNodeElement) for node name {}", sipNode.getSipNodeName());

		testPortStatus(sipNode.getSipNodeName() + " creation" , Integer.parseInt(sipNode.getLocalPort()));

		sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");

		this.sipNode = sipNode;

		Properties prop = new Properties();
		prop.put("javax.sip.STACK_NAME", sipNode.getSipNodeName());
		if (sipNode.getOutboundProxy() != null && !"".equals(sipNode.getOutboundProxy()) && !"Undefined".equals(sipNode.getOutboundProxy())) {
			prop.put("javax.sip.OUTBOUND_PROXY", sipNode.getOutboundProxy());
			prop.setProperty("javax.sip.ROUTER_PATH", SipRouterProxy.class.getName());
			isProxyEnabled = true;
		}
		prop.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", System.getProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT","off"));
		prop.setProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
		prop.setProperty("gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE",  "DisabledAll");
		prop.setProperty("gov.nist.javax.sip.DELIVER_RETRANSMITTED_ACK_TO_LISTENER", "false");

		//		prop.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY",  "gov.nist.javax.sip.stack.NioMessageProcessorFactory");

		// Set SIP stack logging properties
		String sipStackLogLevel = System.getProperty("gov.nist.javax.sip.TRACE_LEVEL","NONE");
		if (!"NONE".equals(sipNode.getStackLog())) {
			_logger.debug("SipNodeContext(SipNodeElement) SIP stack logging enabled : {}", sipNode.getDebuglevel());
			prop.setProperty("gov.nist.javax.sip.STACK_LOGGER", SipStackLogger.class.getName());
			prop.setProperty("gov.nist.javax.sip.SERVER_LOGGER", SipServerLogger.class.getName());
			if (sipNode.getDebuglevel() != null) {
				prop.setProperty("gov.nist.javax.sip.TRACE_LEVEL", sipNode.getDebuglevel());
			} else {
				prop.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "ERROR");
			}
		} if (!"NONE".equals(sipStackLogLevel)){
			_logger.debug("SipNodeContext(SipNodeElement) SIP stack logging enabled by property : {}", sipStackLogLevel);
			prop.setProperty("gov.nist.javax.sip.STACK_LOGGER", SipStackLogger.class.getName());
			prop.setProperty("gov.nist.javax.sip.SERVER_LOGGER", SipServerLogger.class.getName());
			prop.setProperty("gov.nist.javax.sip.TRACE_LEVEL", sipStackLogLevel);
		} else {
			_logger.debug("SipNodeContext(SipNodeElement) SIP stack logging disabled");
		}

		String enableGlobalCaching = System.getProperty("sip.enableGlobalCaching","NOGLOBALCACHING");
		boolean globalCachingEnabled =  "true".equals(System.getProperty("sip.enableGlobalCaching","false"));

		prop.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", System.getProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS","true"));
		prop.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", System.getProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS","true"));
		prop.setProperty("gov.nist.javax.sip.TCP_POST_PARSING_THREAD_POOL_SIZE", System.getProperty("gov.nist.javax.sip.TCP_POST_PARSING_THREAD_POOL_SIZE","0"));


		//        prop.setProperty("javax.sip.FORKABLE_EVENTS", "dialog");`
		prop.setProperty("javax.sip.RETRANSMISSION_FILTER", "OFF");

		prop.setProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "true");
		prop.setProperty("gov.nist.javax.sip.AUTOMATIC_DIALOG_ERROR_HANDLING","false");
		prop.setProperty("gov.nist.javax.sip.TCP_NODELAY", System.getProperty("gov.nist.javax.sip.TCP_NODELAY","false"));
		prop.setProperty("gov.nist.javax.sip.TCP_RESET", System.getProperty("gov.nist.javax.sip.TCP_RESET","false"));
		prop.setProperty("gov.nist.javax.sip.sctp.message.ENABLE_ORDERED_DELIVERY", System.getProperty("gov.nist.javax.sip.sctp.message.ENABLE_ORDERED_DELIVERY","false"));
		prop.setProperty("gov.nist.javax.sip.sctp.SCTP_NODELAY", System.getProperty("gov.nist.javax.sip.sctp.SCTP_NODELAY","false"));
		prop.setProperty("gov.nist.javax.sip.AGGRESSIVE_CLEANUP", System.getProperty("gov.nist.javax.sip.AGGRESSIVE_CLEANUP","false"));
		prop.setProperty("gov.nist.javax.sip.MAX_TX_LIFETIME_INVITE", System.getProperty("gov.nist.javax.sip.MAX_TX_LIFETIME_INVITE","180"));
		prop.setProperty("gov.nist.javax.sip.MAX_TX_LIFETIME_NON_INVITE", System.getProperty("gov.nist.javax.sip.MAX_TX_LIFETIME_NON_INVITE","30"));
		prop.setProperty("gov.nist.javax.sip.NIO_MAX_SOCKET_IDLE_TIME", System.getProperty("gov.nist.javax.sip.NIO_MAX_SOCKET_IDLE_TIME","120000"));
		if ( !"infinity".equals(System.getProperty("gov.nist.javax.sip.THREAD_POOL_SIZE","infinity"))) {
			prop.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE",System.getProperty("gov.nist.javax.sip.THREAD_POOL_SIZE","infinity"));
		}
		prop.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER",System.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER","false"));
		prop.setProperty("gov.nist.javax.sip.MAX_CONNECTIONS", sipNode.getMaxConnections());

		isNonReliableResponseRetransmissionIgnored = !(SipNodeElement.NO.equals(sipNode.getIgnoreNonReliableResponseRetransmission())); //(sipNode.getIgnoreNonReliableResponseRetransmission() == SipNodeElement.YES);
		isReliableResponseRetransmissionIgnored = !(SipNodeElement.NO.equals(sipNode.getIgnoreReliableResponseRetransmission()));
		_logger.info("sipNode.getAutomaticQueuesCleanup() = {}", sipNode.getAutomaticQueuesCleanup());
		isAutomaticCleanup = (SipNodeElement.YES.equals(sipNode.getAutomaticQueuesCleanup()));
		//_logger.info("isAutomaticCleanup = " + isAutomaticCleanup);

		isNodeReady = false;
		System.clearProperty("javax.net.ssl.trustStore");
		System.clearProperty("javax.net.ssl.trustStorePassword");

		String ts = System.getProperty("javax.net.ssl.trustStore","NOTHING");
		String tsp = System.getProperty("javax.net.ssl.trustStorePassword","NOTHING");
		if (ts == null || "".equals(ts) || tsp == null || "".equals(tsp)) {
			System.clearProperty("javax.net.ssl.trustStore");
			System.clearProperty("javax.net.ssl.trustStorePassword");
		}
		_logger.debug("javax.net.ssl.trustStoreType {}", System.getProperty("javax.net.ssl.trustStoreType","NOTHING"));
		_logger.debug("javax.net.ssl.trustStore {}", System.getProperty("javax.net.ssl.trustStore","NOTHING"));
		_logger.debug("javax.net.ssl.trustStorePassword {}", System.getProperty("javax.net.ssl.trustStorePassword","NOTHING"));

		try {
			sipStack = sipFactory.createSipStack(prop);
		} catch (PeerUnavailableException e) {
			_logger.error("createSipStack FAILED for {} : {} : {} {}", sipNode.getLocalIP(), Integer.parseInt(sipNode.getLocalPort()), sipNode.getLocalTransport(), e);
			sipStack = null;
			notReadyReason = "createSipStack FAILED for " + sipNode.getLocalIP()+ ":" + Integer.parseInt(sipNode.getLocalPort())+ ":" +  sipNode.getLocalTransport() + "due to" + e.getMessage();
		}

		if (sipStack != null) {
			if (isProxyEnabled) {
				((SipRouterProxy) sipStack.getRouter()).setLocalNodeCnx(sipNode.getLocalIP(), sipNode.getLocalPort(), sipNode.getLocalTransport());
			}
			if (sipNode.getLocalIP() != null && !"".equals(sipNode.getLocalIP())){
				try {
					_logger.debug("createListeningPoint for {} : {} : {}", sipNode.getLocalIP(), Integer.parseInt(sipNode.getLocalPort()), sipNode.getLocalTransport());
					sipListening = sipStack.createListeningPoint(sipNode.getLocalIP(), Integer.parseInt(sipNode.getLocalPort()), sipNode.getLocalTransport());
					if (sipListening == null) {
						_logger.error("createListeningPoint FAILED for {} : {} : {}", sipNode.getLocalIP(), Integer.parseInt(sipNode.getLocalPort()), sipNode.getLocalTransport());
						notReadyReason = "createListeningPoint FAILED for " + sipNode.getLocalIP()+ ":" + Integer.parseInt(sipNode.getLocalPort())+ ":" +  sipNode.getLocalTransport();
					}
				} catch (NumberFormatException e) {
					_logger.error("createListeningPoint FAILED for {} : {} : {} {}", sipNode.getLocalIP(), Integer.parseInt(sipNode.getLocalPort()), sipNode.getLocalTransport(), e);
					sipListening = null;
					notReadyReason = "createListeningPoint FAILED for " + sipNode.getLocalIP()+ ":" + Integer.parseInt(sipNode.getLocalPort())+ ":" +  sipNode.getLocalTransport() + "due to " + e.getMessage();
				} catch (TransportNotSupportedException e) {
					_logger.error("createListeningPoint FAILED for {} : {} : {} {}", sipNode.getLocalIP(), Integer.parseInt(sipNode.getLocalPort()), sipNode.getLocalTransport(), e);
					sipListening = null;
					notReadyReason = "createListeningPoint FAILED for " + sipNode.getLocalIP()+ ":" + Integer.parseInt(sipNode.getLocalPort())+ ":" +  sipNode.getLocalTransport() + "due to " + e.getMessage();
				} catch (InvalidArgumentException e) {
					_logger.error("createListeningPoint FAILED for {} : {} : {} {}", sipNode.getLocalIP(), Integer.parseInt(sipNode.getLocalPort()), sipNode.getLocalTransport(), e);
					sipListening = null;
					notReadyReason = "createListeningPoint FAILED for " + sipNode.getLocalIP()+ ":" + Integer.parseInt(sipNode.getLocalPort())+ ":" +  sipNode.getLocalTransport() + "due to " + e.getMessage();
				}

			}
			if (sipListening != null) {
				try {
					sipProvider = sipStack.createSipProvider(sipListening);
				} catch (ObjectInUseException e) {
					_logger.error("createSipProvider FAILED for {} : {} : {} {}", sipNode.getLocalIP(), Integer.parseInt(sipNode.getLocalPort()), sipNode.getLocalTransport(), e);
					sipProvider = null;
					notReadyReason = "createSipProvider FAILED for " + sipNode.getLocalIP()+ ":" + Integer.parseInt(sipNode.getLocalPort())+ ":" +  sipNode.getLocalTransport() + "due to " + e.getMessage();
				}

				if (sipProvider != null) {
					try {

						thisSipListener = this;
						sipProvider.addSipListener(thisSipListener);
						sipStack.start();
						isNodeReady = true;

						headerFactory = sipFactory.createHeaderFactory();
						addressFactory = sipFactory.createAddressFactory(); ////new AddressFactoryImpl();//
						messageFactory = sipFactory.createMessageFactory();

					} catch (TooManyListenersException e) {
						_logger.error("addSipListener FAILED for {} : {} : {} {}", sipNode.getLocalIP(), Integer.parseInt(sipNode.getLocalPort()), sipNode.getLocalTransport(), e);
						isNodeReady = false;
						notReadyReason = "addSipListener FAILED for " + sipNode.getLocalIP()+ ":" + Integer.parseInt(sipNode.getLocalPort())+ ":" +  sipNode.getLocalTransport() + "due to " + e.getMessage();
					} catch (SipException e) {
						_logger.error("factories creation FAILED for {} : {} : {} {}", sipNode.getLocalIP(), Integer.parseInt(sipNode.getLocalPort()), sipNode.getLocalTransport(), e);
						isNodeReady = false;
						notReadyReason = "factories creation FAILED for " + sipNode.getLocalIP()+ ":" + Integer.parseInt(sipNode.getLocalPort())+ ":" +  sipNode.getLocalTransport() + "due to " + e.getMessage();
					}

					try {
						queueSize = Integer.parseInt(System.getProperty("sip.queuesize","UNDEFINED"));
					} catch (NumberFormatException nfe) {
						if (SipNodeElement.PERFORMANCE_TRAFIC.equals(sipNode.getTraficType())) {
							queueSize = 60000;
						} else {
							if (Integer.parseInt(sipNode.getMaxConnections()) > 0) {
								queueSize = queueSize * Integer.parseInt(sipNode.getMaxConnections());
							}
						}
					}
					_logger.info("queueSize = {}", queueSize);
					msgRequestsQueues = new ConcurrentHashMap<String, ArrayBlockingQueue<SipRequestTransaction>>(queueSize);
					msgResponsesQueues = new ConcurrentHashMap<String, ArrayBlockingQueue<SipResponseTransaction>>(queueSize);
					msgRequestsIdList = new ArrayList<StringLong>(queueSize);
					msgResponsesIdList = new ArrayList<StringLong>(queueSize);

					dialogList = new ConcurrentHashMap<String, SipDialogData>(queueSize);
					dialogListPerf = new ConcurrentHashMap<String, Request>(queueSize);
					dialogsIdList = new ArrayList<StringLong>(queueSize);

					String perfListenerClassName = System.getProperty("sip.perfListenerClassName"+sipNode.getSipNodeName(),"NOLOCALPERFLISTENER");
					if ("NOLOCALPERFLISTENER".equals(perfListenerClassName)) {
						perfListenerClassName = System.getProperty("sip.perfListenerClassName","NOGLOBALPERFLISTENER");
						if ("NOGLOBALPERFLISTENER".equals(perfListenerClassName)) {
							_logger.error("perfListener = default SimpleCallWithPrack");
							perfListenerClassName = SimpleCallWithPrack.class.getName();
							perfListener = new SimpleCallWithPrack(this);
						} else {
							_logger.info("perfListener global = {}", perfListenerClassName);
						}
					} else {
						_logger.info("perfListener local = {}", perfListenerClassName);
					}
					if (perfListener == null) {
		        	Class<?> clazz;
					try {
						clazz = Class.forName(perfListenerClassName);
						perfListener = (SipListener) clazz.newInstance();
						_logger.error("PerfListener created with {}", perfListenerClassName);
					} catch (Exception e) {
						_logger.error("PerfListener creation failuer for {} ,, due to : {}", perfListenerClassName, e);
					}
					}
					/// First version hardcoded : perfListener = new SimpleCallWithPrack(this);

				}
			}
		}
		int threadpoolmax  = 200;
		int threadpoolmin  = 100;
		try {
			threadpoolmax  = Integer.parseInt(System.getProperty("sip.threadpoolmax","200"));
			threadpoolmin  = Integer.parseInt(System.getProperty("sip.threadpoolmin","100"));
		} catch (NumberFormatException nfe) {
			threadpoolmax  = 200;
			threadpoolmin  = 100;
		}

		threadsPool = new ThreadPoolExecutor(threadpoolmin, threadpoolmax, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(threadpoolmax, true));//keepAliveTime, unit, workQueue)

		String idH = sipNode.getIdentificationHeader();
		if ((idH == null || idH.equals("")) || (!idH.equals("To") && !idH.equals("From") && !idH.equals("Request-URI") && !idH.equals("Call-ID") )) {
			_logger.error("identification header not supported : {}", idH);
			isNodeReady = false;
			notReadyReason = "identification header not supported : " + idH;
		}

		executor = new ScheduledThreadPoolExecutor(100);

		try {
			cleanPeriod = Long.parseLong(System.getProperty("sip.cleanQueuesPeriod","5000"));
		} catch (NumberFormatException nfe) {
			cleanPeriod = 5000;
		}
		try {
			cleanDialogPeriod = Long.parseLong(sipNode.getSessionLifeTime());
		} catch (NumberFormatException nfe) {
			cleanDialogPeriod = 5000;
		}

		_logger.debug("CleanPeriod = {} , CleanDialogPeriod = {}", cleanPeriod, cleanDialogPeriod);
		startMessagesQueuesCleaner();

	}

	public void startMessagesQueuesCleaner() {
		_logger.debug("startMessagesQueuesCleaner : {}", cleanPeriod);
		getExecutor().schedule(new ClearMessagesQueues(this),
				cleanPeriod, TimeUnit.MILLISECONDS);
	}


	public ScheduledThreadPoolExecutor getExecutor()
	{

		return executor;
	}

	public void setExecutor(ScheduledThreadPoolExecutor executor)
	{
		this.executor = executor;
	}


	public class ProcessIncomingRequestMessage implements Runnable {
		private SipNodeContext sipnodectx;

		RequestEvent reqEvt;

		public ProcessIncomingRequestMessage(SipNodeContext node, RequestEvent arg0)
		{
			this.sipnodectx = node;
			this.reqEvt = arg0;
		}

		public void run() {
			long start = dateToString();
			this.sipnodectx.processRequest_v1(reqEvt);
			long diff = dateToString() - start;
			if (diff > 10 && _logger.isWarnEnabled()) _logger.warn("ProcessIncomingRequestMessage for node name and call ID {} : {}  -> took {}", this.sipnodectx.getSipNode().getSipNodeName(), this.reqEvt.getRequest().getHeader("Call-ID"), diff);
		}
	}

	public class ProcessIncomingResponseMessage implements Runnable {
		private SipNodeContext sipnodectx;

		ResponseEvent resEvt;

		public ProcessIncomingResponseMessage(SipNodeContext node, ResponseEvent arg0)
		{
			this.sipnodectx = node;
			this.resEvt = arg0;
		}

		public void run() {

			long start = dateToString();
			this.sipnodectx.processResponse_v1(resEvt);
			long diff = dateToString() - start;
			if (diff > 10 && _logger.isWarnEnabled()) _logger.warn("ProcessIncomingResponseMessage for node name and call ID {} : {}  -> took {}", this.sipnodectx.getSipNode().getSipNodeName(), this.resEvt.getResponse().getHeader("Call-ID"), diff);

		}
	}


	public class ClearMessagesQueues extends TimerTask
	{
		private SipNodeContext sipnode;

		public ClearMessagesQueues(SipNodeContext node)
		{
			this.sipnode = node;
		}



		public void run()
		{
			if (sipnode.isNodeReady) {
				sipnode.ClearMessagesQueues();
			}
		}

	}


	public void ClearMessagesQueues() {
		long curTime = System.currentTimeMillis();
		long limitTime = curTime - cleanPeriod;
		long limitDialogTime = curTime - cleanDialogPeriod;
		_logger.debug("Start ClearMessagesQueues for {} at {} , for limit {} , and limitDialog {}", sipNode.getName(), curTime, limitTime, limitDialogTime);
		printQueueSize();

		long start = dateToString();

		try
		{
			StringLong current;
				if (!msgRequestsIdList.isEmpty()) {
					current = msgRequestsIdList.get(0);
					while (current == null && !msgRequestsIdList.isEmpty()) {
						_logger.debug("msgRequestsIdList : current is null but list is not empty !");
						msgRequestsIdList.remove(0);
						current = msgRequestsIdList.get(0);
					}

					try {
						while (current!= null && current.getaLong() <limitTime) {
							resetRequestQueues(current.getaString());
							//  		a++;
							msgRequestsIdList.remove(0);
							//		b++;
							current = msgRequestsIdList.get(0);
							while (current == null && !msgRequestsIdList.isEmpty()) {
								_logger.debug("msgRequestsIdList : current is null but list is not empty !");
								msgRequestsIdList.remove(0);
								current = msgRequestsIdList.get(0);
							}
						}
						if (current == null) {
							_logger.debug("current is NULL");
						}
					} catch (IndexOutOfBoundsException iobe) {
						//_logger.error("IndexOutOfBoundsException ", e);
					}
				}

				if (!msgResponsesIdList.isEmpty()) {
					current = msgResponsesIdList.get(0);
					while (current == null && !msgResponsesIdList.isEmpty()) {
						_logger.debug("msgResponsesIdList : current is null but list is not empty !");
						msgResponsesIdList.remove(0);
						current = msgResponsesIdList.get(0);
					}
					try {
						while (current != null && current.getaLong() <limitTime) {
							resetResponseQueues(current.getaString());
							msgResponsesIdList.remove(0);
							current = msgResponsesIdList.get(0);
							while (current == null && !msgResponsesIdList.isEmpty()) {
								_logger.debug("msgResponsesIdList : current is null but list is not empty !");
								msgResponsesIdList.remove(0);
								current = msgResponsesIdList.get(0);
							}
						}
						if (current == null) {
							_logger.debug("current is NULL");
						}
					} catch (IndexOutOfBoundsException iobe) {
						// msgResponsesIdList empty. Do nothing.
						_logger.error("IndexOutOfBoundsException {} ", iobe);
					}
				}

			if (isAutomaticCleanup) {
				_logger.debug("automatic dialogue cleanup");

					current = getDialogId();
					while (current != null) {
						if (dialogList.get(current.getaString()) == null || current.getaLong() <limitDialogTime) {
							 if (_logger.isDebugEnabled()) _logger.debug("resetSessionData for {} for {} : {} , for limit {}", sipNode.getName(), current.getaString(), current.getaLong(), limitDialogTime);
							resetSessionData(current.getaString());
							//	e++;
							dialogsIdList.remove(0);
							//f++;
							current = getDialogId();
						} else {
							current = null;
						}
					}
			}

		} catch (Exception ex)
		{
			_logger.error("was not able to clear messages queues {}", ex);
		}

		curTime = System.currentTimeMillis();
		_logger.debug("End ClearMessagesQueues for {} at {}", sipNode.getName(), curTime);
		printQueueSize();
		long diff = dateToString() - start;
		if (diff > 5) _logger.debug("ClearMessagesQueues for node name {} -> SYNC took {}", sipNode.getSipNodeName(), diff);

		    _logger.debug("End CleanQueues");
		startMessagesQueuesCleaner();

	}


	private StringLong getDialogId() {
		StringLong current;
		while (!dialogsIdList.isEmpty()) {
			current = dialogsIdList.get(0);
			if (current == null) {
				dialogsIdList.remove(0);
			} else {
				return current;
			}
		}
		return null;
	}

	public boolean isNodeReady() {
		return isNodeReady;
	}

	public boolean isProxy() {
		return isProxyEnabled;
	}

	public String getNotReadyReason() {
		return notReadyReason;
	}

	private void resetQueues () {
		msgRequestsQueues.clear();
		msgResponsesQueues.clear();
		msgRequestsIdList.clear();
		msgResponsesIdList.clear();
		dialogList.clear();
		dialogsIdList.clear();
	}

	void cleanUp()
	{

		_logger.debug("cleanup");
		try {
			sipProvider.removeSipListener(this);
			sipProvider.removeSipListener(thisSipListener);
			sipStack.deleteSipProvider(sipProvider);
			sipProvider.removeListeningPoint(sipListening);
			sipStack.deleteListeningPoint(sipListening);
			sipStack.stop();
			sipFactory.resetFactory();
			resetQueues();
			isNodeReady = false;
		} catch (ObjectInUseException e) {
			_logger.error("cleanup FAILED for {} : {} : {} {}", sipNode.getLocalIP(), Integer.parseInt(sipNode.getLocalPort()), sipNode.getLocalTransport(), e);
		}

		testPortStatus(sipNode.getSipNodeName() + " cleanup", Integer.parseInt(sipNode.getLocalPort()));

	}

	@Override
	public void processDialogTerminated(DialogTerminatedEvent arg0) {
		_logger.debug("processDialogTerminated: TODO");
	}


	@Override
	public void processIOException(IOExceptionEvent arg0) {
		// TODO Auto-generated method stub
		_logger.debug("processIOException: TODO");
	}


	@Override
	public void processRequest(RequestEvent arg0) {
//		if (_logger.isWarnEnabled()) _logger.warn("RCV "+ arg0.getRequest().getMethod() + " : " + arg0.getRequest().getHeader("Call-ID").toString().substring("Call-ID: ".length()).replaceAll("\\r|\\n", "") + ", From = " + arg0.getRequest().getHeader("From").toString().replaceAll("\\r|\\n", "")+ ", From = " + arg0.getRequest().getHeader("To").toString().replaceAll("\\r|\\n", "")+ "," + LocalDateTime.now().format(formatter));
		threadsPool.execute(new ProcessIncomingRequestMessage(this, arg0));
	}

	public void processRequest_v1(RequestEvent arg0) {
		if (_logger.isDebugEnabled()) _logger.debug("processRequest: {} identified by {} = {}", arg0.getRequest().getMethod(), this.sipNode.getIdentificationHeader(), arg0.getRequest().getHeader(this.sipNode.getIdentificationHeader()));
		SimonManager.getCounter(
				SipCounters.SIP_NODE_INBOUND_REQUEST.toString() + SipCounters.SUCCESS_SUFFIX.toString() + "."
						+ arg0.getRequest().getMethod()
						+ "." + getSipNode().getSipNodeName().replace(".", "_")
				).increase();

		if (arg0 != null && arg0.getRequest() != null && "INVITE".equals(arg0.getRequest().getMethod())) {
		}
		if (SipNodeElement.FUNCTIONAL_TRAFIC.equals(sipNode.getTraficType())) {
			processRequestFunctional(arg0);
		} else {
			processRequestPerformance(arg0);
		}

	}

	public String getIdHeader (Request req) {

		String idHeader = this.sipNode.getIdentificationHeader();
		String id = null;

		if ("To".equals(idHeader)) {
			ToHeader toH = (ToHeader) req.getHeader(idHeader);
			if (_logger.isDebugEnabled()) _logger.debug("getIdHeader: toH {}", toH);
			id = SipString.cleanString(toH.getAddress().toString());
		} else if ("From".equals(idHeader)) {
			FromHeader fromH = (FromHeader) req.getHeader(idHeader);
			if (_logger.isDebugEnabled()) _logger.debug("getIdHeader: fromH {}", fromH);
			id = SipString.cleanString(fromH.getAddress().toString());
		} else if ("Request-URI".equals(idHeader)) {
			SipURI requriH = (SipURI) req.getRequestURI();
			if (_logger.isDebugEnabled()) _logger.debug("getIdHeader: requriH {}", requriH);
			id = SipString.cleanString(requriH.toString());
		} else if ("Call-ID".equals(idHeader)) {
			CallIdHeader callid = (CallIdHeader) req.getHeader("Call-ID");
			id = SipString.cleanString(callid.getCallId());
		}
		id = SipString.extractAddress(id);

		if (_logger.isDebugEnabled()) _logger.debug("getIdHeader: {} identified by {} = {}",req.getMethod(), this.sipNode.getIdentificationHeader(), id);
		return id;
	}

	private void processRequestFunctional(RequestEvent arg0) {
		ServerTransaction st = null;

		String id = getIdHeader(arg0.getRequest());
		String method = arg0.getRequest().getMethod();
		if (_logger.isDebugEnabled()) _logger.debug("processRequestFunctional: {} identified by {} = {}", method, this.sipNode.getIdentificationHeader(), id);

		try {
			if (arg0.getServerTransaction() == null) {
				if (_logger.isDebugEnabled()) _logger.debug("processRequestFunctional: no server transaction for id {}", id);
				if (!arg0.getRequest().getMethod().equals(Request.ACK)) {
					// Create a server transaction only for request not an ACK (Creating server transaction for ACK -- makes no sense!)
					st = sipProvider.getNewServerTransaction(arg0.getRequest());
				}
			} else {
				st = arg0.getServerTransaction();
			}
			ArrayBlockingQueue<SipRequestTransaction> reqQueue = checkRequestQueue(id + "_" + method);


			SipRequestTransaction reqTrans = new SipRequestTransaction((SIPServerTransactionImpl)st, arg0.getRequest());

			reqQueue.add(reqTrans);
		} catch (IllegalStateException ise) {
			_logger.error("processRequestFunctional: IllegalStateException for id {} {}", id, ise);
		} catch (IllegalArgumentException iae) {
			_logger.error("processRequestFunctional: IllegalArgumentException for id {} {}",id, iae);
		} catch (Throwable t) {
			_logger.error("processRequestFunctional: Throwable for id {} {}", id, t);
		}
	}

	private void processRequestPerformance(RequestEvent arg0) {
		perfListener.processRequest(arg0);
	}



	@Override
	public void processResponse(ResponseEvent responseReceivedEvent) {
		CSeqHeader cseH = (CSeqHeader) responseReceivedEvent.getResponse().getHeader("CSeq");

		threadsPool.execute(new ProcessIncomingResponseMessage(this, responseReceivedEvent));
	}

	public void processResponse_v1(ResponseEvent responseReceivedEvent) {
		Response response = (Response) responseReceivedEvent.getResponse();
		if (_logger.isDebugEnabled()) {
			_logger.debug("processResponse: {}", response.getStatusCode());

		}

		String rseq = null;
		if (response.getHeader("RSeq") != null) {
			rseq = response.getHeader("RSeq").toString();
		}
		String require = null;
		if (response.getHeader("Require") != null) {
			require = response.getHeader("Require").toString();
		}
		boolean isReliable = (rseq != null && !"".equals(rseq) && require != null && require.contains("100rel"));

		if (_logger.isInfoEnabled()) _logger.info("Received response retranmission processing {} : {} : {} : {} : {} : {}",
				isReliable,
				this.isReliableResponseRetransmissionIgnored(),
				this.isNonReliableResponseRetransmissionIgnored(),
				((ResponseEventExt)responseReceivedEvent).isRetransmission(),
				this.sipNode.getIgnoreNonReliableResponseRetransmission(),
				this.sipNode.getIgnoreReliableResponseRetransmission());
		if ( (isReliable && this.isReliableResponseRetransmissionIgnored()) || (!isReliable && this.isNonReliableResponseRetransmissionIgnored()))
		{
			if ( ((ResponseEventExt)responseReceivedEvent).isRetransmission())
			{
				if (_logger.isInfoEnabled())
				{
					_logger.info("Received response is a retransmission and retransmission are ignored");
				}
				return ;
			} else {
				_logger.info("Received response is not a retransmission and not ignored");
			}
		}


		SimonManager.getCounter(
				SipCounters.SIP_NODE_INBOUND_ANSWER.toString() + SipCounters.SUCCESS_SUFFIX.toString() + "."
						+ response.getStatusCode()
						+ "." + getSipNode().getSipNodeName().replace(".", "_")
				).increase();

		if (SipNodeElement.FUNCTIONAL_TRAFIC.equals(sipNode.getTraficType())) {
			processResponseFunctional(responseReceivedEvent);
		} else {
			processResponsePerformance(responseReceivedEvent);
		}
	}

	private void processResponseFunctional(ResponseEvent responseReceivedEvent) {

		Response response = (Response) responseReceivedEvent.getResponse();
		if (_logger.isDebugEnabled()) _logger.debug("processResponseFunctional: {}", response.getStatusCode());
		CSeqHeader cseH = (CSeqHeader) response.getHeader("CSeq");
		String method = cseH.getMethod();

		String dialId = SipString.cleanString(responseReceivedEvent.getResponse().getHeader("Call-ID").toString().substring("Call-ID".length() + 2)); //ClientTransaction().getDialog().getDialogId();
		ArrayBlockingQueue<SipResponseTransaction> respQueue;
		try {
			respQueue = checkResponseQueue(dialId+"_" + response.getStatusCode() + "_" + method);
		} catch (Exception e) {
			_logger.error("processResponseFunctional: error in checkResponseQueue for id = {} {}", dialId,e);
			return;
		}


		// Check last received response is different
		if (respQueue != null && respQueue.peek() != null) {
			SipResponseTransaction clientTrans = respQueue.peek();
			Response lastStoredResp = clientTrans.getTheResponse();//clientTrans.getTheTransaction().getLastResponse();
		} else {
			if (_logger.isDebugEnabled()) _logger.debug("resQueue empty. response received: {}", response.toString());
		}

		SIPClientTransactionImpl ct = (SIPClientTransactionImpl) responseReceivedEvent.getClientTransaction();

		SipResponseTransaction respTrans = new SipResponseTransaction(ct, response);

		if (ct==null) {
			if (_logger.isDebugEnabled()) _logger.debug("No transaction put in SipResponseTransaction");
		}

		respQueue.add(respTrans);
		if (_logger.isDebugEnabled()) _logger.debug("processResponseFunctional: put in responsesQeueues for id = {}. queue size = {}", dialId, respQueue.size());
	}

	private void processResponsePerformance(ResponseEvent responseReceivedEvent) {
		perfListener.processResponse(responseReceivedEvent);
	}

	@Override
	public void processTimeout(TimeoutEvent arg0) {
		if (_logger.isDebugEnabled()) _logger.debug("processTimeout: TODO");

		if (arg0.isServerTransaction()) {
			Request theReq = arg0.getServerTransaction().getRequest();
		} else {
			Request theReq = arg0.getClientTransaction().getRequest();
		}
	}


	@Override
	public void processTransactionTerminated(TransactionTerminatedEvent terminationEvt) {
		if (_logger.isDebugEnabled()) { _logger.debug("processTransactionTerminated: isServerTransaction {}", terminationEvt.isServerTransaction());
		_logger.debug("processTransactionTerminated: Source = {}", terminationEvt.getSource()); }

		if (terminationEvt.isServerTransaction()) {
			Request theReq = terminationEvt.getServerTransaction().getRequest();
		} else {
			Request theReq = terminationEvt.getClientTransaction().getRequest();
		}

		if (terminationEvt.isServerTransaction()) {
			ServerTransaction sT = terminationEvt.getServerTransaction();
			String id = getIdHeader(sT.getRequest());

		}

	}

	private long dateToString() {
		return System.currentTimeMillis();
	}

	public ArrayBlockingQueue<SipRequestTransaction> checkRequestQueue(String id) throws Exception{
		long start = dateToString();
		try {
			ArrayBlockingQueue<SipRequestTransaction> reqQueue = msgRequestsQueues.get(id);

			if (reqQueue != null) {
				return reqQueue;
			}
			reqQueue = new ArrayBlockingQueue<SipRequestTransaction>(queueSize);
			msgRequestsQueues.putIfAbsent(id, reqQueue);
			msgRequestsIdList.add(new StringLong(id, System.currentTimeMillis()));
			if (_logger.isErrorEnabled()) { _logger.debug("checkRequestQueue new queue for id {}, and node name {}  -> END", id, sipNode.getSipNodeName());
			long diff = dateToString() - start;
			if (diff > 5 && _logger.isWarnEnabled()) _logger.warn("checkRequestQueue msgRequestsQueues.get for id {}, and node name {} -> SEARCH took {}", id, sipNode.getSipNodeName(), diff); }

			return msgRequestsQueues.get(id); //reqQueue;
		}
		catch ( Exception e){
			_logger.error("checkRequestQueue for id {}: Exception occurred {}", id, e);
			throw(e);
		}


	}

	public void clearQueue(String id, boolean reqQueue, boolean respQueue) {
		//_logger.debug("clearQueue for id " + id + ", and node name " + sipNode.getSipNodeName() + " -> START. " + reqQueue +":"+respQueue);

		if (reqQueue) {
			long start = dateToString();
				ArrayBlockingQueue<SipRequestTransaction> queueReq = msgRequestsQueues.get(id);
				if (queueReq != null) {
					for (SipRequestTransaction sipRequestTransaction : queueReq) {
						Dialog reqDial = sipRequestTransaction.getTheTransaction().getDialog();
						sipRequestTransaction.getTheTransaction().cleanUp();
						if (reqDial != null ) {
							try {
								if (_logger.isDebugEnabled()) _logger.debug("Dialog terminateOnBye");
								reqDial.terminateOnBye(true);
							} catch (SipException e) {
								if (_logger.isDebugEnabled()) _logger.debug("Dialog already terminated");
							}
							reqDial.delete();
						}
					}
					msgRequestsQueues.remove(id);
				}

		}

		if (respQueue) {
			long start = dateToString();
				ArrayBlockingQueue<SipResponseTransaction> queueResp = msgResponsesQueues.get(id);
				if ( queueResp != null ) {
					for (SipResponseTransaction sipResponseTransaction : queueResp) {
						Dialog respDial = sipResponseTransaction.getTheTransaction().getDialog();
						sipResponseTransaction.getTheTransaction().cleanUp();
						if (respDial != null) {
							try {
								if (_logger.isDebugEnabled()) _logger.debug("Dialog terminateOnBye");
								respDial.terminateOnBye(true);
							} catch (SipException e) {
								if (_logger.isDebugEnabled()) _logger.debug("Dialog already terminated");
							}
							respDial.delete();
						}
					}
					msgResponsesQueues.remove(id);
				}

		}
	}

	public ArrayBlockingQueue<SipResponseTransaction> checkResponseQueue(String id) throws Exception{
		if (_logger.isDebugEnabled()) _logger.debug("checkResponseQueue for id {} , and node name {}  -> START {}", id, sipNode.getSipNodeName(), dateToString());
		long start = dateToString();
			try {
				ArrayBlockingQueue<SipResponseTransaction> respQueue = msgResponsesQueues.get(id);

				if (respQueue != null) {
					if (_logger.isDebugEnabled()) _logger.debug("checkResponseQueue for id {} , and node name {} -> END with existing queue", id, sipNode.getSipNodeName());
					return respQueue;
				}
				respQueue = new ArrayBlockingQueue<SipResponseTransaction>(queueSize);
				msgResponsesQueues.putIfAbsent(id, respQueue);
				msgResponsesIdList.add(new StringLong(id, System.currentTimeMillis()));
				if (_logger.isDebugEnabled()) { _logger.debug("checkResponseQueue new queue for id {} , and node name {}  -> END", id, sipNode.getSipNodeName());
				long diff = dateToString() - start;
				if (diff > 5) _logger.debug("checkResponseQueue msgResponsesQueues.get for id {}, and node name {} -> SEARCH took {}", id, sipNode.getSipNodeName(), diff); }

				return msgResponsesQueues.get(id);
			}
			catch ( Exception e){
				_logger.error("checkResponseQueue for id {} : Exception occurred {}", id, e);
				throw(e);
			}


	}

	public void resetQueues(String id) {
		//_logger.error("resetQueues Resp for id " + id + ", and node name " + sipNode.getSipNodeName() + " -> START " + dateToString());
		long start = dateToString();
			ArrayBlockingQueue<SipResponseTransaction> queueResp = msgResponsesQueues.get(id);
			if (queueResp != null) {
				queueResp.clear();
			}
			msgResponsesQueues.remove(id);


		start = dateToString();
			ArrayBlockingQueue<SipRequestTransaction> queueReq = msgRequestsQueues.get(id);
			if (queueReq != null) {
				queueReq.clear();
			}
			msgRequestsQueues.remove(id);

	}

	public void resetRequestQueues(String id) {
		//_logger.error("resetQueues Req for id " + id + ", and node name " + sipNode.getSipNodeName() + " -> START " + dateToString());
		long start = dateToString();
		ArrayBlockingQueue<SipRequestTransaction> queueReq = msgRequestsQueues.get(id);
		if (queueReq != null) {
			queueReq.clear();
			msgRequestsQueues.remove(id);
		} else {
			// _logger.error("resetQueues Req NULL for id " + id );
		}
		msgRequestsQueues.remove(id);
	}

	public void resetResponseQueues(String id) {
		//_logger.error("resetQueues Resp for id " + id + ", and node name " + sipNode.getSipNodeName() + " -> START " + dateToString());
		long start = dateToString();
		ArrayBlockingQueue<SipResponseTransaction> queueResp = msgResponsesQueues.get(id);
		if (queueResp != null) {
			queueResp.clear();
			msgResponsesQueues.remove(id);
		}
		msgResponsesQueues.remove(id);
	}

	public SipNodeElement getSipNode() {
		return sipNode;
	}

	public SipStack getSipStack() {
		return sipStack;
	}

	public ListeningPoint getSipListening() {
		return sipListening;
	}

	public SipProvider getSipProvider() {
		return sipProvider;
	}

	public SipFactory getSipFactory() {
		return sipFactory;
	}

	public HeaderFactory getHeaderFactory() {
		return headerFactory;
	}

	public AddressFactory getAddressFactory() {
		return addressFactory;
	}

	public MessageFactory getMessageFactory() {
		return messageFactory;
	}

	public Map<String, Request> getDialogListPerf() {
		return dialogListPerf;
	}

	public boolean isNonReliableResponseRetransmissionIgnored()
	{
		return isNonReliableResponseRetransmissionIgnored;
	}

	public boolean isReliableResponseRetransmissionIgnored()
	{
		return isReliableResponseRetransmissionIgnored;
	}

	public SipDialogData readSessionData(String id) {
		//_logger.error("readSessionData for " + sipNodeName +"." + dialogueNb + ", with identification = " + identification);

			return dialogList.get(id);

	}

	public void resetSessionData(String id) {
		//_logger.error("resetSessionData for " + sipNodeName +"." + dialogueNb + ", with identification = " + identification);

			dialogList.remove(id);


	}

	public void storeSessionData(String id, SipDialogData sipSess) {

			if (!dialogList.containsKey(id)) {
				dialogsIdList.add(new StringLong(id, System.currentTimeMillis()));
			}
			dialogList.putIfAbsent(id, sipSess);

	}

	private void testPortStatus (String name, int port) {
		boolean portTaken = false;
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			_logger.info("SIP Port validation : for {} . port {} is taken !", name, port);
			portTaken = true;
		} finally {
			if (socket != null)
				_logger.info("SIP Port validation : for {}. port {} is free ! ", name, port);
			try {
				socket.close();
			} catch (IOException e) { /* e.printStackTrace(); */ }
		}
	}


}
