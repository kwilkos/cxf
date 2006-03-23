package org.objectweb.celtix.bus.bindings;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import javax.wsdl.WSDLException;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bindings.AbstractClientBinding;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.transports.TransportFactoryManager;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class TestClientBinding extends AbstractClientBinding {

    protected final AbstractBindingImpl binding;
    
    public TestClientBinding(Bus b, EndpointReferenceType ref) throws WSDLException, IOException {
        super(b, ref);
        binding = new TestBinding(this);
    }
    
    public AbstractBindingImpl getBindingImpl() {
        return  binding; 
    }
    
    public TestClientTransport getClientTransport() {
        if (null == transport) {
            try {
                transport = createTransport(reference);
            } catch (Exception e) {
                // leave at null
            }
        }
        return (TestClientTransport)transport;
    }
    
    protected ClientTransport createTransport(EndpointReferenceType ref) throws WSDLException, IOException {
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
        return tf.createClientTransport(ref);
    }

    protected Method getSEIMethod(List<Class<?>> classList, MessageContext ctx) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isBindingCompatible(String address) {
        // TODO Auto-generated method stub
        return true;
    }
}
    
    
