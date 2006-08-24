package org.apache.cxf.jaxws.bindings.soap;


import java.util.Set;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.bindings.soap2.SoapBinding;
import org.apache.cxf.jaxws.bindings.BindingImpl;

public class SOAPBindingImpl extends BindingImpl implements SOAPBinding {

    // private SoapBinding soapBinding;

    public SOAPBindingImpl(SoapBinding sb) {
        // soapBinding = sb;
    }
    
    public Set<String> getRoles() {
        return null;
    }

    public void setRoles(Set<String> set) {
        // TODO
    }

    public boolean isMTOMEnabled() {
        return false;
    }

    public void setMTOMEnabled(boolean flag) {
        throw new WebServiceException("MTOM is not supported");
    }

    public MessageFactory getMessageFactory() {
        // TODO: get from wrapped SoapBinding
        return null;
    }  

    public SOAPFactory getSOAPFactory() {
        // TODO: get from wrapped SoapBinding
        return null;
    }
}
