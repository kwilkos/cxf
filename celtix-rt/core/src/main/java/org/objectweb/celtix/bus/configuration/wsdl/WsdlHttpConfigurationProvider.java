package org.objectweb.celtix.bus.configuration.wsdl;

import java.util.List;

import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationProvider;
import org.objectweb.celtix.transports.http.configuration.HTTPClientPolicy;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;

public class WsdlHttpConfigurationProvider implements ConfigurationProvider {
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
        if ((serverType && "httpServer".equals(name)) || (!serverType && "httpClient".equals(name))) {
            List<?> list = port.getExtensibilityElements();
            for (Object ep : list) {
                ExtensibilityElement ext = (ExtensibilityElement)ep;
                if ((serverType && ext instanceof HTTPServerPolicy)
                    || (!serverType && ext instanceof HTTPClientPolicy)) {
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
