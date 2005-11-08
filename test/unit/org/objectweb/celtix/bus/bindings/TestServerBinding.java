package org.objectweb.celtix.bus.bindings;

import java.io.IOException;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bindings.AbstractServerBinding;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.transports.TransportFactoryManager;

public class TestServerBinding extends AbstractServerBinding {

    protected final AbstractBindingImpl binding;
    String currentOperation = "undeclared";
    String schemeName = "test";

    public TestServerBinding(Bus b, EndpointReferenceType ref, Endpoint ep) {
        super(b, ref, ep);
        binding = new TestBinding();
    }

    protected MessageContext createBindingMessageContext(MessageContext ctx) {
        return new GenericMessageContext();
    }

    protected ServerTransport createTransport(EndpointReferenceType ref) 
        throws WSDLException, IOException {
        TransportFactoryManager tfm = bus.getTransportFactoryManager();
        String name = "http://celtix.objectweb.org/transports/test";
        TransportFactory tf = null;
        try {
            tf = tfm.getTransportFactory(name);
        } catch (BusException ex) {
            // ignore
        }
        if (tf == null) {
            tf = new TestTransportFactory();
            try {
                tfm.registerTransportFactory(name, tf);
            } catch (BusException ex) {
                System.out.println(ex.getMessage());
                return null;
            }
        }
        return tf.createServerTransport(ref);
    }

    protected void unmarshal(MessageContext context, ObjectMessageContext objContext) {
        // populate object context with test data depending on current operation
        // name
        if ("greetMe".equals(currentOperation)) {
            String name = System.getProperty("user.name");
            objContext.setMessageObjects(name);
        }
    }

    protected void marshal(ObjectMessageContext objContext, MessageContext context) {
    }

    protected void marshalFault(ObjectMessageContext objContext, MessageContext context) {
    }
    
    protected void read(InputStreamMessageContext inCtx, MessageContext context) throws IOException {
        context.put(MessageContext.WSDL_OPERATION, new QName(currentOperation));
    }

    protected void write(MessageContext context, OutputStreamMessageContext outCtx) throws IOException {
    }

    public Binding getBinding() {
        return binding;
    }

    public boolean isCompatibleWithAddress(String address) {
        return null != address && address.startsWith(schemeName);
    }
    
    protected MessageContext invokeOnProvider(MessageContext requestCtx, ServiceMode mode) {
        return null;
    }

    ServerTransport getTransport() {
        return transport;
    }

    public void triggerTransport() {
        if (transport instanceof TestServerTransport) {
            TestServerTransport tsb = (TestServerTransport)transport;
            tsb.fire();
        }
    }

    protected QName getOperationName(MessageContext ctx) {
        return new QName("blah", currentOperation);
    }
    
    class TestTransportFactory implements TransportFactory {

        public ClientTransport createClientTransport(EndpointReferenceType address) throws WSDLException,
            IOException {
            return null;
        }

        public ServerTransport createServerTransport(EndpointReferenceType address) throws WSDLException,
            IOException {
            return new TestServerTransport();
        }

        public ServerTransport createTransientServerTransport(EndpointReferenceType address)
            throws WSDLException, IOException {
            return null;
        }

        public void init(Bus bus) {
        }
    }

    class TestServerTransport implements ServerTransport {

        private ServerTransportCallback callback;

        public void shutdown() {
        }

        public void activate(ServerTransportCallback cb) throws IOException {
            callback = cb;
        }

        public OutputStreamMessageContext createOutputStreamContext(MessageContext context)
            throws IOException {
            return null;
        }

        public void deactivate() throws IOException {
        }

        public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) throws IOException {
        }

        public void fire() {
            callback.dispatch(null, this);
        }
    }
}
