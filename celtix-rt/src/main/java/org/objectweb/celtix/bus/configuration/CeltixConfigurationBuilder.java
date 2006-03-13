package org.objectweb.celtix.bus.configuration;

import org.objectweb.celtix.configuration.impl.ConfigurationBuilderImpl;

public class CeltixConfigurationBuilder extends ConfigurationBuilderImpl {
    
    public CeltixConfigurationBuilder() {

        addModel("config-metadata/bus-config.xml");
        addModel("config-metadata/endpoint-config.xml");
        addModel("config-metadata/http-client-config.xml");  
        addModel("config-metadata/http-listener-config.xml");
        addModel("config-metadata/http-server-config.xml");        
        addModel("config-metadata/port-config.xml");

        addModel("config-metadata/service-config.xml");  
        addModel("config-metadata/jms-client-config.xml");
        addModel("config-metadata/jms-server-config.xml");
        addModel("config-metadata/rm-config.xml");
    }
}
