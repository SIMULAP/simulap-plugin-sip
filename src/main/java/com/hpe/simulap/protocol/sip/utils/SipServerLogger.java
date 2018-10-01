//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package com.hpe.simulap.protocol.sip.utils;

import java.util.Properties;

import javax.sip.SipStack;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import gov.nist.core.ServerLogger;
import gov.nist.javax.sip.message.SIPMessage;

public class SipServerLogger implements ServerLogger {

	private static final Logger _logger = LoggingManager.getLoggerForClass();

	@Override
	public void closeLogFile() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void logMessage(SIPMessage message, String from, String to,
			boolean sender, long time) {
		// TODO Auto-generated method stub
		_logger.info("LogMessage: " + message.toString() + ":" + from +":" + to +":" + sender +":" + time);
		
	}

	@Override
	public void logMessage(SIPMessage message, String from, String to,
			String status, boolean sender, long time) {
		// TODO Auto-generated method stub
		_logger.info("LogMessage with status: " + message.toString() + ":" + from +":" + to +":" + status +":" + sender +":" + time);
		
	}

	@Override
	public void logMessage(SIPMessage message, String from, String to,
			String status, boolean sender) {
		// TODO Auto-generated method stub
		_logger.info("LogMessage with status: " + message.toString() + ":" + from +":" + to +":" + status +":" + sender);
		
	}

	@Override
	public void logException(Exception ex) {
		// TODO Auto-generated method stub
		_logger.info("logException: ", ex);

	}

	@Override
	public void setStackProperties(Properties stackProperties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSipStack(SipStack sipStack) {
		// TODO Auto-generated method stub
		
	}

}
