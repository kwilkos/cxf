package org.objectweb.celtix.bus.configuration;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationProvider;

public class TestConfigurationProvider implements ConfigurationProvider {

    public Object getObject(String name) {
        return null;
    }
    
    public boolean setObject(String name, Object value) {
        return false;
    }

    public void init(Configuration configuration) {        
    }

}
