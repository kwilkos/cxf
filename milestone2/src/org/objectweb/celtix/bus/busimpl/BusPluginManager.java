package org.objectweb.celtix.bus.busimpl;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.application.ApplicationPluginManager;
import org.objectweb.celtix.buslifecycle.BusLifeCycleListener;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.plugins.PluginException;

public class BusPluginManager extends ApplicationPluginManager {

    private Bus bus;
    
    BusPluginManager(Bus b) {
        bus = b;
    }
 
    /* (non-Javadoc)
     * @see org.objectweb.celtix.plugins.PluginManager#getPluginClassLoader()
     */
    public ClassLoader getPluginClassLoader() {
        return getClass().getClassLoader();     
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.application.ApplicationPluginManager#getConfiguration()
     */
    @Override
    public Configuration getConfiguration() {
        return bus.getConfiguration();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.plugins.PluginManager#registerPlugin(org.objectweb.celtix.plugins.Plugin)
     */
    public synchronized void registerPlugin(Object plugin) throws PluginException {
        super.registerPlugin(plugin);
        if (plugin instanceof BusLifeCycleListener) {
            bus.getLifeCycleManager().registerLifeCycleListener((BusLifeCycleListener)plugin);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.plugins.PluginManager#unregisterPlugin(java.lang.String)
     */
    public synchronized void unregisterPlugin(Object plugin) throws PluginException {
        if (plugin instanceof BusLifeCycleListener) {
            bus.getLifeCycleManager().registerLifeCycleListener((BusLifeCycleListener)plugin);
        }
        super.unregisterPlugin(plugin);
    }
    
}
