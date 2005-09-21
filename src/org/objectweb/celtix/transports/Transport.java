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
    
    void shutdown();

    /**
     * @param context The associated MessageContext.
     * @return the context that will be used to obtain the OutputStream
     */
    OutputStreamMessageContext createOutputStreamContext(MessageContext context)
        throws IOException;
    
    /**
     * @param context The <code>OutputStreamMessageContext</code> to prepare.
     */
    void finalPrepareOutputStreamContext(OutputStreamMessageContext context)
        throws IOException;      
}
