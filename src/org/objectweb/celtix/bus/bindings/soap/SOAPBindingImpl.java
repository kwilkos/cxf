package org.objectweb.celtix.bus.bindings.soap;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Set;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding.Style;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;

import org.objectweb.celtix.bus.bindings.BindingImpl;

public class SOAPBindingImpl extends BindingImpl implements SOAPBinding {
    protected final MessageFactory msgFactory;
    protected final SOAPFactory soapFactory;

    public SOAPBindingImpl() {
        try {
            msgFactory = MessageFactory.newInstance();
            soapFactory = SOAPFactory.newInstance();
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
    
    public SOAPMessage buildSoapInputMessage(MessageContext msgCtx) 
        throws SOAPException {
        SOAPMessageInfo messageInfo = new SOAPMessageInfo(msgCtx);
        
        SOAPMessage msg = msgFactory.createMessage();
        msg.setProperty(SOAPMessage.WRITE_XML_DECLARATION,  "true");
        SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
        //SOAPHeaders are NOT supported
        envelope.getHeader().detachNode();
        
        //REVISIT Populate The NameSpace Map at Envelope node.            
        SOAPBody body = envelope.getBody();
        SOAPElement soapElement = addOperationNode(body, messageInfo);
        
        //add in and inout params
        addInputParam(soapElement, messageInfo, msgCtx);

        if (msg.saveRequired()) {
            msg.saveChanges();
        }
        
        return msg;
    }

    public SOAPMessage buildSoapOutputMessage(MessageContext msgCtx) 
        throws SOAPException {
        SOAPMessage msg = msgFactory.createMessage();
        msg.setProperty(SOAPMessage.WRITE_XML_DECLARATION,  "true");
        SOAPMessageInfo messageInfo = new SOAPMessageInfo(msgCtx);

        SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
        //SOAPHeaders are NOT supported
        envelope.getHeader().detachNode();
        
        //REVISIT Populate The NameSpace Map at Envelope node.
        
        SOAPBody body = envelope.getBody();
        SOAPElement soapElement = addOperationNode(body, messageInfo);
        
        addReturn(soapElement, messageInfo, msgCtx);
        //add out and inout params
        addOutputParam(soapElement, messageInfo, msgCtx);

        if (msg.saveRequired()) {
            msg.saveChanges();
        }
        return msg;
    }
    
    private SOAPElement addOperationNode(SOAPBody body, SOAPMessageInfo messageInfo) throws SOAPException {
        if (messageInfo.getSOAPStyle() == Style.RPC) {
            // REVISIT This should be QName.
            Name opName = soapFactory.createName(messageInfo.getOperationName());
            return body.addBodyElement(opName);
        }
        return body;
    }

    private void addInputParam(SOAPElement soapElement ,
            SOAPMessageInfo messageInfo, MessageContext msgCtx) throws SOAPException {

        SOAPElement childNode = null;
        WebParam param = messageInfo.getWebParam(0);
        if (param.mode() != WebParam.Mode.OUT) {
            Object[] params = (Object[])msgCtx.get("org.objectweb.celtix.parameter");
            Method method = (Method) msgCtx.get("org.objectweb.celtix.method");

            JAXBEncoderDecoder encoder = 
                    new JAXBEncoderDecoder(method.getDeclaringClass().getPackage().getName());
            encoder.marshall(params[0], 
                             new QName(param.targetNamespace(), param.name()), 
                             soapElement);            
        }
    }    
        
    
    private void addOutputParam(SOAPElement soapElement ,
            SOAPMessageInfo messageInfo, MessageContext msgCtx) throws SOAPException {
        SOAPElement childNode = null;
        WebParam param = messageInfo.getWebParam(0);
        if (param.mode() != WebParam.Mode.IN) {
            childNode = soapElement.addChildElement(param.name(), "", param.targetNamespace());

            Object[] params = (Object[])msgCtx.get("org.objectweb.celtix.parameter");
            Method method = (Method) msgCtx.get("org.objectweb.celtix.method");

            JAXBEncoderDecoder encoder = 
                    new JAXBEncoderDecoder(method.getDeclaringClass().getPackage().getName());
            encoder.marshall(params[0], new QName(param.targetNamespace(), param.name()) , childNode);
        }
    }    
    
    private void addReturn(SOAPElement soapElement ,
            SOAPMessageInfo messageInfo, MessageContext msgCtx) throws SOAPException {
        QName name = messageInfo.getWebResult();
        SOAPElement childNode = soapElement.addChildElement(name.getLocalPart(), "", name.getNamespaceURI());
        
        Object retVal = msgCtx.get("org.objectweb.celtix.return");
        Method method = (Method) msgCtx.get("org.objectweb.celtix.method");
        
        JAXBEncoderDecoder encoder = 
                new JAXBEncoderDecoder(method.getDeclaringClass().getPackage().getName());
        encoder.marshall(retVal, messageInfo.getWebResult(), childNode);        
    }    
}

