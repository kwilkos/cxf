package org.objectweb.celtix.plugins;

import org.objectweb.celtix.configuration.Configuration;

public interface PluginManager {
    
    ClassLoader getPluginClassLoader();
    
    Configuration getConfiguration();
    
    Object getPluginByName(String name) throws PluginException;
    
    Object getPlugin(String className) throws PluginException;
    
    void registerPlugin(Object plugin) throws PluginException;
    
    void unregisterPlugin(Object plugin) throws PluginException;
    
    void unloadPlugin(Object plugin) throws PluginException;
}
