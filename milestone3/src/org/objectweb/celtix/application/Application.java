package org.objectweb.celtix.application;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.plugins.PluginManager;

public final class Application {
    
    private static Application theInstance;
    
    private final Configuration configuration;
    private final PluginManager pluginManager;
    
    private Application() {
        configuration = new ApplicationConfiguration(); 
        pluginManager = new ApplicationPluginManager();
    }
    
    /** 
     * Returns the <code>Application</code> singleton.
     * 
     * @return Application the application singleton.
     */
    public static Application getInstance() {
        synchronized (Application.class) {
            if (null == theInstance) {
                theInstance = new Application();
            }
        }
        return theInstance;
    }
    
    /** 
     * Returns the <code>Configuration</code> of the <code>Application</code>.
     * 
     * @return Configuration the configuration of the application.
     */
    public Configuration getConfiguration() {
        return configuration;
    }
    
    /** 
     * Returns the <code>PluginManager</code> of the <code>Application</code>.
     * 
     * @return PluginManager the plugin manager of the application.
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }
    
}
