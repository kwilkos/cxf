package org.objectweb.celtix.bindings;

import org.objectweb.celtix.BusException;

public interface BindingManager {

    void registerBinding(String name,
        BindingFactory binding) throws BusException;
    
    void deregisterBinding(String name)
        throws BusException;

    BindingFactory getBindingFactory(String name) throws BusException;
}
