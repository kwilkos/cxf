package org.objectweb.celtix.plugins;

import org.objectweb.celtix.configuration.Configuration;

/**
 * Provides methods for registering, unregistering and obtaining plugin objects.
 */
public interface PluginManager {

    /**
     * Get the plugin class loader.
     * @return ClassLoader
     */
    ClassLoader getPluginClassLoader();
    
    /** 
     * Returns the <code>Configuration</code>.
     * @return Configuration
     */
    Configuration getConfiguration();
    
    /** 
     * Returns the plugin object with the given name.
     * @param name - the name of the plugin.
     * @return Object - the plugin.
     * @throws PluginException - if there is a circular dependency loading
     * dependent objects of the plugin.
     */
    Object getPluginByName(String name) throws PluginException;
    
    /** 
     * Returns the plugin object with the given classname.
     * @param className - the class name of the plugin.
     * @return Object - the plugin.
     * @throws PluginException - if there is a circular dependency loading
     * dependent objects of the plugin.
     */
    Object getPlugin(String className) throws PluginException;
    
    /** 
     * Registers a plugin object with the bus.
     * @param plugin - the plugin to register.
     * @throws PluginException if the given plugin is already registered.
     */
    void registerPlugin(Object plugin) throws PluginException;
    
    /** 
     * Explicitly unregister the given plugin object from the bus.
     * A plugin must be unregistered before it can be unloaded.
     * @param plugin - the plugin to unregister.
     * @throws PluginException if the given plugin is not registered.
     */
    void unregisterPlugin(Object plugin) throws PluginException;
    
    /** 
     * Unload a plugin object after it has been explicitly unregistered.
     * @param plugin - the plugin to unload.
     * @throws PluginException if the given plugin is still registered.
     */
    void unloadPlugin(Object plugin) throws PluginException;
}
