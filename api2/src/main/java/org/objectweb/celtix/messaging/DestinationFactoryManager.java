package org.objectweb.celtix.messaging;

import org.objectweb.celtix.BusException;

/**
 * The DestinationFactoryManager provides an interface to register and retrieve
 * transport factories.
 */
public interface DestinationFactoryManager {

    /**
     * Associates a name, often a URI, with a <code>DestinationFactory</code>
     * when registering with the <code>Bus</code>'s <code>TransportRegistry</code>.
     * @param name A string containing the name used to identify the
     * <code>DestinationFactory</code>
     * @param factory The <code>DestinationFactory</code> to be registered.
     * @throws BusException If there is an error registering the destination factory.
     */
    void registerDestinationFactory(String name,
        DestinationFactory factory) throws BusException;

    /**
     * Unregister a <code>DestinationFactory</code>.
     * @param name A string containing the name of the
     * <code>DestinationFactory</code>.
     * @throws BusException If there is an error deregistering
     * the destination factory.
     */
    void deregisterDestinationFactory(String name)
        throws BusException;
    
    /**
     * Returns the <code>DestinationFactory</code> registered with the specified name, 
     * loading the appropriate plugin if necessary.
     * 
     * @param name
     * @return the registered <code>DestinationFactory</code>
     * @throws BusException
     */
    DestinationFactory getDestinationFactory(String name) throws BusException;
}
