package org.objectweb.celtix.bus.bindings.soap;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.logging.Logger;

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
import javax.xml.ws.soap.SOAPBinding;

import org.objectweb.celtix.bus.bindings.BindingImpl;
import org.objectweb.celtix.context.ObjectMessageContext;

public class SOAPBindingImpl extends BindingImpl implements SOAPBinding {
    private static Logger logger = Logger.getLogger(SOAPClientBinding.class.getName());
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

    public boolean isCompatibleWithAddress(String address) {
        URL url = null;
        try {
            url = new URL(address);
        } catch (MalformedURLException ex) {
            logger.severe("Invalid address:\n" + ex.getMessage());
        }
        String protocol = url.getProtocol();
        return "http".equals(protocol) || "https".equals(protocol);
    }
    
    public SOAPMessage buildSoapInputMessage(ObjectMessageContext msgCtx) 
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

    public SOAPMessage buildSoapOutputMessage(ObjectMessageContext msgCtx) 
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
            SOAPMessageInfo messageInfo, ObjectMessageContext msgCtx) throws SOAPException {

        SOAPElement childNode = null;
        WebParam param = messageInfo.getWebParam(0);
        if (param.mode() != WebParam.Mode.OUT) {
            Object[] params = (Object[])msgCtx.getMessageObjects();
            Method method = (Method) msgCtx.getMethod();

            JAXBEncoderDecoder encoder = 
                    new JAXBEncoderDecoder(method.getDeclaringClass().getPackage().getName());
            encoder.marshall(params[0], 
                             new QName(param.targetNamespace(), param.name()), 
                             soapElement);            
        }
    }    
        
    
    private void addOutputParam(SOAPElement soapElement ,
            SOAPMessageInfo messageInfo, ObjectMessageContext msgCtx) throws SOAPException {
        SOAPElement childNode = null;
        WebParam param = messageInfo.getWebParam(0);
        if (param.mode() != WebParam.Mode.IN) {
            childNode = soapElement.addChildElement(param.name(), "", param.targetNamespace());

            Object[] params = (Object[])msgCtx.getMessageObjects();
            Method method = (Method) msgCtx.getMethod();

            JAXBEncoderDecoder encoder = 
                    new JAXBEncoderDecoder(method.getDeclaringClass().getPackage().getName());
            encoder.marshall(params[0], new QName(param.targetNamespace(), param.name()) , childNode);
        }
    }    
    
    private void addReturn(SOAPElement soapElement ,
            SOAPMessageInfo messageInfo, ObjectMessageContext msgCtx) throws SOAPException {
        QName name = messageInfo.getWebResult();
        SOAPElement childNode = soapElement.addChildElement(name.getLocalPart(), "", name.getNamespaceURI());
        
        Object retVal = msgCtx.get("org.objectweb.celtix.return");
        Method method = (Method) msgCtx.getMethod();
       
        JAXBEncoderDecoder encoder = 
                new JAXBEncoderDecoder(method.getDeclaringClass().getPackage().getName());
        encoder.marshall(retVal, messageInfo.getWebResult(), childNode);        
    }    
}

