package org.objectweb.celtix.bus;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.extension.ExtensionManagerImpl;
import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.resource.DefaultResourceManager;
import org.objectweb.celtix.resource.PropertiesResolver;
import org.objectweb.celtix.resource.ResourceManager;
import org.objectweb.celtix.resource.ResourceResolver;
import org.objectweb.celtix.resource.SinglePropertyResolver;

public class CeltixBus implements Bus {
    
    public static final String BUS_PROPERTY_NAME = "bus";
    
    private static final String BUS_EXTENSION_RESOURCE = "META-INF/bus-extensions.xml";
    
    private List<Interceptor> inInterceptors;
    private List<Interceptor> outInterceptors;
    private List<Interceptor> faultInterceptors;
    private Map<Class, Object> extensions;
//    private ConfigurationBuilder configurationBuilder;
    private Configuration configuration;
    
    public CeltixBus() {
        this(new HashMap<Class, Object>());
    }

    public CeltixBus(Map<Class, Object> e) {
        this(e, null);
    }
    
    public CeltixBus(Map<Class, Object> e, Map<String, Object> properties) {
        
        extensions = e;
        
        ResourceManager resourceManager = new DefaultResourceManager();
        if (null != properties) {
            ResourceResolver propertiesResolver = new PropertiesResolver(properties);
            resourceManager.addResourceResolver(propertiesResolver);
        }
        
        ResourceResolver busResolver = new SinglePropertyResolver(BUS_PROPERTY_NAME, this);
        resourceManager.addResourceResolver(busResolver);
   
        new ExtensionManagerImpl(BUS_EXTENSION_RESOURCE, 
                                                    Thread.currentThread().getContextClassLoader(),
                                                    extensions,
                                                    resourceManager);
        
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
//        
//        properties.put(BusConfigurationBuilder.BUS_ID_PROPERTY, BUS_PROPERTY_NAME);
//        properties.put(BUS_PROPERTY_NAME, this);
//        
//        configurationBuilder = new CeltixConfigurationBuilder();       
//        configuration = new BusConfigurationBuilder().build(configurationBuilder, properties);

    }
    
    public List<Interceptor> getFaultInterceptors() {
        return faultInterceptors;
    }

    public List<Interceptor> getInInterceptors() {
        return inInterceptors;
    }    

    public List<Interceptor> getOutInterceptors() {
        return outInterceptors;
    }
      
    public <T> T getExtension(Class<T> extensionType) {
        Object obj = extensions.get(extensionType);
        if (null != obj) {
            return extensionType.cast(obj);
        }
        return null;
    }

    public <T> void setExtension(T extension, Class<T> extensionType) {
        extensions.put(extensionType, extension);
    }

    // TODO:
    public Configuration getConfiguration() {
        return configuration;
    }
}
