package org.objectweb.celtix.bus.configuration;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.impl.ConfigurationMetadataBuilder;
import org.objectweb.celtix.resource.DefaultResourceManager;

public class LeafConfigurationBuilder {
    
    public LeafConfigurationBuilder() {        
    }
    
    public Configuration build(Configuration top, String id) {
        ConfigurationBuilder cb = null;
        cb = ConfigurationBuilderFactory.getBuilder(null);
        
        ConfigurationMetadataBuilder builder = new ConfigurationMetadataBuilder(true);
        InputStream is = DefaultResourceManager.instance()
            .getResourceAsStream("org/objectweb/celtix/bus/configuration/resources/leaf.xml");
        ConfigurationMetadata model = null;
        try {
            model = builder.build(is);
        } catch (IOException ex) {
            // ignore
        }
        cb.addModel(model);
        return cb.buildConfiguration(model.getNamespaceURI(), id, top);
    }
}
