package org.objectweb.celtix.bus.jaxws;

import javax.xml.namespace.QName;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.AbstractConfigurationImpl;

public class ServiceConfiguration extends AbstractConfigurationImpl {
    
    private QName serviceName;
    
    ServiceConfiguration(Bus bus, QName name) {
        super(ServiceConfiguration.class.getResourceAsStream("configuration/service-configuration.xml"), 
              bus.getConfiguration());       
        serviceName = name;
    }
    
    public Object getId() {
        return serviceName;
    }
}
