package org.objectweb.celtix.plugins;

public interface PluginManager {
    
    ClassLoader getPluginClassLoader();
    
    Plugin getPlugin(String name) throws PluginException;
    
    void registerPlugin(Plugin plugin) throws PluginException;
    
    void unregisterPlugin(String name) throws PluginException;
    
    void unloadPlugin(String name) throws PluginException;
}
