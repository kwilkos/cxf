package org.objectweb.celtix.bus.configuration.wsdl;

import java.util.List;

import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;

import org.objectweb.celtix.configuration.ConfigurationProvider;
import org.objectweb.celtix.transports.jms.JMSAddressPolicyType;
import org.objectweb.celtix.transports.jms.JMSClientBehaviorPolicyType;
import org.objectweb.celtix.transports.jms.JMSServerBehaviorPolicyType;

public class WsdlJMSConfigurationProvider implements ConfigurationProvider {
    private final Port port;
    private final boolean serverType;

    public WsdlJMSConfigurationProvider(Port p, boolean s) {
        port = p;
        serverType = s;
    }

    public Object getObject(String name) {
        if (null == port) {
            return null;
        }
        if ((serverType && "jmsServer".equals(name)) 
            || (!serverType && "jmsClient".equals(name))
            || "jmsAddress".equals(name)) {
            List<?> list = port.getExtensibilityElements();
            for (Object ep : list) {
                ExtensibilityElement ext = (ExtensibilityElement)ep;
                if (("jmsServer".equals(name) && ext instanceof JMSServerBehaviorPolicyType) 
                    || ("jmsClient".equals(name) && ext instanceof JMSClientBehaviorPolicyType)
                    || ("jmsAddress".equals(name) && ext instanceof JMSAddressPolicyType)) {
                    return ext;
                }
            }
        }
        return null;
    }

    /**
     * TODO
     */
    public boolean setObject(String name, Object value) {
        return false;
    }

    public boolean save() {
        //TODO:
        return false;
    }

}
