//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0
package org.apache.jmeter.protocol.sip.utils;

public enum SipCounters {

	SIP_NODE_RESPONSE_TIME("measure.sip.responsetime"),
    SIP_NODE_INBOUND_ANSWER("counter.sip_node.inbound.answer"),
    SIP_NODE_INBOUND_REQUEST("counter.sip_node.inbound.request"),
    SIP_NODE_OUTBOUND_ANSWER("counter.sip_node.outbound.answer"),
    SIP_NODE_OUTBOUND_REQUEST("counter.sip_node.outbound.request"),

    ERROR_SUFFIX ( ".error"),
    SUCCESS_SUFFIX ( ".success"),
    RESULT_SUFFIX ( ".result"),
    TIMEOUT_SUFFIX ( ".timeout"),
    ALL_USER ( "alluser");

	
    private final String value;

    SipCounters(String v) {
        value = v;
    }
    
    public String toString() {
    	return value;
    }
    
}
