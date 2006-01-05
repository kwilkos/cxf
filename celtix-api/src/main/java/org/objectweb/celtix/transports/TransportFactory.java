package org.objectweb.celtix.transports;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;


/**
 * The TransportFactory provides methods to create client and server transports.
 */
public interface TransportFactory {
    
    /**
     * Initializes this <code>TransportFactory</code>.
     * 
     * @param bus The bus class this TransportFactory will use.
     */
    void init(Bus bus);
    
    /**
     * Returns a newly created <code>ServerTransport</code>.
     * 
     * @param address the endpoint reference used by the <code>ServerTransport</code>.
     * @return ServerTransport the newly created <code>ServerTransport</code>.
     * @throws WSDLException If there is an error creating the transport.
     */
    ServerTransport createServerTransport(EndpointReferenceType address)
        throws WSDLException, IOException;

    /**
     * Returns a newly created transient <code>ServerTransport</code>.
     * 
     * @param address the endpoint reference used by the <code>ServerTransport</code>.
     * @return ServerTransport the newly created server transport.
     * @throws WSDLException If there is an error creating the transport.
     */
    ServerTransport createTransientServerTransport(EndpointReferenceType address)
        throws WSDLException, IOException;
    
    /**
     * Returns a newly created <code>ClientTransport</code>.
     * 
     * @param address the endpoint reference used by the <code>ClientTransport</code>.
     * @return ClientTransport the newly created client transport.
     * @throws WSDLException If there is an error creating the transport.
     */
    ClientTransport createClientTransport(EndpointReferenceType address)
        throws WSDLException, IOException;
}
