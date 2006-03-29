package org.objectweb.celtix.transports;



import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import javax.wsdl.Port;

import org.objectweb.celtix.bindings.ResponseCallback;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

/**
 * ClientTransport
 * @author dkulp
 *
 */
public interface ClientTransport extends Transport {

    /**
     * Get target endpoint.
     * 
     * @return EPR for target endpoint
     * @throws IOException if there is an error creating the endpoint.
     */
    EndpointReferenceType getTargetEndpoint();
    
    /**
     * Get decoupled endpoint.
     * 
     * @return EPR for decoupled endpoint
     * @throws IOException if there is an error creating the endpoint.
     */
    EndpointReferenceType getDecoupledEndpoint() throws IOException;
    
    /**
     * Get WSDL port.
     * 
     * @return WSDL port
     */
    Port getPort();
    
    /**
     * invoke on a oneway operation on a remote service.
     * @param context 
     */
    void invokeOneway(OutputStreamMessageContext context) throws IOException;

    /**
     * Invoke on a two-way operation on the remote service. The transport
     * should provide a context that returns a valid InputStream containing
     * the response in one of two ways:
     * <ol>
     * <li>if tightly coupled, via the return value of this method 
     * <li>if decoupled, via the <code>ResponseCallback</code>
     * </ol>
     * @param context containg in the request
     * @return context containing resonse if tightly coupled, null otherwise
     */        
    InputStreamMessageContext invoke(OutputStreamMessageContext context)
        throws IOException;

    /**
     * invoke on a two-way operation on the remote service asyncronously.
     * 
     * @param context
     * @return the context containing the InputStream response payload
     */        
    Future<InputStreamMessageContext> invokeAsync(OutputStreamMessageContext context, Executor executor)
        throws IOException;
    
    /**
     * Retrieve the ResponseCallback used to diapatch decoupled responses.
     * This may be either a new ResponseCallback created via the ClientBinding
     * or a preexisting instance, if the decoupled response endpoint is 
     * being reused.
     * 
     * @return a ResponseCallback instance
     */
    ResponseCallback getResponseCallback();
}
