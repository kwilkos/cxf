package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.configuration.utils.PropertiesLoaderUtils;
import org.objectweb.celtix.configuration.utils.PropertiesUtils;

public final class BindingFactoryManagerImpl implements BindingFactoryManager {

    private static final String FACTORY_NAMESPACE_MAPPINGS_RESOURCE = "META-INF/bindings.properties";
    
    final Map<String, BindingFactory> bindingFactories;
    Properties factoryNamespaceMappings;
    private final Bus bus;
    
    public BindingFactoryManagerImpl(Bus b) throws BusException {
        bindingFactories = new ConcurrentHashMap<String, BindingFactory>();
        bus = b;
        
        try {
            factoryNamespaceMappings = PropertiesLoaderUtils
                .loadAllProperties(FACTORY_NAMESPACE_MAPPINGS_RESOURCE, Thread.currentThread()
                    .getContextClassLoader());
        } catch (IOException ex) {
            throw new BusException(ex);
        }
    }
    
    BindingFactory loadBindingFactory(String className, String ...namespaceURIs) throws BusException {
        BindingFactory factory = null;
        try {
            Class<? extends BindingFactory> clazz = 
                    Class.forName(className).asSubclass(BindingFactory.class);

            factory = clazz.newInstance();
            factory.init(bus);

            for (String namespace : namespaceURIs) {
                registerBindingFactory(namespace, factory);
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
    
    public void registerBindingFactory(String name,
        BindingFactory factory) throws BusException {
        bindingFactories.put(name, factory);
    }
    
    public void deregisterBindingFactory(String name) throws BusException {
        bindingFactories.remove(name);
    }
    
    public BindingFactory getBindingFactory(String namespace) throws BusException {
        BindingFactory factory = bindingFactories.get(namespace);
        if (null == factory) { 
            String classname = factoryNamespaceMappings.getProperty(namespace);
            if (null != classname) {
                Collection<String> names = PropertiesUtils.getPropertyNames(
                    factoryNamespaceMappings, classname);
                String[] allNamespaces = new String[names.size()];
                allNamespaces = names.toArray(allNamespaces);
                factory = loadBindingFactory(classname, allNamespaces);
            }
        }
        return factory;
    }
    
    public void shutdown() {
        //no nothing to do
    }    
}
