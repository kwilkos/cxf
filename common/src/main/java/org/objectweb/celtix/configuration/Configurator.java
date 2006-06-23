package org.objectweb.celtix.configuration;

import java.util.Collection;

public interface Configurator {
    
    Configuration getConfiguration();

    Configurator getHook();
    
    Collection<Configurator> getClients();
    
    void registerClient(Configurator c);
    
    void unregisterClient(Configurator c);
  
}
