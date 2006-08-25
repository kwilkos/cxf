package org.objectweb.celtix.transports;

import org.objectweb.celtix.BusException;

public interface TransportFactoryManager {

    /**
     * Associates a name, often a URI, with a <code>TransportFactory</code>
     * when registering with the <code>Bus</code>'s <code>TransportRegistry</code>.
     * @param name A string containing the name used to identify the
     * <code>TransportFactory</code>
     * @param factory The <code>TransportFactory</code> to be registered.
     * @throws BusException If there is an error registering transport factory.
     */
    void registerTransportFactory(String name,
        TransportFactory factory) throws BusException;

    /**
     * Unregister a <code>TransportFactory</code>.
     * @param name A string containing the name of the
     * <code>TransportFactory</code>.
     * @throws BusException If there is an error deregistering
     * the transport factory.
     */
    void deregisterTransportFactory(String name)
        throws BusException;
    
    /**
     * Returns the <code>TransportFactory</code> registered with the specified name, 
     * loading the appropriate plugin if necessary.
     * 
     * @param name
     * @return the registered <code>TransportFactory</code>
     * @throws BusException
     */
    TransportFactory getTransportFactory(String name) throws BusException;

}
