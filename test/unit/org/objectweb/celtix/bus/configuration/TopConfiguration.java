package org.objectweb.celtix.bus.configuration;


public class TopConfiguration extends AbstractConfigurationImpl {
    
    public TopConfiguration(String id) {
        super(TopConfiguration.class, "resources/top.xml", id);  
    }
}
