package org.objectweb.celtix.bus.configuration;

import org.objectweb.celtix.configuration.Configuration;

public class LeafConfiguration extends AbstractConfigurationImpl {
    
    public LeafConfiguration(Configuration top) {
        super(LeafConfiguration.class.getResource("resources/leaf.xml"), top);     
    }
}
