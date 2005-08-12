package org.objectweb.celtix.plugins;

import org.objectweb.celtix.Bus;

public interface BusPlugin extends Plugin {
    
    PerBusPluginState init(Bus bus);        
}
