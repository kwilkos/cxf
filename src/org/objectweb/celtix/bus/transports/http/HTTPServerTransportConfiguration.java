package org.objectweb.celtix.bus.transports.http;

import javax.wsdl.Port;

import org.objectweb.celtix.bus.configuration.wsdl.WsdlHttpConfigurationProvider;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.impl.AbstractConfigurationImpl;

public class HTTPServerTransportConfiguration extends AbstractConfigurationImpl {
    
    HTTPServerTransportConfiguration(Configuration endpointConfiguration, Port port) {
        super("config-metadata/http-server-config.xml", 
              "http-server", endpointConfiguration); 
        if (null != port) {
            this.getProviders().add(new WsdlHttpConfigurationProvider(port, true));
        }
    }
}
