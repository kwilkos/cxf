package org.objectweb.celtix.bus.bindings.soap;

import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.security.SecurityConfiguration;
import javax.xml.ws.soap.SOAPBinding;

public class SOAPBindingImpl implements SOAPBinding {

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

    public List<Handler> getHandlerChain() {
        return null;
    }

    public void setHandlerChain(List<Handler> list) {
        throw new UnsupportedOperationException("setHandlerChain is not supported");
    }

    public SecurityConfiguration getSecurityConfiguration() {
        throw new UnsupportedOperationException("getSecurityConfiguration is not supported");
    }
}
