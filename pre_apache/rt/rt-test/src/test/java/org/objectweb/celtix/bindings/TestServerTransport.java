package org.objectweb.celtix.bindings;

import java.io.IOException;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bindings.TestServerBinding.ToyOutputStreamMessageContext;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

class TestServerTransport implements ServerTransport {

    private ServerTransportCallback callback;

    public void shutdown() {
    }

    public void activate(ServerTransportCallback cb) throws IOException {
        callback = cb;
    }
    
    public OutputStreamMessageContext rebase(MessageContext context,
                                             EndpointReferenceType decoupledResponseEndpoint)
        throws IOException {
        return null;
    }

    public OutputStreamMessageContext createOutputStreamContext(MessageContext context)
        throws IOException {
        return new ToyOutputStreamMessageContext(new GenericMessageContext());
    }

    public void deactivate() throws IOException {
    }

    public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) throws IOException {
    }
    
    public void postDispatch(MessageContext bindingContext, 
                                       OutputStreamMessageContext context) throws IOException {
        
    }

    public void fire() {
        callback.dispatch(new TestInputStreamContext(new byte[] {0}), this);
    }
}
