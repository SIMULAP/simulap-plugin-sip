//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package com.hpe.simulap.protocol.sip.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.map.HashedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListHeaders implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1397797659478992271L;
	private Map<String, List<String>> headersMap = new HashedMap();

	private static final Logger _logger =LoggerFactory.getLogger(ListHeaders.class);

	public ListHeaders() {		
	}
	
	public void addHeader(String name, String value) {
		if (_logger.isDebugEnabled()) _logger.debug("addHeader name = " + name + ", value = "+ value);
		List<String> theValues = headersMap.get(name);
		if (theValues == null) {
			theValues = new ArrayList<String>();
			headersMap.put(name, theValues);
		}
		theValues.add(value);
	}
	
	public List<String> getHeaders(String name) {
		if (_logger.isDebugEnabled()) _logger.debug("getHeaders name = " + name);
		return headersMap.get(name);
	}

	public List<String> removeHeaders(String name) {
		if (_logger.isDebugEnabled()) _logger.debug("removeHeaders name = " + name);
		return headersMap.remove(name);
	}

	public List<String> removeHeaders(String name, int count) throws UnicityHeaderException {
		if (_logger.isDebugEnabled()) _logger.debug("removeHeaders name " + name + ":" + count);
		if (headersMap.get(name) == null) {
			return null;
		}
		if (headersMap.get(name).size() != count) {
			throw new UnicityHeaderException("Header " + name + ", expecting " + count + " values, and got " + headersMap.get(name).size());
		}
		return headersMap.remove(name);
	}

	
	public Set<Entry<String, List<String>>> entrySet() {
		return headersMap.entrySet();
	}

}
