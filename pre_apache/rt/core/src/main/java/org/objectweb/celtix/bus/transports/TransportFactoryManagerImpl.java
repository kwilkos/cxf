package org.objectweb.celtix.bus.transports;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.configuration.utils.StandardTypesHelper;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingType;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.transports.TransportFactoryManager;

public final class TransportFactoryManagerImpl implements TransportFactoryManager {

    private static final String FACTORY_NAMESPACE_MAPPINGS_RESOURCE = "META-INF/transports.xml";
    
    final Map<String, TransportFactory> transportFactories;
    List<ClassNamespaceMappingType> factoryNamespaceMappings;

    private final Bus bus;

    public TransportFactoryManagerImpl(Bus b) {
        transportFactories = new ConcurrentHashMap<String, TransportFactory>();
        bus = b;
        factoryNamespaceMappings = 
            StandardTypesHelper.getFactoryNamespaceMappings(FACTORY_NAMESPACE_MAPPINGS_RESOURCE, 
                                                                   getClass().getClassLoader());
    }
    
    public List<ClassNamespaceMappingType> getFactoryNamespaceMappings() {
        return factoryNamespaceMappings;
    }

    TransportFactory loadTransportFactory(String classname, String... namespaces) throws BusException {
        TransportFactory factory = null;
        try {
            Class<? extends TransportFactory> clazz = Class.forName(classname)
                .asSubclass(TransportFactory.class);

            factory = clazz.newInstance();
            factory.init(bus);
            for (String namespace : namespaces) {
                registerTransportFactory(namespace, factory);
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
     * @see org.objectweb.celtix.bus.TransportFactoryManager#registerTransportFactory(java.lang.String,
     *      org.objectweb.celtix.transports.TransportFactory)
     */
    public void registerTransportFactory(String namespace, TransportFactory factory) throws BusException {
        transportFactories.put(namespace, factory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.bus.TransportFactoryManager#deregisterTransportFactory(java.lang.String)
     */
    public void deregisterTransportFactory(String namespace) throws BusException {
        transportFactories.remove(namespace);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.bus.TransportFactoryManager#TransportFactory(java.lang.String)
     */
    /**
     * Returns the transport factory for the given namespace, constructing it
     * (and storing in the cache for future reference) if necessary, using its
     * list of factory classname to namespace mappings.
     * 
     * @param namespace the namespace.
     */
    public TransportFactory getTransportFactory(String namespace) throws BusException {
        TransportFactory factory = transportFactories.get(namespace);
        if (null == factory) {
            for (ClassNamespaceMappingType mapping : factoryNamespaceMappings) {
                if (StandardTypesHelper.supportsNamespace(mapping, namespace)) {
                    String[] namespaces = new String[mapping.getNamespace().size()];
                    mapping.getNamespace().toArray(namespaces);
                    factory = loadTransportFactory(mapping.getClassname(), namespaces);
                    break;
                }
            }
        }
        return factory;
    }

    public void shutdown() {
        // nothing to do
    }
}
