package org.apache.cxf.transport.jms;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transports.jms.JMSClientBehaviorPolicyType;
import org.apache.cxf.transports.jms.jms_conf.JMSClientConfig;



public class JMSConduitConfiguration extends JMSConfiguration {
    private JMSClientBehaviorPolicyType jmsClientPolicy;
    private JMSClientConfig jmsClientConfig;
    
    public JMSConduitConfiguration(Bus bus, EndpointInfo endpointInfo) { 
        super(bus, endpointInfo, false);       
        jmsClientPolicy = getClientPolicy();
        jmsClientConfig = getClientConfig();
    }
     
    private JMSClientBehaviorPolicyType getClientPolicy() {
        JMSClientBehaviorPolicyType pol = 
            configuration.getObject(JMSClientBehaviorPolicyType.class, "jmsClient");
        if (pol == null) {
            pol = new JMSClientBehaviorPolicyType();
        }
        return pol;
    }
    
    private JMSClientConfig getClientConfig() {
        JMSClientConfig clientConf = configuration.getObject(JMSClientConfig.class, "jmsClientConfig");
        if (clientConf == null) {
            clientConf = new JMSClientConfig();
        }
        
        return clientConf;
    }
    
    public JMSClientBehaviorPolicyType getJMSClientBehaviorPolicyType() {
        return jmsClientPolicy;
    }
    
    public JMSClientConfig getClientConfiguration() {
        return jmsClientConfig;
    }
        
        
   
}
