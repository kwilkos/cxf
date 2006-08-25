package org.objectweb.celtix.bus.configuration;

import java.util.ArrayList;
import java.util.Collection;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.Configurator;

/**
 * REVISIT check if this could be based on javax.swing.TreeNode
 *
 */
public class ConfiguratorImpl implements Configurator {
    
    private Configuration configuration;
    private Configurator hook;
    private Collection<Configurator> clients;   
    
    public ConfiguratorImpl(Configuration c, Configuration parent) {
        configuration = c;
        clients = new ArrayList<Configurator>();
        if (null != parent) {
            hook = parent.getConfigurator();
            hook.registerClient(this);
        }
    }
    
    public Configuration getConfiguration() {        
        return configuration;
    }

    public Collection<Configurator> getClients() {
        return clients;
    }
    
    public Configurator getHook() {
        return hook;
    }
    
    public void registerClient(Configurator c) {       
        clients.add(c);
    }
    public void unregisterClient(Configurator c) {
        clients.remove(c);    
    } 
}
