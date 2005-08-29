package org.objectweb.celtix.bus.bindings.soap;

import java.net.URI;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.objectweb.celtix.context.GenericMessageContext;

class SOAPMessageContextImpl extends GenericMessageContext implements SOAPMessageContext {
    private static final long serialVersionUID = 1L;
    private Set<URI> soapRoles;
    private SOAPMessage soapMessage;
    
    public SOAPMessageContextImpl() {
        //Complete
    }
    
    public SOAPMessage getMessage() {
        return soapMessage;
    }

    public void setMessage(SOAPMessage soapMsg) {
        soapMessage = soapMsg;
    }

    public Object[] getHeaders(QName header, JAXBContext jaxbContext, boolean allRoles) {
        throw new WebServiceException("Not supported yet");
    }

    public Set<URI> getRoles() {
        return soapRoles;
    }
}
