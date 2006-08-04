package org.objectweb.celtix.messaging;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.common.util.PropertiesLoaderUtils;

public final class DestinationFactoryManagerImpl implements DestinationFactoryManager {

    private static final String FACTORY_NAMESPACE_MAPPINGS_RESOURCE = "META-INF/destinations.properties";
    
    final Map<String, DestinationFactory> destinationFactories;
    Properties factoryNamespaceMappings;

    @Resource
    private Bus bus;

    public DestinationFactoryManagerImpl() throws BusException {
        destinationFactories = new ConcurrentHashMap<String, DestinationFactory>();

        try {
            factoryNamespaceMappings = PropertiesLoaderUtils
                .loadAllProperties(FACTORY_NAMESPACE_MAPPINGS_RESOURCE, Thread.currentThread()
                    .getContextClassLoader());
        } catch (IOException ex) {
            throw new BusException(ex);
        }
    }
    
    DestinationFactory loadDestinationFactory(String classname, String... namespaces) throws BusException {
        DestinationFactory factory = null;
        try {
            Class<? extends DestinationFactory> clazz = Class.forName(classname)
                .asSubclass(DestinationFactory.class);

            factory = clazz.newInstance();
            
            // inject bus resources into conduit initiator
            bus.getResourceManager();
            
            for (String namespace : namespaces) {
                registerDestinationFactory(namespace, factory);
            }
        } catch (ClassNotFoundException e) {
            throw new BusException(e);
        } catch (InstantiationException e) {
            throw new BusException(e);
        } catch (IllegalAccessException e) {
            throw new BusException(e);
        }
        return factory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.bus.DestinationFactoryManager#registerDestinationFactory(java.lang.String,
     *      org.objectweb.celtix.transports.DestinationFactory)
     */
    public void registerDestinationFactory(String namespace, DestinationFactory factory) throws BusException {
        destinationFactories.put(namespace, factory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.bus.DestinationFactoryManager#deregisterDestinationFactory(java.lang.String)
     */
    public void deregisterDestinationFactory(String namespace) throws BusException {
        destinationFactories.remove(namespace);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.bus.DestinationFactoryManager#DestinationFactory(java.lang.String)
     */
    /**
     * Returns the conduit initiator for the given namespace, constructing it
     * (and storing in the cache for future reference) if necessary, using its
     * list of factory classname to namespace mappings.
     * 
     * @param namespace the namespace.
     */
    public DestinationFactory getDestinationFactory(String namespace) throws BusException {
        DestinationFactory factory = destinationFactories.get(namespace);
        if (null == factory) {
            String classname = factoryNamespaceMappings.getProperty(namespace);
            if (null != classname) {
                Collection<String> names = PropertiesLoaderUtils.
                    getPropertyNames(factoryNamespaceMappings, classname);
                String[] allNamespaces = new String[names.size()];
                allNamespaces = names.toArray(allNamespaces);
                factory = loadDestinationFactory(classname, allNamespaces);
            }
        }
        return factory;
    }

    @PreDestroy
    public void shutdown() {
        // nothing to do
    }
}
