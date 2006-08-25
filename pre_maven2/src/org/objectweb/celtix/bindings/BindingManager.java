package org.objectweb.celtix.bindings;

import org.objectweb.celtix.BusException;

/**
 * The manager interface represents a repository for accessing 
 * <code>BindingFactory</code>s.
 *
 * Provides methods necessary for registering, deregistering or retrieving of
 * BindingFactorys.
 */
public interface BindingManager {

    /**
     * Registers a BindingFactory using the provided name.
     *
     * @param name The name of the BindingFactory.
     * @param binding The instance of the class that implements the
     * BindingFactory interface.
     * @throws BusException If there is an error registering the BindingFactory.
     */
    void registerBinding(String name,
        BindingFactory binding) throws BusException;
    
    /**
     * Deregisters the BindingFactory with the provided name.
     *
     * @param name The name of the BindingFactory.
     * @throws BusException If there is an error deregistering the name.
     */
    void deregisterBinding(String name)
        throws BusException;

    /**
     * Retrieves the BindingFactory registered with the given name.
     *
     * @param name The name of the BindingFactory.
     * @return BindingFactory The registered BindingFactory.
     * @throws BusException If there is an error retrieving the BindingFactory.
     */
    BindingFactory getBindingFactory(String name) throws BusException;
}
