package org.objectweb.celtix.handlers;

import org.objectweb.celtix.BusException;

public interface HandlerFactoryManager {

    void registerHandlerFactory(HandlerFactory factory) throws BusException;

    void deregisterHandlerFactory(HandlerFactory factory) throws BusException;
}
