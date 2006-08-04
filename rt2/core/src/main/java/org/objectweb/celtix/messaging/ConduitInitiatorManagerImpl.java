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

public final class ConduitInitiatorManagerImpl implements ConduitInitiatorManager {

    private static final String FACTORY_NAMESPACE_MAPPINGS_RESOURCE = "META-INF/conduits.properties";
    
    final Map<String, ConduitInitiator> conduitInitiators;
    Properties factoryNamespaceMappings;

    @Resource
    private Bus bus;

    public ConduitInitiatorManagerImpl() throws BusException {
        conduitInitiators = new ConcurrentHashMap<String, ConduitInitiator>();

        try {
            factoryNamespaceMappings = PropertiesLoaderUtils
                .loadAllProperties(FACTORY_NAMESPACE_MAPPINGS_RESOURCE, Thread.currentThread()
                    .getContextClassLoader());
        } catch (IOException ex) {
            throw new BusException(ex);
        }
    }
    
    ConduitInitiator loadConduitInitiator(String classname, String... namespaces) throws BusException {
        ConduitInitiator factory = null;
        try {
            Class<? extends ConduitInitiator> clazz = Class.forName(classname)
                .asSubclass(ConduitInitiator.class);

            factory = clazz.newInstance();
            
            // inject bus resources into conduit initiator
            bus.getResourceManager();
            
            for (String namespace : namespaces) {
                registerConduitInitiator(namespace, factory);
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
     * @see org.objectweb.celtix.bus.ConduitInitiatorManager#registerConduitInitiator(java.lang.String,
     *      org.objectweb.celtix.transports.ConduitInitiator)
     */
    public void registerConduitInitiator(String namespace, ConduitInitiator factory) throws BusException {
        conduitInitiators.put(namespace, factory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.bus.ConduitInitiatorManager#deregisterConduitInitiator(java.lang.String)
     */
    public void deregisterConduitInitiator(String namespace) throws BusException {
        conduitInitiators.remove(namespace);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.bus.ConduitInitiatorManager#ConduitInitiator(java.lang.String)
     */
    /**
     * Returns the conduit initiator for the given namespace, constructing it
     * (and storing in the cache for future reference) if necessary, using its
     * list of factory classname to namespace mappings.
     * 
     * @param namespace the namespace.
     */
    public ConduitInitiator getConduitInitiator(String namespace) throws BusException {
        ConduitInitiator factory = conduitInitiators.get(namespace);
        if (null == factory) {
            String classname = factoryNamespaceMappings.getProperty(namespace);
            if (null != classname) {
                Collection<String> names = PropertiesLoaderUtils.
                    getPropertyNames(factoryNamespaceMappings, classname);
                String[] allNamespaces = new String[names.size()];
                allNamespaces = names.toArray(allNamespaces);
                factory = loadConduitInitiator(classname, allNamespaces);
            }
        }
        return factory;
    }

    @PreDestroy
    public void shutdown() {
        // nothing to do
    }
}
