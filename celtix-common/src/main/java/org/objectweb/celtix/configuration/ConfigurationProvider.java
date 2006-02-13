package org.objectweb.celtix.configuration;


public interface ConfigurationProvider {
    
    void init(Configuration configuration);

    /**
     * Lookup the value for the configuration item with the given name in the 
     * underlying store.
     * 
     * @param name the name of the configuration item.
     * @return the value of the configuration item.
     */
    Object getObject(String name);
    
    /**
     * Change the value of  the configuration item with the given name.
     * Return true if the change was accepted and the value changed.
     * It is the providers responsibility to persiste the change in its underlying store
     * if it accepts the change.
     * 
     * @param name the name of the configuration item.
     * @param value the new value for the configuration item.
     * @return true if the change was accepted.
     */
    boolean setObject(String name, Object value);
}
