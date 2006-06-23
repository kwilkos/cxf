package org.objectweb.celtix.transports;

import java.io.IOException;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;


/**
 * ServerTransport
 * @author dkulp
 *
 */
public interface ServerTransport extends Transport {
    

    /**
     * activate the server transport, involves starting listeners or creating of message queues.
     * @param callback - The call back object that the transport calls when there is a message to 
     *                   dispatch
     */
    void activate(ServerTransportCallback callback) throws IOException;

    /**
     * deactivate the server transport, involves stopping the listeners or message queues.
     * subsequently the transport could be activated using activate call. 
     */
    void deactivate() throws IOException;
    
    /**
     * Rebase the InputStreamMessageContext on an alternative response destination.
     * 
     * @param context the MessageContext
     * @param decoupledResponseEndpoint the decoupled response endpoint
     * @return an output stream message context for the original channel
     * @throws IOException If there is an error creating the context.
     */
    OutputStreamMessageContext rebase(MessageContext context, 
                                      EndpointReferenceType decoupledResponseEndpoint)
        throws IOException;
    
    /**
     * 
     * Do the post dispatch task here.
     */
    void postDispatch(MessageContext bindingContext, OutputStreamMessageContext context) 
        throws IOException;
   
}
