//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package org.apache.jmeter.protocol.sip.utils;

import java.util.Properties;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import gov.nist.core.StackLogger;

public class SipStackLogger implements StackLogger {

	private static final Logger _logger = LoggingManager.getLoggerForClass();

	@Override
	public void logStackTrace() {
		// TODO Auto-generated method stub
		_logger.info("logStackTrace");
		
	}

	@Override
	public void logStackTrace(int traceLevel) {
		// TODO Auto-generated method stub
		_logger.info("logStackTrace " + traceLevel);
	}

	@Override
	public int getLineCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void logException(Throwable ex) {
		// TODO Auto-generated method stub
		_logger.error("logException ", ex);
	}

	@Override
	public void logDebug(String message) {
		// TODO Auto-generated method stub
		_logger.debug(message);
	}

	@Override
	public void logTrace(String message) {
		// TODO Auto-generated method stub
		_logger.info(message);
	}

	@Override
	public void logFatalError(String message) {
		// TODO Auto-generated method stub
		_logger.error(message);
	}

	@Override
	public void logError(String message) {
		// TODO Auto-generated method stub
		_logger.error(message);
	}

	@Override
	public boolean isLoggingEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isLoggingEnabled(int logLevel) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void logError(String message, Exception ex) {
		// TODO Auto-generated method stub
		_logger.error(message, ex);
	}

	@Override
	public void logWarning(String string) {
		// TODO Auto-generated method stub
		_logger.warn(string);
	}

	@Override
	public void logInfo(String string) {
		// TODO Auto-generated method stub
		_logger.info(string);
	}

	@Override
	public void disableLogging() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enableLogging() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBuildTimeStamp(String buildTimeStamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStackProperties(Properties stackProperties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLoggerName() {
		// TODO Auto-generated method stub
		return this.getClass().getName();
	}

}
