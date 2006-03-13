package org.objectweb.celtix.configuration;


public interface ConfigurationBuilder {
    String CONFIGURATION_BUILDER_CLASS_PROPERTY = "org.objectweb.celtix.ConfigurationBuilderClass";
    
    /**
     * Returns the top level <code>Configuration</code> object with the specified namespace and
     * identifer or null if such a configuration does not exist.
     * @throws ConfigurationException if no configuration metadata for the specified namespace is 
     * available.
     * @param namespaceUri the configuration namespace.
     * @param id the configuration identifier.
     * @return the configuration.
     */
    Configuration getConfiguration(String namespaceUri, String id);
    
    /**
     * Returns the <code>Configuration</code> object with the specified namespace and
     * identifer that is a child of the specified parent configuration, or null if such a 
     * configuration does not exist.
     * @throws ConfigurationException if no configuration metadata for the specified namespace is 
     * available.
     * @param namespaceURI the configuration namespace.
     * @param id the configuration identifier.
     * @param parent the parent configuration.
     * @return the configuration.
     */
    Configuration getConfiguration(String namespaceURI, String id, Configuration parent);
    
    /** 
     * Creates a new top level <code>Configuration</code> object for the given namepace 
     * with the specified identifier. 
     * @param namespaceUri the configuration namespace.
     * @param id the configuration identifier.
     * @return the newly created configuration.
     */
    Configuration buildConfiguration(String namespaceUri, String id);
    
    /**
     * Creates a new <code>Configuration</code> object for the given namepace as a child
     * of the specified parent configuration and with the specified identifier.
     * @paam namespaceUri the configuration namespace.
     * @param id the configuration identifier.
     * @param parent the parent configuration.
     * @return the newly created configuration.
     */
    Configuration buildConfiguration(String namespaceUri, String id, Configuration parent);
    
    /**
     * Stores the specified configuration model with the builder.
     * @param model the configuration metadata model.
     */
    void addModel(ConfigurationMetadata model);

    /**
     * Stores the specified configuration model with the builder.
     * @param resource url to the configuration metadata model.
     */
    void addModel(String resource);
    
    /**
     * Returns the configuration metadata model for the given namespace or null if no such
     * model is stored in this builder.
     * @param namespaceURI the configuration namespace.
     * @return the configuration metadata model.
     */
    ConfigurationMetadata getModel(String namespaceURI);
    
}
