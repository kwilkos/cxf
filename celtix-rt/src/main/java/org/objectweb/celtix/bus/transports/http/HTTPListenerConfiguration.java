package org.objectweb.celtix.bus.transports.http;


import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.impl.AbstractConfigurationImpl;

public class HTTPListenerConfiguration extends AbstractConfigurationImpl {
    
    HTTPListenerConfiguration(Bus bus, int port) {
        super("config-metadata/http-listener-config.xml", 
              "http-listener." + port, bus.getConfiguration()); 
    }
}
