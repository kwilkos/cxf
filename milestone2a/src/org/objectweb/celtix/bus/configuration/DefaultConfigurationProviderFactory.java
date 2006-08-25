package org.objectweb.celtix.bus.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.ConfigurationProvider;


public final class DefaultConfigurationProviderFactory {
    
    private static final Logger LOG = LogUtils.getL7dLogger(DefaultConfigurationProviderFactory.class);
    
    private static final String DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME = 
        "org.objectweb.celtix.bus.configuration.spring.ConfigurationProviderImpl";
    
    private static final String DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME_PROPERTY = 
        "org.objectweb.celtix.bus.configuration.ConfigurationProvider.default.class";
    
    private static DefaultConfigurationProviderFactory theInstance;
    
    
    private DefaultConfigurationProviderFactory() {
    }
    
    public static DefaultConfigurationProviderFactory getInstance() {
        if (null == theInstance) {
            theInstance = new DefaultConfigurationProviderFactory();
        }
        return theInstance;
    }
    
    public ConfigurationProvider createDefaultProvider(Configuration configuration) {
        
        String className = getDefaultProviderClassName();
       
        Class<? extends ConfigurationProvider> providerClass;
        try {
            providerClass = Class.forName(className).asSubclass(ConfigurationProvider.class);
        } catch (ClassCastException ex) {
            throw new ConfigurationException(new Message("DEFAULT_PROVIDER_INSTANTIATION_EXC", LOG), ex);
        } catch (ClassNotFoundException ex) {
            throw new ConfigurationException(new Message("DEFAULT_PROVIDER_INSTANTIATION_EXC", LOG), ex);
        }
        
        try {
            ConfigurationProvider provider = providerClass.newInstance();
            provider.init(configuration);
            return provider;
        } catch (IllegalAccessException ex) {
            throw new ConfigurationException(new Message("DEFAULT_PROVIDER_INSTANTIATION_EXC", LOG), ex);
        } catch (InstantiationException ex) {
            throw new ConfigurationException(new Message("DEFAULT_PROVIDER_INSTANTIATION_EXC", LOG), ex);
        }
    }
    
    private String getDefaultProviderClassName() {
        
        String providerClass = null;
        
        // check system properties
        providerClass = System.getProperty(DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME_PROPERTY);
        if (null != providerClass && !"".equals(providerClass)) {
            return providerClass;
        }
    
        // next, check for the services stuff in the jar file
        String serviceId = "META-INF/services/" + DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME_PROPERTY;
        InputStream is = ClassLoader.getSystemResourceAsStream(serviceId);
  
        if (is != null) {
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                providerClass = rd.readLine();
                rd.close();
            } catch (UnsupportedEncodingException ex) {
                //we're asking for UTF-8 which is supposed to always be supported,
                //but we'll throw a ConfigurationException anyway
                throw new ConfigurationException(new Message("DEFAULT_PROVIDER_INSTANTIATION_EXC", LOG), ex);
            } catch (IOException ex) {
                throw new ConfigurationException(new Message("DEFAULT_PROVIDER_INSTANTIATION_EXC", LOG), ex);
            }
        }
        
        if (providerClass != null && !"".equals(providerClass)) {
            return providerClass;
        }
        
        // fallback to hardcoced default
        
        return DEFAULT_CONFIGURATION_PROVIDER_CLASSNAME;
    }
}
