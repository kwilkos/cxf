package org.objectweb.celtix.messaging;

import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.objectweb.celtix.BusException;
import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.extension.ExtensionManager;

public final class DestinationFactoryManagerImpl implements DestinationFactoryManager {
  
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(DestinationFactoryManager.class);
    
    final Map<String, DestinationFactory> destinationFactories;
    Properties factoryNamespaceMappings;
    
    @Resource
    private ExtensionManager extensionManager;

    public DestinationFactoryManagerImpl() throws BusException {
        destinationFactories = new ConcurrentHashMap<String, DestinationFactory>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.bus.DestinationFactoryManager#registerDestinationFactory(java.lang.String,
     *      org.objectweb.celtix.transports.DestinationFactory)
     */
    public void registerDestinationFactory(String namespace, DestinationFactory factory) {
        destinationFactories.put(namespace, factory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.bus.DestinationFactoryManager#deregisterDestinationFactory(java.lang.String)
     */
    public void deregisterDestinationFactory(String namespace) {
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
            extensionManager.activateViaNS(namespace);
            factory = destinationFactories.get(namespace);
        }
        if (null == factory) {
            throw new BusException(new Message("NO_CONDUIT_INITIATOR_EXC", BUNDLE, namespace));
        }
        return factory;
    }

    @PreDestroy
    public void shutdown() {
        // nothing to do
    }
}
