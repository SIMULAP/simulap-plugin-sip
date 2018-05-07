//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package org.apache.jmeter.protocol.sip.utils;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class SipString {

    private static final Logger _logger = LoggingManager.getLoggerForClass();

    public static String cleanString (String a) {
    	if (a==null) {
    		return null; 
    	}
		String b = a.replaceAll("[\\n\\t ]", ""); // remove newlines, tabs, and spaces
//		b = b.replaceAll("\"", "");
		b = b.replaceAll("\\r$", "");
		return b;
    }

    public static String extractAddress (String a) {
    	if (a==null) {
    		return a;
    	}
		String b = cleanString(a);
		if (b.contains("<")) {
			// name-addr case
			b = b.substring(0, b.indexOf(">")+1);
		} else if (b.contains(";")) {
			b = b.substring(0, b.indexOf(";"));
		}
		return b;
    }
    
    public static String extractRemoveUriParams (String a) {
    	if (a==null) {
    		return a;
    	}
		String b = cleanString(a);
		if (b.contains(";")) {
			b = b.substring(0, b.indexOf(";"));
			b= b + ">";
		}
		return b;
    }

    public static String extractTag (String a) {
		String b = null;
		if (a.contains(";tag=")) {
			b = a.substring(a.indexOf(";tag=")+5, a.length()); 	/// extract tag, excluding "tag="
			//b = a.substring(a.indexOf(";tag=")+1, a.length());		/// extract tag, including "tag="
			if (b.contains(";")) {
				b = b.substring(0, b.indexOf(";"));
			}
		}
		return b;
    }

    public static String[] extractParams (String a) {
    	if (a == null || "".equals(a)) {
    		return null;
    	}
		String b = a;
		if (a.contains(">")) {
			b = a.substring(a.indexOf(">")+1, a.length());
		}
		if (b.startsWith(";")) {
			b = b.substring(1);
		}
		if (_logger.isDebugEnabled()) _logger.debug("extractParams b =" + b + ".");
		return b.split(";");
    }
    
    public static String extractNameFromParam(String param){
    	if (param == null) {
    		return null;
    	} else if (param.contains("=")) {
    		return param.substring(0, param.indexOf("="));
    	} else {
    		return param;
    	}
    }

    public static String extractValueFromParam(String param){
    	if (param == null) {
    		return null;
    	} else if (param.contains("=")) {
    		return param.substring(param.indexOf("=") +1, param.length());
    	} else {
    		return null;
    	}
    }

    public static String removeTag (String a) {
		String b = a;
		if (a.contains(";tag=")) {
			b = a.substring(0, a.indexOf(";tag="));
		}
		return b;
    }

    public static String removeTag2 (String a, String tag) {
		String b = a;
		if (tag != null) {
			b = a.replace(tag, "");
		}
		return b;
    }

    public static String getHeaderValue (String a) {
		String b = a;
		if (a.contains(":")) {
			b = a.substring(a.indexOf(":")+1);
		}
		while (b!= null && b.startsWith(" ")) {
			b = b.substring(1);			
		}
		return b;
    }    
    
}
