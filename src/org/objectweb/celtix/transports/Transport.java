package org.objectweb.celtix.transports;

import java.io.IOException;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.OutputStreamMessageContext;


/**
 * Transport
 * @author dkulp
 *
 */
public interface Transport {
    
    /**
     * Shutdown the <code>Transport</code>.
     */
    void shutdown();

    /**
     * Create a context from which an OutputStream for the Transport can
     * be obtained.
     * @param context The associated MessageContext.
     * @return the context that will be used to obtain the OutputStream
     * @throws IOException If there is an error creating the context.
     */
    OutputStreamMessageContext createOutputStreamContext(MessageContext context)
        throws IOException;
    
    /**
     * Prepare the OutputStream context before writing.
     * @param context The <code>OutputStreamMessageContext</code> to prepare.
     * @throws IOException If there is an error preparing the context.
     */
    void finalPrepareOutputStreamContext(OutputStreamMessageContext context)
        throws IOException;
}
