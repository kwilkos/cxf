package org.objectweb.celtix.bus.configuration;

import org.objectweb.celtix.configuration.Configuration;

public class LeafConfiguration extends AbstractConfigurationImpl {
    
    public LeafConfiguration(Configuration top, String id) {
        super(LeafConfiguration.class, "resources/leaf.xml", id, top);  
    }
}
