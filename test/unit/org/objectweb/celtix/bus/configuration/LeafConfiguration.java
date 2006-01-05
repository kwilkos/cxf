package org.objectweb.celtix.bus.configuration;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.impl.AbstractConfigurationImpl;

public class LeafConfiguration extends AbstractConfigurationImpl {
    
    public LeafConfiguration(Configuration top, String id) {
        super("org/objectweb/celtix/bus/configuration/resources/leaf.xml", id, top);  
    }
}
