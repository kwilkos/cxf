package org.objectweb.celtix.bus.bindings.soap;

import java.net.URL;

import javax.xml.ws.Binding;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bindings.ServerBinding;

public class SOAPServerBinding implements ServerBinding {
    
    protected final SOAPBindingImpl soapBinding;
    
    public SOAPServerBinding(Bus b, EndpointReferenceType ref) {
        soapBinding = new SOAPBindingImpl();
    }
    
    public Binding getBinding() {
        return soapBinding;
    }
    
    public boolean isCompatibleWithAddress(URL address) {
        String protocol = address.getProtocol();
        return "http".equals(protocol) || "https".equals(protocol);
    }

}
