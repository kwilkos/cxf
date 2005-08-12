package org.objectweb.celtix.bus;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.application.Application;
import org.objectweb.celtix.bus.BusPluginInfo.BusPluginState;
import org.objectweb.celtix.plugins.BusPlugin;
import org.objectweb.celtix.plugins.PerBusPluginState;
import org.objectweb.celtix.plugins.Plugin;
import org.objectweb.celtix.plugins.PluginException;
import org.objectweb.celtix.plugins.PluginManager;

public class BusPluginManager implements PluginManager {

    
    private static final MessageFormat PLUGINS_PREREQUISITE_FMT = 
        new MessageFormat("plugins:{0}:prerequisite_plugins");
    
    private List<BusPluginInfo> pluginInfos;
    private Bus bus;
    
    protected BusPluginManager(Bus b) {
        bus = b;
        pluginInfos = new ArrayList<BusPluginInfo>();
    }
    
    /* (non-Javadoc)
     * @see org.objectweb.celtix.plugins.PluginManager#getPlugin(java.lang.String)
     */
    public Plugin getPlugin(String pluginName) throws PluginException {
        
        /*
         * asmyth:
         * we cannot delegate the loading of the prerequiaite plusings to the 
         * application plugin manager as we need to make sure that the 
         * prerequisite plugins' bus state can be initialised with this bus if
         * necessary
         */
        String key = PLUGINS_PREREQUISITE_FMT.format(pluginName);
        String[] prerequisites = (String[]) bus.getConfiguration().getObject(key);
        for (String p : prerequisites) {
            getPlugin(p);
        }
        
        Application app = Application.getInstance();
        PluginManager pm = app.getPluginManager();  
        
        Plugin p = pm.getPlugin(pluginName);
        
        if (p instanceof BusPlugin) {
            
            BusPluginInfo info = getPluginInfo(pluginName);
            
            // on-demand loading - the bus is initialised
            if (info.getState() == BusPluginState.LOADED) {
                BusPlugin bp = (BusPlugin)p;
                info.setState(BusPluginState.INITIALIZING);
                PerBusPluginState busState = bp.init(bus);
                info.setBusState(busState);
                busState.initComplete();
                info.setState(BusPluginState.READY);
            }
        }
        return  null;
    } 
    
    
    /* (non-Javadoc)
     * @see org.objectweb.celtix.plugins.PluginManager#getPluginClassLoader()
     */
    public ClassLoader getPluginClassLoader() {
        // TO DO: replace by Bus specific classloader
        return getClass().getClassLoader();
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.plugins.PluginManager#registerPlugin(org.objectweb.celtix.plugins.Plugin)
     */
    public void registerPlugin(Plugin plugin) throws PluginException {
        Application.getInstance().getPluginManager().registerPlugin(plugin);        
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.plugins.PluginManager#unloadPlugin(java.lang.String)
     */
    public void unloadPlugin(String name) throws PluginException {
        Application.getInstance().getPluginManager().unloadPlugin(name); 
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.plugins.PluginManager#unregisterPlugin(java.lang.String)
     */
    public void unregisterPlugin(String name) throws PluginException {
        Application.getInstance().getPluginManager().unloadPlugin(name);         
    }

    BusPluginInfo getPluginInfo(String name) {
        for (BusPluginInfo info : pluginInfos) {
            if (name.equals(info.getName())) {
                return info;
            }
        }
        BusPluginInfo info = new BusPluginInfo(name);
        pluginInfos.add(info);
        return info;
    }
    

    
}
