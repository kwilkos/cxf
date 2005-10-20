package org.objectweb.celtix.configuration;

import java.util.List;

public interface Configuration {
   
    /**
     * Returns the (instance) identifier of this configuration. This should be unique among all 
     * instances of the same type, and would typically be implemented as a string (e.g. the bus id),
     * but other types, e.g. QName, may also be used.
     * 
     * The combination of this identifier with the namespaceURI of the associated metadata model 
     * should allow to uniquely identify a Configuration object (i.e. an instance of a 
     * configurable component) inside an application. 
     * 
     * @return
     */
    Object getId();
    
    /** 
     * Returns the <code>Configurator</code> associated with this <code>Configuration</code>.
     *
     * @return the configuration's configurator object.
     */
    Configurator getConfigurator();
    
    /**
     * Returns the configuration metadata model for this <code>Configuration</code>.
     * @return
     */
    ConfigurationMetadata getModel();
    
    /**
     * Returns the configuration item with the specified name if this item is held by
     * this <code>Configuration<code>, null otherwise.
     * 
     * @param name the name of the configuration item.
     * @return the configuration item.
     */   
    ConfigurationItem getItem(String name);
    
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
