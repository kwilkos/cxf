package org.objectweb.celtix.bus.bindings;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingListType;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingType;

public final class BindingManagerImpl implements BindingManager {

    private final Map<String, BindingFactory> bindingFactories;
    private final Bus bus;
    
    public BindingManagerImpl(Bus b) throws BusException {
        bindingFactories = new ConcurrentHashMap<String, BindingFactory>();
        bus = b;
        
        Object obj = bus.getConfiguration().getObject("bindingFactories");
        
        List<ClassNamespaceMappingType> factoryMappings = ((ClassNamespaceMappingListType)obj).getMap();
        for (ClassNamespaceMappingType mapping : factoryMappings) {
            String classname = mapping.getClassname();
            List<String> namespaceList = mapping.getNamespace();
            String[] namespaces = new String[namespaceList.size()];
            namespaceList.toArray(namespaces);
            loadBindingFactory(classname, namespaces);
        }
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
        bindingFactories.put(name, factory);
    }
    
    public void deregisterBinding(String name) throws BusException {
        bindingFactories.remove(name);
    }
    
    public BindingFactory getBindingFactory(String name) throws BusException {
        return bindingFactories.get(name);
    }
        
}
