package org.objectweb.celtix.application;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.plugins.PluginManager;

public final class Application {
    
    private static Application theInstance;
    
    private Configuration configuration;
    private PluginManager pluginManager;
    
    private Application() {
        configuration = new ApplicationConfiguration(); 
        pluginManager = new ApplicationPluginManager();
    }
    
    public static Application getInstance() {
        synchronized (Application.class) {
            if (null == theInstance) {
                theInstance = new Application();
            }
        }
        return theInstance;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public PluginManager getPluginManager() {
        return pluginManager;
    }
    
}
