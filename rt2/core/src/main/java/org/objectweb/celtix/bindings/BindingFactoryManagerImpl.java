package org.objectweb.celtix.bindings;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.celtix.BusException;

public final class BindingFactoryManagerImpl implements BindingFactoryManager {

    final Map<String, BindingFactory> bindingFactories;

    public BindingFactoryManagerImpl() {
        this(new HashMap<String, BindingFactory>());
    }
    
    public BindingFactoryManagerImpl(Map<String, BindingFactory> intiailBindings) {
        bindingFactories = new ConcurrentHashMap<String, BindingFactory>(intiailBindings);
    }

    public void registerBindingFactory(String name,
        BindingFactory factory) throws BusException {
        bindingFactories.put(name, factory);
    }
    
    public void unregisterBindingFactory(String name) throws BusException {
        bindingFactories.remove(name);
    }
    
    public BindingFactory getBindingFactory(String namespace) throws BusException {
        return bindingFactories.get(namespace);
    }

}
