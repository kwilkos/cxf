package org.objectweb.celtix.configuration.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationMetadata;
import org.objectweb.celtix.resource.DefaultResourceManager;
import org.springframework.core.io.UrlResource;

public class ConfigurationBuilderImpl implements ConfigurationBuilder {
        
    protected static final ResourceBundle BUNDLE =
        BundleUtils.getBundle(ConfigurationBuilderImpl.class);
    private static final Logger LOG = LogUtils.getL7dLogger(ConfigurationBuilderImpl.class);
    private static final String METADATA_RESOURCE_MAPPINGS_RESOURCE 
        = "META-INF/config-metadata-mappings.xml";

    protected Map<String, Map<String, Configuration>> configurations;
    private Map<String, ConfigurationMetadata> models;


    public ConfigurationBuilderImpl() {
        models = new HashMap<String, ConfigurationMetadata>();
        configurations = new HashMap<String, Map<String, Configuration>>();
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
        if (parent == null) {
            return null;
        }

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
        /*
        if (parent != null && !isValidChildConfiguration(model, parent)) {
            throw new ConfigurationException(new Message("INVALID_CHILD_CONFIGURATION",
                                                         BUNDLE, namespaceUri,
                                                         parent.getModel().getNamespaceURI()));
        }
        */
        if (parent == null && !isValidTopConfiguration(model, parent)) {
            throw new ConfigurationException(new Message("INVALID_TOP_CONFIGURATION",
                                                         BUNDLE, namespaceUri));
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

    /* The presence of parentNamespaceURI means that the underlying type of
     * configuration can only be created as a child of a configuration with
     * namespace 'parentNamespace'. The absence of this attribute means that it
     * can be be created as a top level configuration object
     **/
    protected boolean isValidTopConfiguration(ConfigurationMetadata model, Configuration parent) {
        String parentNamespaceURI = model.getParentNamespaceURI();

        if (parentNamespaceURI == null || "".equals(parentNamespaceURI)) {
            return true;
        }

        return false;
    }

    /*
    private boolean isValidChildConfiguration(ConfigurationMetadata model, Configuration parent) {
        if (parent != null && parent.getModel().getNamespaceURI().equals(model.getParentNamespaceURI())) {
            return true;
        }

        return false;
    }
    */

    public Configuration buildConfiguration(String namespaceUri, String id) {
        return buildConfiguration(namespaceUri, id, null);
    }

    public final void addModel(ConfigurationMetadata model) {
        models.put(model.getNamespaceURI(), model);
    }

    public ConfigurationMetadata getModel(String namespaceUri) {
        ConfigurationMetadata model = models.get(namespaceUri);
        if (null == model) {
            String resourceName = getResourceName(namespaceUri);
            if (null != resourceName) {
                model = loadModel(resourceName);
                addModel(model);
            }
        }
        return model;
    }

    public void addModel(String resource) {        
        addModel(loadModel(resource));
    }
    
    public void clearConfigurations() {
        configurations.clear();
    }
    
    public void clearModels() {
        models.clear();
    }
    
    private String getResourceName(String namespaceURI) {
        String resourceName = null;
        Enumeration<URL> candidates;
        try { 
            candidates = Thread.currentThread().getContextClassLoader()
                .getResources(METADATA_RESOURCE_MAPPINGS_RESOURCE);
            while (candidates.hasMoreElements()) {
                URL url = candidates.nextElement();
                UrlResource ur = new UrlResource(url); 
                Properties mappings = new Properties();
                mappings.loadFromXML(ur.getInputStream()); 
                resourceName = mappings.getProperty(namespaceURI);
                if (null != resourceName) {
                    break;
                }
            }
        } catch (IOException ex) {
            Message msg = new Message("CANNOT_FIND_METADATA_FOR_NAMESPACE_MSG", BUNDLE, namespaceURI);
            LOG.log(Level.WARNING, msg.toString(), ex);
            return null;
        }
        if (null == resourceName) {
            Message msg = new Message("CANNOT_FIND_METADATA_FOR_NAMESPACE_MSG", BUNDLE, namespaceURI);
            LOG.log(Level.WARNING, msg.toString());
        }
        return resourceName;
    }

    private InputStream loadResource(String resourceName) {
        return DefaultResourceManager.instance().getResourceAsStream(resourceName);
    }
    
    private ConfigurationMetadata loadModel(String resource) {
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

        return model;
    }
}
