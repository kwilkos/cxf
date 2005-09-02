package org.objectweb.celtix.bus.bindings.soap;

import java.net.URI;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.objectweb.celtix.context.GenericMessageContext;

class SOAPMessageContextImpl extends GenericMessageContext implements SOAPMessageContext {
    private static final long serialVersionUID = 1L;
    private static final String SOAP_MESSAGE = "org.objectweb.celtix.bindings.soap.message";
    private Set<URI> soapRoles;
    
    public SOAPMessageContextImpl() {
        //Complete
    }
    
    public SOAPMessage getMessage() {
        return (SOAPMessage)get(SOAP_MESSAGE);
    }

    public void setMessage(SOAPMessage soapMsg) {
        put(SOAP_MESSAGE, soapMsg);
        setScope(SOAP_MESSAGE, MessageContext.Scope.HANDLER);        
    }

    public Object[] getHeaders(QName header, JAXBContext jaxbContext, boolean allRoles) {
        throw new WebServiceException("Not supported yet");
    }

    public Set<URI> getRoles() {
        return soapRoles;
    }
}
