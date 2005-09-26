package org.objectweb.celtix.application;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.plugins.PluginException;
import org.objectweb.celtix.plugins.PluginManager;
import org.objectweb.celtix.plugins.PluginMessage;

/**
 * @author asmyth The ApplicationPluginManager manages objects loaded on demand
 *         in response to a getPlugin request. The loading of one pluggable
 *         object may require explicitly loading dependent objects. Such
 *         dependencies are always specified by plugin name (as otherwise they
 *         can trivially be resolved by the clasloader). Circular dependencies
 *         are not allowed and will cause a PluginException. Plugins may be
 *         unloaded after they have been explicitly unregistered.
 */
public class ApplicationPluginManager implements PluginManager {

    private static final Logger LOG = Logger.getLogger(ApplicationPluginManager.class.getName());
    
    private static final MessageFormat PLUGINS_CLASSNAME_FMT = 
        new MessageFormat("plugins:{0}:className");

    private static final MessageFormat PLUGINS_PREREQUISITES_FMT = 
        new MessageFormat("plugins:{0}:prerequisites");

    private static List<PluginInfo> plugins;

    public ApplicationPluginManager() {
        plugins = new ArrayList<PluginInfo>();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.plugins.PluginManager#getPlugin(java.lang.String)
     */
    public Object getPlugin(String className) throws PluginException {
        return getPlugin(null, className, null);
    }  

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.plugins.PluginManager#getPluginByName(java.lang.String)
     */
    public Object getPluginByName(String pluginName) throws PluginException {        
        return getPluginByName(pluginName, null);
    }  
    
    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.plugins.PluginManager#registerPlugin(org.objectweb.celtix.plugins.Plugin)
     */
    public synchronized void registerPlugin(Object plugin) throws PluginException {
        PluginInfo info = findPluginInfo(plugin); 
        if (info.isRegisteredWith(this)) {
            throw new PluginException(new PluginMessage("ALREADY_REGISTERED", info.getClassName()));
        } else {
            info.register(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.plugins.PluginManager#unloadPlugin(java.lang.String)
     */
    public synchronized void unloadPlugin(Object plugin) throws PluginException {
        PluginInfo info = findPluginInfo(plugin);
        if (info.isRegistered()) {
            throw new PluginException(new PluginMessage("STILL_REGISTERED", info.getClassName()));
        } else {
            plugins.remove(plugin);
            info = null;
            plugin = null;
        }    
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.plugins.PluginManager#unregisterPlugin(java.lang.String)
     */
    public synchronized void unregisterPlugin(Object plugin) throws PluginException {
        PluginInfo info = findPluginInfo(plugin); 
        if (info.isRegisteredWith(this)) {
            info.unregister(this);
        } else {
            throw new PluginException(new PluginMessage("NOT_REGISTERED", info.getClassName()));
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

    /* (non-Javadoc)
     * @see org.objectweb.celtix.plugins.PluginManager#getConfiguration()
     */
    public Configuration getConfiguration() {
        return Application.getInstance().getConfiguration();
    }

    Object getPluginByName(String pluginName, PluginInfo dependent) throws PluginException {
        String key = PLUGINS_CLASSNAME_FMT.format(pluginName);
        Configuration configuration = getConfiguration();
        String pluginClassName = (String)configuration.getObject(key);
        
        return getPlugin(pluginName, pluginClassName, dependent);
    }

    Object getPlugin(String pluginName, String pluginClassName, PluginInfo dependent) throws PluginException {
        LOG.entering(getClass().getName(), "getPlugin");
        PluginInfo info = null;
        PluginStateMachine state = null;
        synchronized (this) {
            info = findPluginByClassname(pluginClassName);
            if (info == null) {
                info = new PluginInfo(pluginClassName, this);
                plugins.add(info);
            }
            state = info.getState();
            if (PluginStateMachine.PluginState.LOADING == state.getCurrentState()) {
                state.waitForState(PluginStateMachine.PluginState.LOADED);
                LOG.exiting(getClass().getName(), "getPlugin", "object currently being loaded");
                return info.getPlugin();
            } else if (PluginStateMachine.PluginState.LOADED == state.getCurrentState()) {
                LOG.exiting(getClass().getName(), "getPlugin", "object already loaded");
                return info.getPlugin();
            }
            state.setNextState(PluginStateMachine.PluginState.LOADING);
        }

        // check for circular dependencies

        if (dependent != null) {
            info.setRequiredFor(dependent);
            if (info.isCircularDependency()) {
                throw new PluginException(new PluginMessage("CIRCULAR_DEPENDENCY", pluginClassName));
            }
        }

        if (null != pluginName) {
            Configuration configuration = getConfiguration();

            String key = PLUGINS_PREREQUISITES_FMT.format(pluginName);
            String[] prerequisites = (String[])configuration.getObject(key);

            if (prerequisites != null) {
                for (String p : prerequisites) {
                    getPluginByName(p, info);
                }
            }
        }
        
        Object plugin = createPlugin(pluginClassName);
        info.setPlugin(plugin);
        state.setNextState(PluginStateMachine.PluginState.LOADED);
        LOG.exiting(getClass().getName(), "getPlugin", "object newly created");
        return plugin;
    }

    Object createPlugin(String pluginClassName) throws PluginException {

        ClassLoader cl = getPluginClassLoader();
        Object plugin = null;
        try {
            Class<?> pluginClass = Class.forName(pluginClassName, true, cl);
            plugin = pluginClass.newInstance();
        } catch (Exception ex) {
            LOG.severe("Failed to load " + pluginClassName + ": " + ex.getMessage());
            throw new PluginException(new PluginMessage("LOAD_FAILED", pluginClassName), ex);
        }
        return plugin;
    }
    
    PluginInfo findPluginByClassname(String className) {
        for (PluginInfo info : plugins) {
            if (info.getClassName().equals(className) 
                && info.getClassLoader() == getPluginClassLoader()) {
                return info;
            }
        }
        LOG.info("Could not find plugin info for class " + className);
        return null;
    }
    
    PluginInfo findPluginInfo(Object plugin) {

        for (PluginInfo info : plugins) {
            if (plugin == info.getPlugin()) {
                return info;
            }
        }  
        LOG.info("Could not find plugin info for plugin " + plugin);
        return null;
    }
}
