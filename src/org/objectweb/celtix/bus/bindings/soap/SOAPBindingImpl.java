package org.objectweb.celtix.bus.bindings.soap;

import java.net.URI;
import java.net.URL;
import java.util.Set;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;

import org.objectweb.celtix.bus.bindings.BindingImpl;

public class SOAPBindingImpl extends BindingImpl implements SOAPBinding {

    public SOAPBindingImpl() {
        //TODO
    }

    public Set<URI> getRoles() {
        return null;
    }
    
    public void setRoles(Set<URI> set) {
        //TODO
    }

    public boolean isMTOMEnabled() {
        return false;
    }

    public void setMTOMEnabled(boolean flag) {
        throw new WebServiceException("MTOM is not supported");
    }

    public boolean isCompatibleWithAddress(URL address) {
        String protocol = address.getProtocol();
        if ("http".equals(protocol) || "https".equals(protocol)) {
            return true;
        }
        return false;
    }
    
    
}
