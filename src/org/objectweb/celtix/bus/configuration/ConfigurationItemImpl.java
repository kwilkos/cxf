package org.objectweb.celtix.bus.configuration;

import org.objectweb.celtix.configuration.ConfigurationItem;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.ConfigurationProvider;

public class ConfigurationItemImpl implements ConfigurationItem {
    
    private ConfigurationItemMetadata definition;
    private ConfigurationProvider[] providers;
    
    protected ConfigurationItemImpl(ConfigurationItemMetadata d) {
        definition = d;
    }
    
    public ConfigurationItemMetadata getDefinition() {
        return definition;
    }
    
    public Object getValue() {
        return null;
    }
    
    public ConfigurationProvider[] getProviders() {
        return providers;
    }
    
    public void setProviders(ConfigurationProvider[] p) {
        providers = p;
    }      
}
