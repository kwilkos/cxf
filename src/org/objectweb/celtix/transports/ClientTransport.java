package org.objectweb.celtix.transports;



import java.util.concurrent.Future;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;

/**
 * ClientTransport
 * @author dkulp
 *
 */
public interface ClientTransport extends Transport {

    /**
     * @param context The associated MessageContext.
     * @return the context that will be used to obtain the OutputStream
     */
    OutputStreamMessageContext createOutputStreamContext(MessageContext context);
    
    /**
     * @param context The associated MessageContext.
     * @return the context that will be used to obtain the OutputStream
     */
    void finalPrepareOutputStreamContext(OutputStreamMessageContext context);


    /**
     * invoke on a oneway operation on a remote service.
     * @param context 
     */
    void invokeOneway(OutputStreamMessageContext context);

    /**
     * invoke on a two-way operation on the remote service.   The transport
     * should provide a context that returns a valid InputStream upon return.
     * @param context
     * @return the context containing the InputStream response payload
     */        
    InputStreamMessageContext invoke(OutputStreamMessageContext context);

    /**
     * invoke on a two-way operation on the remote service asyncronously.
     * 
     * @param context
     * @return the context containing the InputStream response payload
     */        
    Future<InputStreamMessageContext> invokeAsync(OutputStreamMessageContext context);

}
