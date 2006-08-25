package org.objectweb.celtix.bindings;

import org.objectweb.celtix.context.InputStreamMessageContext;

public interface ResponseCallback {

    /**
     * Used to dispatch a response from the <code>ClientTransport</code>
     * back up to the binding. This is required for decoupled response
     * processing.
     *
     * @param responseContext the context containing the InputStream 
     * response payload
     */
    void dispatch(InputStreamMessageContext responseContext);
}
