package org.objectweb.celtix.transports.jms;


import org.objectweb.celtix.configuration.ConfigurationProvider;
import org.objectweb.celtix.service.model.EndpointInfo;


public class ServiceModelJMSConfigurationProvider implements ConfigurationProvider {

    private final EndpointInfo info;
    
    public ServiceModelJMSConfigurationProvider(EndpointInfo i) {
        info = i;       
    }
    
    public Object getObject(String name) {
             
        if (null == info) {
            return null;
        }

        if ("jmsServer".equals(name)) {
            return info.getExtensor(JMSServerBehaviorPolicyType.class);
        }
        
        if ("jmsClient".equals(name)) {
            return info.getExtensor(JMSClientBehaviorPolicyType.class);
        }
        
        if ("jmsAddress".equals(name)) {
            return info.getExtensor(JMSAddressPolicyType.class);
        }

        return null;
    }

    
    public boolean setObject(String name, Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean save() {
        // TODO Auto-generated method stub
        return false;
    }

}
