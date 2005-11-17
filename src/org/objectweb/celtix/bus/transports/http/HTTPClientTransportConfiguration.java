package org.objectweb.celtix.bus.transports.http;

import javax.wsdl.Port;

import org.objectweb.celtix.bus.configuration.AbstractConfigurationImpl;
import org.objectweb.celtix.bus.configuration.wsdl.WsdlHttpConfigurationProvider;
import org.objectweb.celtix.configuration.Configuration;

public class HTTPClientTransportConfiguration extends AbstractConfigurationImpl {

    HTTPClientTransportConfiguration(Configuration portConfiguration, Port port) {
        super(HTTPClientTransportConfiguration.class, "config-metadata/http-client-config.xml", 
              portConfiguration); 
        if (null != port) {
            this.getProviders().add(new WsdlHttpConfigurationProvider(port, false));
        }
    }
}
