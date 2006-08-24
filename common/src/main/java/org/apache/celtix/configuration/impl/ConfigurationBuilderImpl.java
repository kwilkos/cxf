package org.apache.cxf.configuration.impl;

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


import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.injection.ResourceInjector;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.CompoundName;
import org.apache.cxf.configuration.Configuration;
import org.apache.cxf.configuration.ConfigurationBuilder;
import org.apache.cxf.configuration.ConfigurationException;
import org.apache.cxf.configuration.ConfigurationMetadata;
import org.apache.cxf.configuration.ConfigurationProvider;
import org.apache.cxf.resource.DefaultResourceManager;
import org.apache.cxf.resource.PropertiesResolver;
import org.apache.cxf.resource.ResourceManager;
import org.springframework.core.io.UrlResource;

public class ConfigurationBuilderImpl implements ConfigurationBuilder {
        
    protected static final ResourceBundle BUNDLE =
        BundleUtils.getBundle(ConfigurationBuilderImpl.class);
    private static final Logger LOG = LogUtils.getL7dLogger(ConfigurationBuilderImpl.class);
    private static final String METADATA_RESOURCE_MAPPINGS_RESOURCE 
        = "META-INF/config-metadata-mappings.xml";

    protected Map<CompoundName, Configuration> configurations;
    private Map<String, ConfigurationMetadata> models;
    private URL url;

    public ConfigurationBuilderImpl() {
        this(null);        
    }

    public ConfigurationBuilderImpl(URL u) {
        models = new HashMap<String, ConfigurationMetadata>();
        configurations = new HashMap<CompoundName, Configuration>();
        url = u;
    }
    
    public URL getURL() {
        return url;
    }

    public Configuration getConfiguration(String namespaceUri, CompoundName id) {
        
        Configuration c = configurations.get(id);
        if (null == c) {
            c = buildConfiguration(namespaceUri, id);
        }
        return c;
    }
  

    public ConfigurationMetadata getModel(String namespaceUri) {
        ConfigurationMetadata model = models.get(namespaceUri);
        if (null == model) {
            String resourceName = getResourceName(namespaceUri);
            if (null != resourceName) {
                model = loadModel(resourceName);
                addModel(namespaceUri, model);
            }
        }
        return model;
    }
    
    public void addModel(String namespaceURI, ConfigurationMetadata model) {
        models.put(namespaceURI, model);
    }

    
    protected Configuration buildConfiguration(String namespaceUri, CompoundName id) {
        ConfigurationMetadata model = getModel(namespaceUri);
        if (null == model) {
            throw new ConfigurationException(new Message("UNKNOWN_NAMESPACE_EXC", BUNDLE, namespaceUri));
        }

        Configuration c = new ConfigurationImpl(model, id);
        configurations.put(id, c);
        
        DefaultConfigurationProviderFactory factory = DefaultConfigurationProviderFactory.getInstance();
        ConfigurationProvider defaultProvider = factory.createDefaultProvider();
  
        inject(defaultProvider, c);

        if (null != defaultProvider) {
            c.getProviders().add(defaultProvider); 
        }
        
        return c;
    }
    
    protected void inject(ConfigurationProvider provider, Configuration configuration) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("configuration", configuration);
        properties.put("url", url);
        PropertiesResolver resolver = new PropertiesResolver(properties);
        ResourceManager manager = new DefaultResourceManager(resolver);
        ResourceInjector injector = new ResourceInjector(manager);
        injector.inject(provider);
    }
    
    private String getResourceName(String namespaceURI) {
        String resourceName = null;
        Enumeration<URL> candidates;
        try { 
            candidates = Thread.currentThread().getContextClassLoader()
                .getResources(METADATA_RESOURCE_MAPPINGS_RESOURCE);
            while (candidates.hasMoreElements()) {
                URL u = candidates.nextElement();
                UrlResource ur = new UrlResource(u); 
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
