package org.apache.cxf.binding;

import org.apache.cxf.BusException;

/**
 * The manager interface represents a repository for accessing 
 * <code>BindingFactory</code>s.
 *
 * Provides methods necessary for registering, deregistering or retrieving of
 * BindingFactorys.
 */
public interface BindingFactoryManager {

    /**
     * Registers a BindingFactory using the provided name.
     *
     * @param name The name of the BindingFactory.
     * @param binding The instance of the class that implements the
     * BindingFactory interface.
     */
    void registerBindingFactory(String name, BindingFactory binding);
    
    /**
     * Deregisters the BindingFactory with the provided name.
     *
     * @param name The name of the BindingFactory.
     */
    void unregisterBindingFactory(String name);

    /**
     * Retrieves the BindingFactory registered with the given name.
     *
     * @param name The name of the BindingFactory.
     * @return BindingFactory The registered BindingFactory.
     * @throws BusException If there is an error retrieving the BindingFactory.
     */
    BindingFactory getBindingFactory(String name) throws BusException;
}
