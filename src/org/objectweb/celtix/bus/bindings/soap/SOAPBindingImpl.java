package org.objectweb.celtix.bus.bindings.soap;

import java.net.URI;
import java.net.URL;
import java.util.Set;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;

import org.objectweb.celtix.bus.bindings.BindingImpl;

public class SOAPBindingImpl extends BindingImpl implements SOAPBinding {
    protected final MessageFactory msgFactory;

    public SOAPBindingImpl() {
        try {
            msgFactory = MessageFactory.newInstance();
        } catch (SOAPException se) {
            throw new WebServiceException(se.getMessage());
        }        
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
    
    public SOAPMessage buildSoapMessage(MessageContext msgCtx) throws SOAPException {
        SOAPMessage msg = msgFactory.createMessage();
        //msg.setProperty(msg.WRITE_XML_DECLARATION,  new Boolean(true));
        //msg.getProperty(SOAPMessage.WRITE_XML_DECLARATION) = true;
        SOAPMessageInfo messageInfo = new SOAPMessageInfo(msgCtx);

        SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
        //REVISIT Populate The NameSpace Map at Envelope node.            

        SOAPBody body = envelope.getBody();
        //Populate The Obejcts into SAAJ Model based on SoapMessageInfo
            
        return msg;
    }
}
