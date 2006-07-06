package org.objectweb.celtix.bus.bindings;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.bus.configuration.utils.StandardTypesHelper;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingType;

public final class BindingManagerImpl implements BindingManager {

    private static final String FACTORY_NAMESPACE_MAPPINGS_RESOURCE = "META-INF/bindings.xml";
    
    final Map<String, BindingFactory> bindingFactories;
    List<ClassNamespaceMappingType> factoryNamespaceMappings;
    private final Bus bus;
    
    public BindingManagerImpl(Bus b) {
        bindingFactories = new ConcurrentHashMap<String, BindingFactory>();
        bus = b;
  
        factoryNamespaceMappings = 
            StandardTypesHelper.getFactoryNamespaceMappings(FACTORY_NAMESPACE_MAPPINGS_RESOURCE,
                                                                   getClass().getClassLoader());       
    }
    
    public List<ClassNamespaceMappingType> getFactoryNamespaceMappings() {
        return factoryNamespaceMappings;
    }
    
    BindingFactory loadBindingFactory(String className, String ...namespaceURIs) throws BusException {
        BindingFactory factory = null;
        try {
            Class<? extends BindingFactory> clazz = 
                    Class.forName(className).asSubclass(BindingFactory.class);

            factory = clazz.newInstance();
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
        return factory;
    }
    
    public void registerBinding(String name,
        BindingFactory factory) throws BusException {
        bindingFactories.put(name, factory);
    }
    
    public void deregisterBinding(String name) throws BusException {
        bindingFactories.remove(name);
    }
    
    public BindingFactory getBindingFactory(String namespace) throws BusException {
        BindingFactory factory = bindingFactories.get(namespace);
        if (null == factory) {            
            for (ClassNamespaceMappingType mapping : factoryNamespaceMappings) {
                if (StandardTypesHelper.supportsNamespace(mapping, namespace)) {
                    String[] namespaces = new String[mapping.getNamespace().size()];
                    mapping.getNamespace().toArray(namespaces);
                    factory = loadBindingFactory(mapping.getClassname(), namespaces);
                    break;
                }
            }
        }
        return factory;
    }
    
    public void shutdown() {
        //no nothing to do
    }    
}
