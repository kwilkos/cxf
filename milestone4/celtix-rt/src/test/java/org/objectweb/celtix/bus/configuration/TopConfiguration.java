package org.objectweb.celtix.bus.configuration;

import org.objectweb.celtix.configuration.impl.AbstractConfigurationImpl;


public class TopConfiguration extends AbstractConfigurationImpl {
    
    public TopConfiguration(String id) {
        super("org/objectweb/celtix/bus/configuration/resources/top.xml", id);  
    }
}
