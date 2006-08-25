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

public final class ConduitInitiatorManagerImpl implements ConduitInitiatorManager {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(ConduitInitiatorManager.class);


    final Map<String, ConduitInitiator> conduitInitiators;
    Properties factoryNamespaceMappings;
    
    @Resource
    private ExtensionManager extensionManager;

    public ConduitInitiatorManagerImpl() {
        conduitInitiators = new ConcurrentHashMap<String, ConduitInitiator>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.bus.ConduitInitiatorManager#registerConduitInitiator(java.lang.String,
     *      org.objectweb.celtix.transports.ConduitInitiator)
     */
    public void registerConduitInitiator(String namespace, ConduitInitiator factory) {
        conduitInitiators.put(namespace, factory);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.celtix.bus.ConduitInitiatorManager#deregisterConduitInitiator(java.lang.String)
     */
    public void deregisterConduitInitiator(String namespace) {
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
            extensionManager.activateViaNS(namespace);
            factory = conduitInitiators.get(namespace);
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
