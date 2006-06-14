package org.objectweb.celtix.bus.bindings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bindings.AbstractServerBinding;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.MessageContextWrapper;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.transports.TransportFactoryManager;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class TestServerBinding extends AbstractServerBinding {

    protected final AbstractBindingImpl binding;
    String currentOperation = "undeclared";
    String schemeName = "test";

    public TestServerBinding(Bus b, EndpointReferenceType ref, 
                             ServerBindingEndpointCallback cbFactory) {
        super(b, ref, cbFactory);
        binding = new TestBinding(this);
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

    public AbstractBindingImpl getBindingImpl() {
        return binding;
    }

    public boolean isCompatibleWithAddress(String address) {
        return null != address && address.startsWith(schemeName);
    }
    
    protected MessageContext invokeOnProvider(MessageContext requestCtx, ServiceMode mode) {
        return null;
    }

    ServerTransport getTransport() {
        return serverTransport();
    }

    public void triggerTransport() {
        if (transport instanceof TestServerTransport) {
            TestServerTransport tsb = (TestServerTransport)transport;
            tsb.fire();
        }
    }
    public QName getOperationName(MessageContext ctx) {
        return new QName("", currentOperation);
    }


    static class ToyInputStreamMessageContext extends MessageContextWrapper 
        implements InputStreamMessageContext {
        private static final long serialVersionUID = 1;
        
        ToyInputStreamMessageContext(MessageContext wrapped) { 
            super(wrapped); 
        } 

        public InputStream getInputStream() { 
            return null; 
        }
    
        public void setInputStream(InputStream ins) {
        }

        public boolean isFault() { 
            return false;
        } 

        public void setFault(boolean isfault) { 
        }
    }

    static class ToyOutputStreamMessageContext extends MessageContextWrapper 
        implements OutputStreamMessageContext {
        private static final long serialVersionUID = 1;
        private OutputStream outputStream;
        
        ToyOutputStreamMessageContext(MessageContext wrapped) { 
            super(wrapped); 
        } 

        public OutputStream getOutputStream() { 
            if (null == outputStream) {
                outputStream = new ByteArrayOutputStream();
            }
            return outputStream; 
        }
    
        public void setOutputStream(OutputStream os) {
            outputStream = os;
        }

        public boolean isFault() { 
            return false; 
        } 

        public void setFault(boolean b) {
        }
        
        public void setOneWay(boolean isOneWay) {
            put(ONEWAY_MESSAGE_TF, isOneWay);
        }
        
        public boolean isOneWay() {
            return ((Boolean)get(ONEWAY_MESSAGE_TF)).booleanValue();
        }

        public InputStreamMessageContext getCorrespondingInputStreamContext()
            throws IOException {
            return null;
        }
    }

    public boolean isBindingCompatible(String address) {
        return address.contains("http:");
    }

}
