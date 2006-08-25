package org.objectweb.celtix.application;

import java.util.ArrayList;
import java.util.Collection;

import org.objectweb.celtix.plugins.PluginManager;

public class PluginInfo {

    private String className;
    private PluginManager loadingManager;
    private PluginStateMachine state;
    private Object plugin;
    private PluginInfo requiredFor;
    private Collection<PluginManager> referringManagers;
    
    PluginInfo(String cn, PluginManager o) {
        className = cn;
        loadingManager = o;
        state = new PluginStateMachine();
        referringManagers = new ArrayList<PluginManager>();
    }
    
    String getClassName() {
        return className;
    }
    
    ClassLoader getClassLoader() {
        return loadingManager.getPluginClassLoader();
    }

    PluginStateMachine getState() {
        return state;
    }

    Object getPlugin() {
        return plugin;
    }
    
    void setPlugin(Object p) {
        plugin = p;
    }
    
    void setRequiredFor(PluginInfo r) {
        requiredFor = r;
    }
    
    boolean isCircularDependency() {
        PluginInfo r = requiredFor;
        while (null != r) {
            if (getClassName().equals(requiredFor.getClassName()) 
                && getClassLoader() == requiredFor.getClassLoader()) {
                return true;
            }
            r = r.requiredFor;
        }
        
        return false;
    }
    
    boolean isRegistered() {
        return referringManagers.size() > 0;
    }
    
    boolean isRegisteredWith(PluginManager manager) {
        for (PluginManager pm : referringManagers) {
            if (manager == pm) {
                return true;
            }
        }
        return false;
    }
    
    void register(PluginManager manager) {
        referringManagers.add(manager);
    }
    
    void unregister(PluginManager manager) {
        referringManagers.remove(manager);
    }
}
