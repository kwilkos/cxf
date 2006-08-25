package org.objectweb.celtix.bus.jaxws;

import javax.xml.namespace.QName;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.AbstractConfigurationImpl;

public class ServiceConfiguration extends AbstractConfigurationImpl {
    
    ServiceConfiguration(Bus bus, QName name) {
        super("config-metadata/service-config.xml", 
              name, bus.getConfiguration()); 
    }
}
