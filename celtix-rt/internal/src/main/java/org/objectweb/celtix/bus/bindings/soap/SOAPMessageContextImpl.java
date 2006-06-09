package org.objectweb.celtix.bus.bindings.soap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.objectweb.celtix.bus.jaxws.JAXBEncoderDecoder;
import org.objectweb.celtix.context.MessageContextWrapper;

class SOAPMessageContextImpl extends MessageContextWrapper implements SOAPMessageContext {
    private static final long serialVersionUID = 1L;
    private static final String SOAP_MESSAGE = "org.objectweb.celtix.bindings.soap.message";
    private Set<String> soapRoles;
    
    public SOAPMessageContextImpl(MessageContext ctx) {
        super(ctx);
    }
    
    public SOAPMessage getMessage() {
        return (SOAPMessage)get(SOAP_MESSAGE);
    }

    public void setMessage(SOAPMessage soapMsg) {
        put(SOAP_MESSAGE, soapMsg);
        setScope(SOAP_MESSAGE, MessageContext.Scope.HANDLER);        
    }

    public Object[] getHeaders(QName headerName, JAXBContext jaxbContext, boolean allRoles) {
        SOAPMessage msg = getMessage();
        assert msg != null;

        List<Object> headerList  = new ArrayList<Object>();

        SOAPHeader header = null;
        try {
            header = msg.getSOAPHeader();
        } catch (SOAPException se) {
            throw new WebServiceException("Could not get the SOAPHeader node", se);
        }
        
        if (header == null) {
            return new Object[0];
        }
        Iterator iter = header.getChildElements(headerName);

        //TODO Role/Actor attribute is not supported yet.
        //Assuming ultimate receiver.
        while (iter.hasNext()) {
            SOAPHeaderElement headerNode = (SOAPHeaderElement)iter.next();
            Object headerValue = 
                JAXBEncoderDecoder.unmarshall(jaxbContext, null, 
                                              headerNode, headerName, null);
            assert headerValue != null;
            headerList.add(headerValue);            
        }
        
        return headerList.toArray();
    }

    public Set<String> getRoles() {
        return soapRoles;
    }
}
