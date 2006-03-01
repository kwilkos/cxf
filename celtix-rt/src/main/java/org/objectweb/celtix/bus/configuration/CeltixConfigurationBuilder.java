package org.objectweb.celtix.bus.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.configuration.impl.ConfigurationBuilderImpl;
import org.objectweb.celtix.configuration.impl.ConfigurationMetadataBuilder;
import org.objectweb.celtix.configuration.impl.ConfigurationMetadataImpl;
import org.objectweb.celtix.resource.DefaultResourceManager;

public class CeltixConfigurationBuilder extends ConfigurationBuilderImpl {
    
    private static final ResourceBundle BUNDLE = 
        BundleUtils.getBundle(CeltixConfigurationBuilder.class);
    
    public CeltixConfigurationBuilder() {

        add("config-metadata/bus-config.xml");
        add("config-metadata/endpoint-config.xml");
        add("config-metadata/http-client-config.xml");  
        add("config-metadata/http-listener-config.xml");
        add("config-metadata/http-server-config.xml");        
        add("config-metadata/port-config.xml");

        add("config-metadata/service-config.xml");  
        add("config-metadata/jms-client-config.xml");
        add("config-metadata/jms-server-config.xml");
        add("config-metadata/rm-config.xml");
    }
    
    private void add(String resource) {
        
        InputStream is = null;
        if (resource != null) {
            is = loadResource(resource);
            if (is == null) {
                throw new ConfigurationException(new Message("METADATA_RESOURCE_EXC", 
                                                             BUNDLE, resource));
            }
        }
        
        ConfigurationMetadata model = null;      
        ConfigurationMetadataBuilder builder = new ConfigurationMetadataBuilder(true);
        if (null != is) {
            try {
                model = builder.build(is);
            } catch (IOException ex) {
                throw new ConfigurationException(new Message("METADATA_RESOURCE_EXC", 
                                                             BUNDLE, resource), ex);
            }
        } else {
            model = new ConfigurationMetadataImpl();
        }
        
        addModel(model);
    }
    
    private InputStream loadResource(String resourceName) { 
        return DefaultResourceManager.instance().getResourceAsStream(resourceName);
    }
    
}
