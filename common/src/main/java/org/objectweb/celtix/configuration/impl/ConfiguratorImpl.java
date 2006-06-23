package org.objectweb.celtix.configuration.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.Configurator;


/**
 * REVISIT check if this could be based on javax.swing.TreeNode
 *
 */
public class ConfiguratorImpl implements Configurator {
    
    private final Configuration configuration;
    private final Configurator hook;
    private final Collection<Configurator> clients;   
    
    public ConfiguratorImpl(Configuration c, Configuration parent) {
        configuration = c;
        clients = new ArrayList<Configurator>();
        if (parent instanceof AbstractConfigurationImpl) {
            hook = ((AbstractConfigurationImpl)parent).getConfigurator();
            hook.registerClient(this);
        } else {
            hook = null;
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
    
    public synchronized void registerClient(Configurator c) { 
        // replace an existing client hook if it has the same namespace and id
        Object clientId = c.getConfiguration().getId();
        String clientNamepace = c.getConfiguration().getModel().getNamespaceURI();
        for (Configurator client : clients) {
            if (clientId.equals(client.getConfiguration().getId())
                && clientNamepace.equals(client.getConfiguration().getModel().getNamespaceURI())) {
                clients.remove(client);
                break;
            }
        }
        clients.add(c);
    }
    public void unregisterClient(Configurator c) {
        clients.remove(c);    
    } 
}
