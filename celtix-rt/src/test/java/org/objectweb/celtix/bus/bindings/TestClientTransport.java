package org.objectweb.celtix.bus.bindings;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import javax.wsdl.Port;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.ResponseCallback;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class TestClientTransport implements ClientTransport {

    private InputStreamMessageContext istreamCtx;
    
    public TestClientTransport() {        
    }
    
    public TestClientTransport(Bus bus, EndpointReferenceType ref) {        
    }
    
    
    public EndpointReferenceType getTargetEndpoint() {
        return null;
    }
        
    public EndpointReferenceType getDecoupledEndpoint() throws IOException {
        return null;
    }
        
    public Port getPort() {
        return null;
    }

    public OutputStreamMessageContext createOutputStreamContext(MessageContext context) 
        throws IOException {
        return new TestOutputStreamContext(null, context);
    }

    public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) throws IOException {
    }

    public void invokeOneway(OutputStreamMessageContext context) throws IOException {
        //nothing to do
    }

    public InputStreamMessageContext invoke(OutputStreamMessageContext context) throws IOException {
        if (null != istreamCtx) {
            return istreamCtx;
        }
        return context.getCorrespondingInputStreamContext();
    }

    public Future<InputStreamMessageContext> invokeAsync(OutputStreamMessageContext context, Executor ex) 
        throws IOException {
        return null;
    }
    
    public ResponseCallback getResponseCallback() {
        return null;
    }

    public void shutdown() {
        //nothing to do
    }
    
    public void setInputStreamMessageContext(InputStreamMessageContext i) {
        istreamCtx = i;
    }
}

