//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package com.hpe.simulap.protocol.sip.config;

import gov.nist.javax.sip.stack.DefaultRouter;
import gov.nist.javax.sip.stack.HopImpl;
import gov.nist.javax.sip.stack.SIPTransactionStack;

import java.util.ListIterator;

import javax.sip.SipException;
import javax.sip.SipStack;
import javax.sip.address.Hop;
import javax.sip.address.Router;
import javax.sip.message.Request;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class SipRouterProxy extends DefaultRouter { //implements Router {

	private static final Logger _logger = LoggingManager.getLoggerForClass();

	private SIPTransactionStack sipStack;
	private String outpx = null;
	private Hop outHop = null;
	private Hop nodeHop = null;
	//	private String nodeIP = null;
	//	private String nodePort = null;


	public SipRouterProxy ( SipStack sipStack, String outboundProxy) {		
		super(sipStack,outboundProxy);
		_logger.debug("SipRouterProxy with outboundproxy = " + outboundProxy);
		this.sipStack = (SIPTransactionStack) sipStack;
		this.outpx = outboundProxy; 
		this.outHop = (Hop) this.sipStack.getAddressResolver()
				.resolveAddress(extractHopFromString(outpx));		
	}

	private Hop extractHopFromString (String hopDef) {

		String ipAddress = "" ;
		int port = -1 ;
		String transportType = "";

		try {

			if (hopDef.contains("[") && hopDef.contains("]")) {
				// ipv6 address format
////				ipAddress = hopDef.substring(1,hopDef.indexOf("]")-1);
				ipAddress = hopDef.substring(1,hopDef.indexOf("]"));
////				port = Integer.parseInt(hopDef.substring(hopDef.indexOf("]")+2,hopDef.indexOf("/")-1));
				port = Integer.parseInt(hopDef.substring(hopDef.indexOf("]")+2,hopDef.indexOf("/")));
////				transportType = hopDef.substring(hopDef.indexOf("/")+1, hopDef.length() -1);
				transportType = hopDef.substring(hopDef.indexOf("/")+1, hopDef.length());
			}
			else {
				// ipV4 address format
				int deuxpoints = hopDef.indexOf(":");
				int divise = hopDef.indexOf("/");
				int longueur = hopDef.length();

				ipAddress = hopDef.substring(0, deuxpoints);
				port = Integer.parseInt(hopDef.substring(deuxpoints+1, divise));
				transportType = hopDef.substring(divise+1, longueur);
			}

		}
		catch(Exception e) {
            _logger.error("Problem while parsing hop IP address <"+hopDef+">",e);
            return null ;
		}

		_logger.debug("extractHopFromString from " + hopDef + "-> " + ipAddress + "#" 
				+ port + "#" + transportType);

		return new HopImpl(ipAddress, port, transportType);
	}

	public void setLocalNodeCnx(String nodeIP, String nodePort, String nodeTransport) {
		//		this.nodeIP = nodeIP;
		//		this.nodePort = nodePort;
		nodeHop = this.sipStack.getAddressResolver().resolveAddress(new HopImpl(nodeIP, Integer.parseInt(nodePort), nodeTransport)); //nextH.getTransport()));
	}

	@Override
	public Hop getNextHop(Request arg0) throws SipException {
		_logger.debug("getNextHop from DefaultRouter for " + arg0.getMethod());
		Hop nextH = super.getNextHop(arg0);

		//			Hop localH = this.sipStack.getAddressResolver().resolveAddress(new HopImpl(nodeIP, Integer.parseInt(nodePort), nextH.getTransport()));
		//			if (localH.getHost().equals(nextH.getHost()) && localH.getPort() == nextH.getPort()){
		/*			if (nodeHop.getHost().equals(nextH.getHost()) && nodeHop.getPort() == nextH.getPort()){
				_logger.debug("SipRouterProxy getNextHop " + this.outHop.toString());
				arg0.removeHeader("Route");
				return this.outHop;
			} else {
				_logger.debug("DefaultRouter getNextHop " + nextH.toString());				
				return nextH;				
			}

		 */

		// arg0.removeHeader("Route");
		return this.outHop;

	}

	/*	@Override
	public ListIterator getNextHops(Request arg0) {
		_logger.debug("getNextHops for " + arg0.getMethod());
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Hop getOutboundProxy() {
		_logger.debug("getOutboundProxy");
		// TODO Auto-generated method stub
		return this.outHop;
	}
	 */
	
	
	
	
}
