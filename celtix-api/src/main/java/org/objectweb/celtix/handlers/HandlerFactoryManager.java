package org.objectweb.celtix.handlers;

import org.objectweb.celtix.BusException;

/**
 * Provides methods for registering and deregistering handler factories.
 */
public interface HandlerFactoryManager {

    /** 
     * Registers a <code>HandlerFactory</code> with the bus.
     * @param factory - the handler factory to register.
     * @throws BusException 
     */
    void registerHandlerFactory(HandlerFactory factory) throws BusException;

    /** 
     * Deregisters a <code>HandlerFactory</code> from the bus.
     * @param factory - the handler factory to deregister.
     * @throws BusException 
     */
    void deregisterHandlerFactory(HandlerFactory factory) throws BusException;
}
