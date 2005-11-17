package org.objectweb.celtix.bus.transports.http;

import javax.wsdl.Port;

import org.objectweb.celtix.bus.configuration.AbstractConfigurationImpl;
import org.objectweb.celtix.bus.configuration.wsdl.WsdlHttpConfigurationProvider;
import org.objectweb.celtix.configuration.Configuration;

public class HTTPServerTransportConfiguration extends AbstractConfigurationImpl {
    
    HTTPServerTransportConfiguration(Configuration endpointConfiguration, Port port) {
        super(HTTPClientTransportConfiguration.class, "config-metadata/http-server-config.xml", 
              endpointConfiguration); 
        if (null != port) {
            this.getProviders().add(new WsdlHttpConfigurationProvider(port, true));
        }
    }
}
