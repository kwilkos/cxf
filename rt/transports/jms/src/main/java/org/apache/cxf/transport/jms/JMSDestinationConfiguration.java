package org.apache.cxf.transport.jms;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transports.jms.JMSServerBehaviorPolicyType;
import org.apache.cxf.transports.jms.jms_conf.JMSServerConfig;

public class JMSDestinationConfiguration extends JMSConfiguration {
    
    
    public JMSDestinationConfiguration(Bus bus, EndpointInfo endpointInfo) {
        super(bus, endpointInfo, true);
    }
    
    public JMSServerBehaviorPolicyType getJMSServerBehaviorPolicy() {
        JMSServerBehaviorPolicyType pol = 
            configuration.getObject(JMSServerBehaviorPolicyType.class, 
                                    "jmsServer");
        if (pol == null) {
            pol = new JMSServerBehaviorPolicyType();
        }
        return pol;
    }
          
    public JMSServerConfig getServerConfig() {
        JMSServerConfig serverConf = 
            configuration.getObject(JMSServerConfig.class, "jmsServerConfig");
        if (serverConf == null) {
            serverConf = new JMSServerConfig();
        }
        return serverConf;
    }
    
    
}
