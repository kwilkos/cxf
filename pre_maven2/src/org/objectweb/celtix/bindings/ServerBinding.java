package org.objectweb.celtix.bindings;

import java.io.IOException;

import javax.wsdl.WSDLException;
import javax.xml.ws.Endpoint;

/**
 * ServerBinding
 */
public interface ServerBinding extends BindingBase {
    
    /**
     * Gets the endpoint that is the target of the opertaions for this binding
     * @return The endpoint that is the target of operations for this binding
     */
    Endpoint getEndpoint(); 
    
    /**
     * Activate the endpoint.  Usually creates the transport and binds to the transport.
     * @throws WSDLException
     * @throws IOException
     */
    void activate() throws WSDLException, IOException;
    
    /**
     * Deactivate the endpoint.  Tear down the transport, disconnect from ports, etc...
     * @throws IOException
     */
    void deactivate() throws IOException;
}
