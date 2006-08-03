package org.objectweb.celtix.bus.configuration;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.impl.ConfigurationMetadataBuilder;
import org.objectweb.celtix.resource.DefaultResourceManager;


public class TopConfigurationBuilder {
    public TopConfigurationBuilder() {        
    }
    
    public Configuration build(String id) {
        ConfigurationBuilder cb = new CeltixConfigurationBuilder();     
        ConfigurationMetadataBuilder builder = new ConfigurationMetadataBuilder(true);
        InputStream is = DefaultResourceManager.instance()
            .getResourceAsStream("org/objectweb/celtix/bus/configuration/resources/top.xml");
        ConfigurationMetadata model = null;
        try {
            model = builder.build(is);
        } catch (IOException ex) {
            // ignore
        }
        cb.addModel(model);
        return cb.buildConfiguration(model.getNamespaceURI(), id);
    }
}
