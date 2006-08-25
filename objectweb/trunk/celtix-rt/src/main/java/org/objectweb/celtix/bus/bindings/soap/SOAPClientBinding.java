package org.objectweb.celtix.bus.bindings.soap;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bindings.AbstractClientBinding;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;


public class SOAPClientBinding extends AbstractClientBinding {
    protected final SOAPBindingImpl soapBinding;
    
    public SOAPClientBinding(Bus b, EndpointReferenceType ref) throws WSDLException, IOException {
        super(b, ref);
        soapBinding = new SOAPBindingImpl(false);
    }
    
    public AbstractBindingImpl getBindingImpl() {
        return soapBinding;
    } 
    
    public boolean isBindingCompatible(String address) {
        return address.contains("http:");
    }
}
