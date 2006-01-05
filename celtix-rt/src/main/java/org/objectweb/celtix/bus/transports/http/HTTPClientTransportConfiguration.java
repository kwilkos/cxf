package org.objectweb.celtix.bus.transports.http;

import javax.wsdl.Port;

import org.objectweb.celtix.bus.configuration.wsdl.WsdlHttpConfigurationProvider;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.impl.AbstractConfigurationImpl;

public class HTTPClientTransportConfiguration extends AbstractConfigurationImpl {

    HTTPClientTransportConfiguration(Configuration portConfiguration, Port port) {
        super("config-metadata/http-client-config.xml", 
              "http-client", portConfiguration); 
        if (null != port) {
            this.getProviders().add(new WsdlHttpConfigurationProvider(port, false));
        }
    }
}
