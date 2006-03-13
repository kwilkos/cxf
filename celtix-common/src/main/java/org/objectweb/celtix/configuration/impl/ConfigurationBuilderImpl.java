package org.objectweb.celtix.configuration.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.resource.DefaultResourceManager;

public class ConfigurationBuilderImpl implements ConfigurationBuilder {

    private static final ResourceBundle BUNDLE = 
        BundleUtils.getBundle(ConfigurationBuilderImpl.class);
    
    private Map<String, ConfigurationMetadata> models;
    
    private Map<String, Map<String, Configuration>> configurations;
    
    public ConfigurationBuilderImpl() {
        models = new HashMap<String, ConfigurationMetadata>();
        configurations = new HashMap<String, Map<String, Configuration>>();

        /*
        add("config-metadata/bus-config.xml");
        add("config-metadata/endpoint-config.xml");
        add("config-metadata/http-client-config.xml");  
        add("config-metadata/http-listener-config.xml");
        add("config-metadata/http-server-config.xml");        
        add("config-metadata/port-config.xml");
        add("config-metadata/service-config.xml");   
        */
    }
    
    public Configuration getConfiguration(String namespaceUri, String id) {
        Map<String, Configuration> instances  = configurations.get(namespaceUri);
        if (null == instances) {
            if (null == getModel(namespaceUri)) {
                throw new ConfigurationException(new Message("UNKNOWN_NAMESPACE_EXC", BUNDLE, namespaceUri));
            }
            return null;
        }
        return instances.get(id);
    }
    
    public Configuration getConfiguration(String namespaceUri, String id, Configuration parent) {
        Configuration c = parent.getChild(namespaceUri, id);
        if (null == c && null == getModel(namespaceUri)) {
            throw new ConfigurationException(new Message("UNKNOWN_NAMESPACE_EXC", BUNDLE, namespaceUri)); 
        }
        return c;
    }
       
    public Configuration buildConfiguration(String namespaceUri, String id, Configuration parent) {
        ConfigurationMetadata model = getModel(namespaceUri);
        if (null == model) {
            throw new ConfigurationException(new Message("UNKNOWN_NAMESPACE_EXC", BUNDLE, namespaceUri));
        }
        Configuration c = new AbstractConfigurationImpl(model, id, parent);
        if (null == parent) {
            Map<String, Configuration> instances = configurations.get(namespaceUri);
            if (null == instances) {
                instances = new HashMap<String, Configuration>();
                configurations.put(namespaceUri, instances);
            }
            instances.put(id, c);
        }  
        return c;      
    }

    public Configuration buildConfiguration(String namespaceUri, String id) {
        return buildConfiguration(namespaceUri, id, null);
    }
    
    public final void addModel(ConfigurationMetadata model) {
        models.put(model.getNamespaceURI(), model);
    }
    
    public ConfigurationMetadata getModel(String namespaceUri) {
        return models.get(namespaceUri);
    }

    public void addModel(String resource) {
        
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
