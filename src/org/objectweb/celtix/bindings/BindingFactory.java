package org.objectweb.celtix.bindings;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;

/**
 * A factory interface for creating client and server bindings. 
 */
public interface BindingFactory {
    /**
     * Initialize the factory with a Bus reference.
     *
     * @param bus The <code>Bus</code> for this BindingFactory.
     */
    void init(Bus bus);
    
    /**
     * Creates a ClientBinding using the <code>EndpointReferenceType</code>.
     *
     * @param reference The EndpointReferenceType the binding will use.
     * @return ClientBinding The newly created ClientBinding.
     */
    ClientBinding createClientBinding(EndpointReferenceType reference);

    /**
     * Creates a ServerBinding for the <code>EndpointReferenceType</code>
     * and <code>Endpoint</code> provided.
     *
     * @param reference The EndpointReferenceType the binding will use.
     * @param endpoint The server endpoint.
     * @return ServerBinding The newly created ServerBinding.
     */
    ServerBinding createServerBinding(EndpointReferenceType reference, Endpoint endpoint);
}
