package org.objectweb.celtix.transports;



import org.objectweb.celtix.context.StreamMessageContext;

/**
 * ClientTransport
 * @author dkulp
 *
 */
public interface ClientTransport extends Transport {
    /**
     * Close the Client Connection
     */
    void disconnect();


    /**
     * Called at the start of the invokation.   The client transport is
     * expected to return a context that will return a valid OutputStream.
     * That can be done by calling setOutputStream on the passed in
     * context or by wrappering the existing context with a new Context. 
     *
     * @param context The associated MessageContext.
     * @return the context that will be used to obtain the OutputStream
     */
    StreamMessageContext startInvokation(StreamMessageContext context);

    /**
     * invoke on a oneway operation on a remote service.
     * @param context 
     */
    void invokeOneway(StreamMessageContext context);

    /**
     * invoke on a two-way operation on the remote service.   The transport
     * should provide a context that returns a valid InputStream upon return.
     * @param context
     * @return the context containing the InputStream response payload
     */        
    StreamMessageContext invoke(StreamMessageContext context);
}
