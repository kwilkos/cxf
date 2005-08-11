package org.objectweb.celtix.bus;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.transports.TransportFactoryManager;

public class TransportFactoryManagerImpl implements TransportFactoryManager {

    private Map<String, TransportFactory> transportFactories;
      
    TransportFactoryManagerImpl(Bus bus) {
        transportFactories = new HashMap<String, TransportFactory>();
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.bus.TransportFactoryManager#registerTransportFactory(java.lang.String, 
     * org.objectweb.celtix.transports.TransportFactory)
     */
    public void registerTransportFactory(String name, TransportFactory factory) throws BusException {
        synchronized (this) {
            transportFactories.put(name, factory);
        }
        // register wsdl extensions with factory ? 
    }
    
    /* (non-Javadoc)
     * @see org.objectweb.celtix.bus.TransportFactoryManager#deregisterTransportFactory(java.lang.String)
     */
    public void deregisterTransportFactory(String name)
        throws BusException {
    
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.bus.TransportFactoryManager#TransportFactory(java.lang.String)
     */
    public TransportFactory getTransportFactory(String name) throws BusException {
        // TODO Auto-generated method stub
        return null;
    }
    
    
}
