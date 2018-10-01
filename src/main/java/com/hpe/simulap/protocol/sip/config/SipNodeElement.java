//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package com.hpe.simulap.protocol.sip.config;


import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class SipNodeElement extends AbstractTestElement implements ConfigElement,
TestStateListener, TestIterationListener, TestBean {
	
	
	
	private static final long serialVersionUID = -1521562608694035895L;
			
	private static final Logger _logger = LoggingManager.getLoggerForClass();

	public static String TRANSPORT_TCP="TCP";
	public static String TRANSPORT_UDP="UDP";
	public static String TRANSPORT_SCTP="SCTP";	
	
	public static String FUNCTIONAL_TRAFIC = "FUNCTIONAL";
	public static String PERFORMANCE_TRAFIC = "PERFORMANCE";
	public static String SAMPLING_TRAFIC = "SAMPLING";
	private String stackLog = "NONE";
	
	public static String YES = "yes";
	public static String NO = "no";
	
	transient String sipNodeName, traficType, autoAnswerTimeout, localPort, localIP, localTransport, maxConnections, cacheConnections, sessionLifeTime,queueTimeout, outboundProxy, identificationHeader, debuglevel, 
	ignoreNonReliableResponseRetransmission, ignoreReliableResponseRetransmission,automaticQueuesCleanup;
	

	
	public SipNodeElement() {
	}
	
	public void testEnded() {
		_logger.info("testEnded()");
		
		//  cleanup and delete threads in charge of destroying zombis session 
		//  cleanup and delete SIP link
		JMeterVariables variables = getThreadContext().getVariables();
		SipNodeContext context = (SipNodeContext) variables.getObject(getSipNodeName());
		context.cleanUp();
		
		

	}
	
	

	public void testEnded(String host) {
		_logger.info("testEnded(host)");
		testEnded();
	}

	public void testIterationStart(LoopIterationEvent event) {

	}

	public void testStarted() {
		
		this.setRunningVersion(true);
		
		TestBeanHelper.prepare(this);
		JMeterVariables variables = getThreadContext().getVariables();
		String debugStack = variables.get("stackloglevel");
		if (debugStack != null && !debugStack.equals("")) {
			stackLog = debugStack;
		}
		
		_logger.info("testStarted()" + stackLog);

		if (variables.getObject(getSipNodeName()) != null) {
			_logger.warn("Sip node context already defined ", null);
		} else {
			synchronized (this) {
				try {
					_logger.info("create Sip node  context with name " + getSipNodeName());
					// create a SIP link
					variables.putObject(getSipNodeName(), new SipNodeContext(this));
					
				} catch (Exception e) {
					_logger.error("cannot initialize SIP node ", e);
				}
			}
		}
	}

	public void testStarted(String host) {
		testStarted();
	}

	public Object clone() {
		SipNodeElement el = (SipNodeElement) super.clone();
		// clone the private fields
		return el;
	}
	
	public void addConfigElement(ConfigElement config) {
		_logger.debug("addConfigElement");
	}

	public boolean expectsModification() {
		return false;
	}

	public String getSessionLifeTime()
    {
        return sessionLifeTime;
    }

    public void setSessionLifeTime(String sessionLifeTime)
    {
        this.sessionLifeTime = sessionLifeTime;
    }

    public String getQueueTimeout() {
		return queueTimeout;
	}

	public void setQueueTimeout(String queueTimeout) {
		this.queueTimeout = queueTimeout;
	}

	public String getTraficType() {
		return traficType;
	}

	public void setTraficType(String traficType) {
		this.traficType = traficType;
	}

	public String getAutoAnswerTimeout() {
		return autoAnswerTimeout;
	}



	public void setAutoAnswerTimeout(String autoAnswerTimeout) {
		this.autoAnswerTimeout = autoAnswerTimeout;
	}
	
	public String getSipNodeName() {
		return sipNodeName;
	}

	public void setSipNodeName(String sipNodeName) {
		this.sipNodeName = sipNodeName;
	}

	public String getLocalPort() {
		return localPort;
	}

	public void setLocalPort(String localPort) {
		this.localPort = localPort;
	}

	public String getLocalIP() {
		return localIP;
	}

	public void setLocalIP(String localIP) {
		this.localIP = localIP;
	}

	public String getLocalTransport() {
		return localTransport;
	}

	public void setLocalTransport(String localTransport) {
		this.localTransport = localTransport;
	}

	public String getOutboundProxy() {
		return outboundProxy;
	}

	public void setOutboundProxy(String outboundProxy) {
		this.outboundProxy = outboundProxy;
	}

	public String getIdentificationHeader() {
		return identificationHeader;
	}

	public void setIdentificationHeader(String identificationHeader) {
		this.identificationHeader = identificationHeader;
	}

	public String getDebuglevel() {
		JMeterVariables variables = getThreadContext().getVariables();

		return variables.get("debugLevel");
	}

	public String getStackLog() {
		return stackLog;
	}

	public String getMaxConnections() {
		return (maxConnections == null || "".equals(maxConnections)) ? "1" : maxConnections ;
	}

	public void setMaxConnections(String maxConnections) {
		this.maxConnections = maxConnections;
	}

	public String getCacheConnections() {
		return ( cacheConnections == null || "".equals(cacheConnections)) ? NO : cacheConnections;
	}

	public void setCacheConnections(String cacheConnections) {
		this.cacheConnections = cacheConnections;
	}

	public String getIgnoreNonReliableResponseRetransmission() {
		return ignoreNonReliableResponseRetransmission;
	}

	public void setIgnoreNonReliableResponseRetransmission(String ignoreNonReliableResponseRetransmission) {
		this.ignoreNonReliableResponseRetransmission = ignoreNonReliableResponseRetransmission;
	}

	public String getIgnoreReliableResponseRetransmission() {
		return ignoreReliableResponseRetransmission;
	}

	public void setIgnoreReliableResponseRetransmission(String ignoreReliableResponseRetransmission) {
		this.ignoreReliableResponseRetransmission = ignoreReliableResponseRetransmission;
	}

	public String getAutomaticQueuesCleanup() {
		return automaticQueuesCleanup;
	}

	public void setAutomaticQueuesCleanup(String automaticQueuesCleanup) {
		this.automaticQueuesCleanup = automaticQueuesCleanup;
	}
	
}
