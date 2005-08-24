package org.objectweb.celtix.bus;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.BindingManager;

public class BindingManagerImpl implements BindingManager {

    private final Map<String, BindingFactory> bindingFactories;
    private final Bus bus;
    
    BindingManagerImpl(Bus b) throws BusException {
        bindingFactories = new HashMap<String, BindingFactory>();
        bus = b;
        
        // TODO - config instead of hard coded
        loadBindingFactory("org.objectweb.celtix.bus.bindings.soap.SOAPBindingFactory",
                             "http://schemas.xmlsoap.org/wsdl/soap/");        
    }
    
    private void loadBindingFactory(String className, String ...namespaceURIs) throws BusException {
        try {
            Class<? extends BindingFactory> clazz = 
                    Class.forName(className).asSubclass(BindingFactory.class);

            BindingFactory factory = clazz.newInstance();
            factory.init(bus);

            for (String namespace : namespaceURIs) {
                registerBinding(namespace, factory);
            }
        } catch (ClassNotFoundException cnfe) {
            throw new BusException(cnfe);
        } catch (InstantiationException ie) {
            throw new BusException(ie);
        } catch (IllegalAccessException iae) {
            throw new BusException(iae);
        }
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
