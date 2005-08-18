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
    void dispatch(InputStreamMessageContext ctx);
    
    /**
     * A factory method for obtaining contexts that will be supplied
     * with a call to <code>dispatch</code>.
     *
     * @return context The <code>MessageContext</code> associated with the call.
     */
    InputStreamMessageContext getDispatchContext();
}
