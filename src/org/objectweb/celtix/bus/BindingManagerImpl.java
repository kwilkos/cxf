package org.objectweb.celtix.bus;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.BindingManager;

public class BindingManagerImpl implements BindingManager {

    private Map<String, BindingFactory> bindingFactories;
    
    BindingManagerImpl(Bus bus) {
        bindingFactories = new HashMap<String, BindingFactory>();
    }
    
    public void registerBinding(String name,
        BindingFactory factory) throws BusException {
        synchronized (this) {
            bindingFactories.put(name, factory);
        }        
    }
    
    public void deregisterBinding(String name) throws BusException {
        synchronized (this) {
            bindingFactories.remove(name);
        }
    }
    
    public BindingFactory getBindingFactory(String name) throws BusException {
        synchronized (this) {
            return bindingFactories.get(name);
        }
    }
        
}
