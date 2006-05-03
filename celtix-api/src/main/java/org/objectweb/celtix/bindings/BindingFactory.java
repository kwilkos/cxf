package org.objectweb.celtix.bindings;

import java.io.IOException;
import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

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
    ClientBinding createClientBinding(EndpointReferenceType reference)
        throws WSDLException, IOException;

    /**
     * Creates a ServerBinding for the <code>EndpointReferenceType</code>
     * and <code>Endpoint</code> provided.
     *
     * @param reference The EndpointReferenceType the binding will use.
     * @param endpointCallback The callback used by the binding to obtain additional information
     *       from the data binding layers.
     * @return ServerBinding The newly created ServerBinding.
     */
    ServerBinding createServerBinding(EndpointReferenceType reference,
                                      ServerBindingEndpointCallback endpointCallback)
        throws WSDLException, IOException;
}
