//© Copyright 2018 Hewlett Packard Enterprise Development LP
//Licensed under Apache License version 2.0: http://www.apache.org/licenses/LICENSE-2.0


package org.apache.jmeter.protocol.sip.config;

import java.beans.PropertyDescriptor;
import org.apache.jmeter.protocol.sip.config.SipNodeElement;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


public class SipNodeElementBeanInfo extends BeanInfoSupport {
	Logger _logger = LoggingManager.getLoggerForClass();
	

	public SipNodeElementBeanInfo() {
		super(SipNodeElement.class);
		
		createPropertyGroup("chooseNodeName", new String[] { 
				"sipNodeName",
		});
		
		createPropertyGroup("stackProperties", new String[] {
				"traficType","autoAnswerTimeout", "queueTimeout","sessionLifeTime", "outboundProxy", "ignoreNonReliableResponseRetransmission", "ignoreReliableResponseRetransmission","automaticQueuesCleanup" });
				
		createPropertyGroup("listeningPoint", new String[] { 
				"localIP", "localPort", "localTransport", "maxConnections", "cacheConnections"
		});

		createPropertyGroup("selectIdentificationHeader", new String[] { 
				"identificationHeader",
		});

		PropertyDescriptor p = property("sipNodeName");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);		
		p.setValue(DEFAULT,"");
		
		p = property("traficType");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT,SipNodeElement.FUNCTIONAL_TRAFIC);
		p.setValue(TAGS,new String[]{				
				SipNodeElement.FUNCTIONAL_TRAFIC,
				SipNodeElement.PERFORMANCE_TRAFIC,
		});		
				
		p = property("autoAnswerTimeout");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "0");
				
		p = property("sessionLifeTime");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "30000");
		p = property("queueTimeout");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "5000");
		p = property("outboundProxy");
        p.setValue(NOT_UNDEFINED, Boolean.FALSE);
        p = property("ignoreNonReliableResponseRetransmission");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT,SipNodeElement.YES);
        p.setValue(TAGS,new String[]{
                SipNodeElement.YES,
                SipNodeElement.NO,
        });
        p = property("ignoreReliableResponseRetransmission");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT,SipNodeElement.YES);
        p.setValue(TAGS,new String[]{
                SipNodeElement.YES,
                SipNodeElement.NO,
        });
        p = property("automaticQueuesCleanup");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT,SipNodeElement.NO);
        p.setValue(TAGS,new String[]{
                SipNodeElement.YES,
                SipNodeElement.NO,
        });
		
		p = property("localPort");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT,"");
		p = property("localIP");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");
		p = property("localTransport");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT,SipNodeElement.TRANSPORT_TCP);
		p.setValue(TAGS,new String[]{
				SipNodeElement.TRANSPORT_TCP,
				SipNodeElement.TRANSPORT_UDP,
				SipNodeElement.TRANSPORT_SCTP,
		});
		p = property("maxConnections");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "1");
        p = property("cacheConnections");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT,SipNodeElement.NO);
        p.setValue(TAGS,new String[]{
                SipNodeElement.YES,
                SipNodeElement.NO,
        });

		
		p = property("identificationHeader");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT,"To");
		p.setValue(TAGS,new String[]{
				"To",
				"From",
				"Request-URI",
				"Call-ID",
		});
	}
}
