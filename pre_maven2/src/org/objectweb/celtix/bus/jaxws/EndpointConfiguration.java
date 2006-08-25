package org.objectweb.celtix.bus.jaxws;

import javax.xml.namespace.QName;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.AbstractConfigurationImpl;

public class EndpointConfiguration extends AbstractConfigurationImpl {
    
    EndpointConfiguration(Bus bus, QName name) {
        super("config-metadata/endpoint-config.xml", 
              name, bus.getConfiguration());       
    }
}
