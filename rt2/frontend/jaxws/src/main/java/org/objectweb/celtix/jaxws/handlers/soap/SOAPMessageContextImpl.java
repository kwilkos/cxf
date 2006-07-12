package org.objectweb.celtix.jaxws.handlers.soap;

import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;


import org.objectweb.celtix.jaxws.context.WrappedMessageContext;
import org.objectweb.celtix.message.Message;

public class SOAPMessageContextImpl extends WrappedMessageContext  implements SOAPMessageContext {

    SOAPMessageContextImpl(Message m) {
        super(m);
    }

    public void setMessage(SOAPMessage message) {
        
    }
    
    public SOAPMessage getMessage() {
        return null;
    }
    
    public Object[] getHeaders(QName name, JAXBContext context, boolean b) {
        return null; 
    }

    public Set<String> getRoles() {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    
    
}
