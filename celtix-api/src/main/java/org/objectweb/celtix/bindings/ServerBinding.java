package org.objectweb.celtix.bindings;

import java.io.IOException;

import javax.wsdl.WSDLException;
import javax.xml.ws.Endpoint;

import org.objectweb.celtix.context.OutputStreamMessageContext;

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
    
    /**
     * Make an initial partial response to an incoming request. The partial
     * response may only contain 'header' information, and not a 'body'.
     * 
     * @param context object message context
     * @param callback callback for data binding
     */
    void partialResponse(OutputStreamMessageContext outputContext, 
                         DataBindingCallback callback) throws IOException;
}
