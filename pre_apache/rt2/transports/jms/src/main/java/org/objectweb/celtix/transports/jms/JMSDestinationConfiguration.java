package org.objectweb.celtix.transports.jms;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.transports.jms.jms_conf.JMSServerConfig;

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
