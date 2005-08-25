package org.objectweb.celtix.transports;

import org.objectweb.celtix.context.InputStreamMessageContext;

/**
 * ServerTransportCallback
 * @author dkulp
 *
 */
public interface ServerTransportCallback {
    /**
     * Used to dispatch a message from the <code>ServerTransport</code>
     * to an servant. 
     *
     * @param ctx The MessageContext associated with the call.
     */
    void dispatch(InputStreamMessageContext ctx, ServerTransport transport);
    
}
