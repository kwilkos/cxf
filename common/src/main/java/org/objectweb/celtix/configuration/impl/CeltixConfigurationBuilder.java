package org.objectweb.celtix.configuration.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.ConfigurationProvider;

public class CeltixConfigurationBuilder extends ConfigurationBuilderImpl {
    
    public CeltixConfigurationBuilder() {
    }
    
    public CeltixConfigurationBuilder(URL url) {
        super(url);
    }  
    
    public Configuration buildConfiguration(String namespaceUri, String id, Configuration parent) {
        ConfigurationMetadata model = getModel(namespaceUri);
        if (null == model) {
            throw new ConfigurationException(new Message("UNKNOWN_NAMESPACE_EXC", BUNDLE, namespaceUri));
        }
        
        if (parent == null && !isValidTopConfiguration(model, parent)) {
            throw new ConfigurationException(new Message("INVALID_TOP_CONFIGURATION",
                                                         BUNDLE, namespaceUri));
        }

        Configuration c = new ConfigurationImpl(model, id, parent);
        if (null == parent) {
            Map<String, Configuration> instances = configurations.get(namespaceUri);
            if (null == instances) {
                instances = new HashMap<String, Configuration>();
                configurations.put(namespaceUri, instances);
            }
            instances.put(id, c);
        }
        
        DefaultConfigurationProviderFactory factory = DefaultConfigurationProviderFactory.getInstance();
        ConfigurationProvider defaultProvider = factory.createDefaultProvider();
        
        inject(defaultProvider, c);

        if (null != defaultProvider) {
            c.getProviders().add(defaultProvider); 
        }
        
        return c;
    }
}
