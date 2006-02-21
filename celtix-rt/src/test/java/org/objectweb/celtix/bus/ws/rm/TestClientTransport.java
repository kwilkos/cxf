package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import javax.wsdl.Port;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.bindings.soap.TestOutputStreamContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

class TestClientTransport implements ClientTransport {
    
    private InputStreamMessageContext istreamCtx;
    
    public TestClientTransport(Bus b, EndpointReferenceType ref) {
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
        return istreamCtx;
    }

    public Future<InputStreamMessageContext> invokeAsync(OutputStreamMessageContext context, Executor ex) 
        throws IOException {
        return null;
    }

    public void shutdown() {
        //nothing to do
    }
    
    public void setInputStreamMessageContext(InputStreamMessageContext i) {
        istreamCtx = i;
    }
}