package org.objectweb.celtix.configuration;

import java.util.List;

import javax.xml.namespace.QName;

public interface Configuration {
   
    /** 
     * Returns the unique identifier for this configuration instance.
     * 
     * @return the identifier for this configuration.
     */
    QName getName();
    
    /** 
     * Returns the <code>Configurator</code> associated with this <code>Configuration</code>.
     *
     * @return the configuration's configurator object.
     */
    Configurator getConfigurator();
    
    /**
     * Returns the configuration metadata model for this <code>Configuration</code>.
     * 
     * @return the configuration metadata model.
     */
    ConfigurationMetadata getModel();
    
    /**
     * Sets the list of configuration providers for this configuration - these will be 
     * consulted in orde when looking up the value for a particular configuration item.
     * 
     * @param providers the configuration providers to use for this configuration.
     */
    void setProviders(ConfigurationProvider[] providers);
    
    /**
     * Returns the list of configuration providers for this configuration.
     * 
     * @return the list of configuration providers for this configuration.
     */
    ConfigurationProvider[] getProviders();
    
    /**
     * Returns the object holding the value for the configuration item with the specified name. 
     * The runtime class of this object is determined by the jaxb mapping of the configuration
     * item's type, e.g. for a boolean item it is an instance of java.lang.Boolean.
     * 
     * @throws ConfigurationException if no such item is defined in this configuration's 
     * metadata model, or if no value for this item can be found in either this configuration
     * or any of its parent configuration's and if no default value is specified for the
     * item in the metadata model. 
     * 
     * @param name the name of the configuration item.
     * @return the object holding the configuration item's value.
     */   
    Object getObject(String name);
    
    /** Convenience method to extract the value of a boolean type configuration item from
     * its holder object.
     * 
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    boolean getBoolean(String name);
    
    /** Convenience method to extract the value of a int type configuration item from
     * its holder object.
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    int getInteger(String name);
    
    /** Convenience method to extract the value of a long type configuration item from
     * its holder object.
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    long getLong(String name);
    
    /** Convenience method to extract the value of a double type configuration item from
     * its holder object.
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    double getDouble(String name);
    
    /** Convenience method to extract the value of a string type configuration item from
     * its holder object.
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    String getString(String name);
    
    /** Convenience method to extract the value of a string list type configuration item from
     * its holder object.
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    List<String> getStringList(String name);
       
       
}
