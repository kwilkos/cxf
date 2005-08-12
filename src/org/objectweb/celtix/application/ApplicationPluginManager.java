package org.objectweb.celtix.application;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.plugins.Plugin;
import org.objectweb.celtix.plugins.PluginException;
import org.objectweb.celtix.plugins.PluginManager;

/**
 * @author asmyth
 * 
 * TO DO: check for circular dependencies - need a generci plugin state (loaded, loading, ...) and 
 * a bus specific one (initializing, shutting down)
 */
public class ApplicationPluginManager implements PluginManager {

    private static final MessageFormat PLUGINS_CLASS_FMT = 
        new MessageFormat("plugins:{0}:className");
    private static final MessageFormat PLUGINS_PREREQUISITE_FMT = 
        new MessageFormat("plugins:{0}:prerequisite_plugins");
    
    private Map<String, Plugin> plugins;

    protected ApplicationPluginManager() {
        plugins = new HashMap<String, Plugin>();
    }
       
 
     /* (non-Javadoc)
     * @see org.objectweb.celtix.plugins.PluginManager#getPlugin(java.lang.String)
     */
    public Plugin getPlugin(String pluginName) throws PluginException {
        
        Plugin plugin = plugins.get(pluginName);
        if (null != plugin) {
            return plugin;
        }

        Application application = Application.getInstance();
        Configuration configuration = application.getConfiguration();
        
        String key = PLUGINS_CLASS_FMT.format(pluginName);
        String pluginClassName = (String)configuration.getObject(key);
        
        // hardcoded for MS1
        if (null == pluginClassName) {
            if ("http".equals(pluginName)) {
                pluginClassName = "org.objectweb.celtix.trasnports.HttpTransportPlugin";
            } else if ("soap".equals(pluginName)) {
                pluginClassName = "org.objectweb.celtix.trasnports.SoapBindingPlugin";
            } // ...
        }
        
        key = PLUGINS_PREREQUISITE_FMT.format(pluginName);
        String[] prerequisites = (String[])configuration.getObject(key);
      
        if (prerequisites != null) {
            for (String p : prerequisites) {
                getPlugin(p);
            }
        }
        
        plugin = loadPlugin(pluginName, pluginClassName);
        plugins.put(pluginName, plugin);
        return plugin;
        
    }



    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.plugins.PluginManager#registerPlugin(org.objectweb.celtix.plugins.Plugin)
     */
    public void registerPlugin(Plugin plugin) {
        plugins.put(plugin.getName(), plugin);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.plugins.PluginManager#unloadPlugin(java.lang.String)
     */
    public void unloadPlugin(String name) {
        // intentionally empty
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.plugins.PluginManager#unregisterPlugin(java.lang.String)
     */
    public void unregisterPlugin(String name) {
        Plugin p = plugins.get(name);
        if (null != p) {
            plugins.remove(p);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.plugins.PluginManager#getPluginClassLoader()
     */
    public ClassLoader getPluginClassLoader() {
        return getClass().getClassLoader();
    }
    
    Plugin loadPlugin(String pluginName, String pluginClassName) throws PluginException {

        ClassLoader cl = getPluginClassLoader();
        Plugin plugin = null;
        try {
            Class pluginClass = Class.forName(pluginClassName, true, cl);
            plugin = (Plugin)pluginClass.newInstance();
        } catch (Exception ex) {
            throw new PluginException("LOAD_FAILED", ex, pluginName);
        }
        return plugin;
    }
}
