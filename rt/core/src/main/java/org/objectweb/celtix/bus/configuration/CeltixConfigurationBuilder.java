package org.objectweb.celtix.bus.configuration;

import java.net.URL;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.configuration.CompoundName;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.ConfigurationProvider;
import org.objectweb.celtix.configuration.impl.ConfigurationBuilderImpl;
import org.objectweb.celtix.configuration.impl.DefaultConfigurationProviderFactory;

public class CeltixConfigurationBuilder extends ConfigurationBuilderImpl {
    
    public CeltixConfigurationBuilder() {
    }
    
    public CeltixConfigurationBuilder(URL url) {
        super(url);
    }  
    
    public Configuration buildConfiguration(String namespaceUri, CompoundName id) {
        ConfigurationMetadata model = getModel(namespaceUri);
        if (null == model) {
            throw new ConfigurationException(new Message("UNKNOWN_NAMESPACE_EXC", BUNDLE, namespaceUri));
        }
        
        Configuration c = new CeltixConfigurationImpl(model, id);
        configurations.put(id, c);
        
        DefaultConfigurationProviderFactory factory = DefaultConfigurationProviderFactory.getInstance();
        ConfigurationProvider defaultProvider = factory.createDefaultProvider();
        
        inject(defaultProvider, c);

        if (null != defaultProvider) {
            c.getProviders().add(defaultProvider); 
        }
        
        return c;
    }
}
