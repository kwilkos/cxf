package org.objectweb.celtix.bus;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.handlers.HandlerFactory;
import org.objectweb.celtix.handlers.HandlerFactoryManager;


public class HandlerFactoryManagerImpl implements HandlerFactoryManager {

    HandlerFactoryManagerImpl(Bus bus) {
    }


    /* org.objectweb.celtix.handlers.Javadoc)
     * @see org.objectweb.celtix.bus.HandlerFactoryManager#registerHandlerFactory(
     * org.objectweb.celtix.handlers.HandlerFactory)
     */
    public void registerHandlerFactory(HandlerFactory factory)
        throws BusException {
    
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.bus.HandlerFactoryManager#deregisterHandlerFactory(
     * org.objectweb.celtix.handlers.HandlerFactory)
     */
    public void deregisterHandlerFactory(HandlerFactory factory)
        throws BusException {
    
    }
}
