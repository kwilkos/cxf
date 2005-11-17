package org.objectweb.celtix.bus.configuration.wsdl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;

import org.objectweb.celtix.bus.configuration.spring.ConfigurationProviderImpl;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationProvider;
import org.objectweb.celtix.transports.http.configuration.ClientType;
import org.objectweb.celtix.transports.http.configuration.ServerType;

public class WsdlHttpConfigurationProvider implements ConfigurationProvider {
    private static final Logger LOG = LogUtils.getL7dLogger(ConfigurationProviderImpl.class);
    private final Port port;
    private final boolean serverType;
    
    public WsdlHttpConfigurationProvider(Port p, boolean s) {
        port = p;
        serverType = s;
    }
    
    
    public void init(Configuration configuration) {
        // not needed
    }
    
    public Object getObject(String name) {
        if (null == port) {
            return null;
        }
        List<?> list = port.getExtensibilityElements();
        for (Object ep : list) {
            ExtensibilityElement ext = (ExtensibilityElement)ep;
            if ((serverType && ext instanceof ServerType)
                || (!serverType && ext instanceof ClientType)) {
                StringBuffer methodName = new StringBuffer("get");
                methodName.append(name);
                methodName.setCharAt(3, Character.toUpperCase(methodName.charAt(3)));
                try {
                    Class cl = serverType ? ServerType.class : ClientType.class;
                    Method m = cl.getMethod(methodName.toString());
                    return m.invoke(ext);
                } catch (Exception ex) {          
                    throw new ConfigurationException(new Message("PROVIDER_EXC", LOG, name), ex);
                }                
            }
        }
        return null;
    }
}
