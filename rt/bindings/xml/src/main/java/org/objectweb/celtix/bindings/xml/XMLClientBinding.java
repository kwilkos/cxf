package org.objectweb.celtix.bindings.xml;

import java.io.IOException;
import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bindings.AbstractClientBinding;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class XMLClientBinding extends AbstractClientBinding {
    protected final XMLBindingImpl xmlBinding;
    
    public XMLClientBinding(Bus b, EndpointReferenceType ref) throws WSDLException, IOException {
        super(b, ref);
        xmlBinding = new XMLBindingImpl(b, ref, false);
    }
    
    public AbstractBindingImpl getBindingImpl() {
        return xmlBinding;
    }

    public boolean isBindingCompatible(String address) {
        // TODO Auto-generated method stub
        return false;
    }  
}
