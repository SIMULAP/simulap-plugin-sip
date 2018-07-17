//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package org.apache.jmeter.protocol.sip.sampler;

public class GuiHeadersParser {
	
	public static String[] decodeReqUri(String theReqString) {
		if (theReqString.indexOf("sips:") == -1) {
			String[] reqparts = theReqString.split("(sip:)|(sip:)*(@)|(@)*(>)");
			if (reqparts.length == 3) {
				String[] res = {reqparts[1], reqparts[2]};
				return res;
			} else if (reqparts.length == 2) {
				String[] res = {null, reqparts[1]};
				return res;
			}
			return null;
  		} else {
  			return decodeReqUriSipS(theReqString);
  		}
	}

	public static String[] decodeReqTelUri(String theReqString) {
		String[] reqparts = theReqString.split("(tel:)|(tel:)*(@)|(@)*(>)");
		if (reqparts.length == 3) {
			String[] res = {reqparts[1], reqparts[2]};
			return res;
		} else if (reqparts.length == 2) {
			String[] res = {null, reqparts[1]};
			return res;
		}
		return null;  		
	}

	public static String[] decodeReqUriSipS(String theReqString) {
		String[] reqparts = theReqString.split("(sips:)|(sips:)*(@)|(@)*(>)");
		if (reqparts.length == 3) {
			String[] res = {reqparts[1], reqparts[2]};
			return res;
		}
		return null;
	}

	public static String[] decodeVia(String theViaString) {
		if (theViaString.contains("[") && theViaString.contains("]")) {
			return decodeViav6(theViaString);
		}
		/// String[] reqparts = theViaString.split("(sip:)|(sip:)*(:)|(:)*(>)");
		String[] reqparts = theViaString.split("(sip:)|(sip:)*(:)|(:)*(>)|(;branch=)");
		if (reqparts.length >= 2) {
//			String[] res = {reqparts[0], reqparts[1]};
			return reqparts;
		}
		return null;
	}

	public static String[] decodeViav6(String theViaString) {

		String ipv6 = theViaString.substring(theViaString.indexOf("["), theViaString.indexOf("]")+1);

		/// String[] reqparts = theViaString.split("(sip:)|(sip:)*(:)|(:)*(>)");
		String[] subreqparts = theViaString.substring(theViaString.indexOf("]")+2).split("(sip:)|(sip:)*(:)|(:)*(>)|(;branch=)");
		if (subreqparts.length == 1) {
			String[] reqparts = {ipv6, subreqparts[0]};
			return reqparts;
		}			
		if (subreqparts.length == 2) {
			String[] reqparts = {ipv6, subreqparts[0], subreqparts[1]};
			return reqparts;
		}			
		
		return null;
	}

	
	public static String[] decodeCseq(String theCseqString) {
		String[] reqparts = theCseqString.split("(sip:)|(sip:)*( )|( )*(>)");
		if (reqparts.length == 2) {
			String[] res = {reqparts[0], reqparts[1]};
			return res;
		}
		return null;
	}
	
}
