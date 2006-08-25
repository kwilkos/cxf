package org.objectweb.celtix.bus.configuration;


public class TopConfiguration extends AbstractConfigurationImpl {

    public TopConfiguration() {
        super(TopConfiguration.class.getResourceAsStream("resources/top.xml"));  
    }
}
