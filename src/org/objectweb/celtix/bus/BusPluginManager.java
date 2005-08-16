package org.objectweb.celtix.bus;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.application.ApplicationPluginManager;
import org.objectweb.celtix.configuration.Configuration;

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
    
}
