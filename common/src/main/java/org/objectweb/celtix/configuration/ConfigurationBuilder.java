package org.objectweb.celtix.configuration;

import java.net.URL;


public interface ConfigurationBuilder {
    
    /**
     * Returns the configurations builders's url which it provides to the <code>configuration</code> 
     * objects it creates and their providers.
     * 
     * @return the builder's url
     */
    URL getURL();
    
    /**
     * Returns the <code>Configuration</code> object with the specified namespace and
     * identifer, creating it if necessary.
     * @throws ConfigurationException if no configuration metadata for the specified namespace is 
     * available.
     * @param namespaceUri the configuration namespace.
     * @param id the configuration identifier.
     * @return the configuration.
     */
    Configuration getConfiguration(String namespaceUri, CompoundName id);
    
    /**
     * Returns the configuration metadata model for the given namespace or null if no such
     * model is stored in this builder.
     * @param namespaceURI the configuration namespace.
     * @return the configuration metadata model.
     */
    ConfigurationMetadata getModel(String namespaceURI);
    
    void addModel(String namespaceURI, ConfigurationMetadata model);
     
}
