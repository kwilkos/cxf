package org.objectweb.celtix.bus.bindings.soap;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding.Style;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
//import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
//import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;

import org.w3c.dom.Node;

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

    public MessageFactory getMessageFactory() {
        return msgFactory;
    }
    
    public SOAPMessage marhsalMessage(ObjectMessageContext objContext, MessageContext mc) 
        throws SOAPException {

        boolean isInputMsg = (Boolean)mc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        
        SOAPMessage msg = msgFactory.createMessage();
        msg.setProperty(SOAPMessage.WRITE_XML_DECLARATION,  "true");
        SOAPMessageInfo messageInfo = new SOAPMessageInfo(objContext.getMethod());

        //SOAPHeaders, SwA are NOT supported
        msg.getSOAPHeader().detachNode();
        
        //REVISIT Populate The NameSpace Map at Envelope node.
        
        SOAPElement soapElement = addOperationNode(msg.getSOAPBody(), messageInfo);
        
        //add out and inout params
        addParts(soapElement, messageInfo, objContext, isInputMsg);

        if (msg.saveRequired()) {
            msg.saveChanges();
        }
        return msg;
    }
    
    public void unmarshalMessage(MessageContext mc, ObjectMessageContext objContext) 
        throws SOAPException {
        
        boolean isOutputMsg = (Boolean)mc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (!SOAPMessageContext.class.isInstance(mc)) {
            throw new SOAPException("SOAPMessageContext not available");
        }

        SOAPMessageContext soapContext = SOAPMessageContext.class.cast(mc);
        SOAPMessage soapMessage = soapContext.getMessage();
        
        //Assuming No Headers are inserted.
        Node soapEl = soapMessage.getSOAPBody();
        
        SOAPMessageInfo messageInfo = new SOAPMessageInfo(objContext.getMethod());
        
        if (messageInfo.getSOAPStyle() == Style.RPC) {
            soapEl = soapEl.getFirstChild();
        }

        getParts(soapEl, messageInfo, objContext, isOutputMsg);
    }

    public void parseInputMessage(InputStream in, MessageContext mc) 
        throws SOAPException, IOException {

        if (!SOAPMessageContext.class.isInstance(mc)) {
            throw new SOAPException("SOAPMessageContext not available");
        }

        SOAPMessageContext soapContext = SOAPMessageContext.class.cast(mc);
        SOAPMessage soapMessage = 
                getMessageFactory().createMessage(null, in);
        soapContext.setMessage(soapMessage);

        QName opName = getOperationName(soapMessage);

        mc.put(MessageContext.WSDL_OPERATION, opName);
    }

    private QName getOperationName(SOAPMessage soapMessage) {
        QName opName = null;

        return opName;
    }

    private SOAPElement addOperationNode(SOAPElement body, SOAPMessageInfo messageInfo) throws SOAPException {
        if (messageInfo.getSOAPStyle() == Style.RPC) {
            // REVISIT This should be QName.
            return body.addChildElement(messageInfo.getOperationName());
        }
        return body;
    }

    private void getParts(Node xmlNode , SOAPMessageInfo messageInfo,
                              ObjectMessageContext objCtx, boolean isOutBound) throws SOAPException {

        Method method = messageInfo.getMethod();
        JAXBEncoderDecoder decoder = 
            new JAXBEncoderDecoder(method.getDeclaringClass().getPackage().getName());

        Node childNode = xmlNode.getFirstChild();
        
        Object retVal = null;
        if (isOutBound && !"void".equals(method.getReturnType().getName())) {
            retVal = decoder.unmarshall(childNode, messageInfo.getWebResult());
            childNode = childNode.getNextSibling();
        }

        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;                
        int noArgs = method.getParameterTypes().length;
        ArrayList<Object> replyParamList = new ArrayList<Object>();
        int argsCnt = 0;
        
        //Unmarshal parts of mode that should notbe ignored and are not part of the SOAP Headers
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = messageInfo.getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && !param.header()) {
                Object obj = decoder.unmarshall(
                                childNode,
                                new QName(param.targetNamespace(), param.name()));
                replyParamList.add(obj);
                childNode = childNode.getNextSibling();
                ++argsCnt;
            }
        }
        
        objCtx.setReturn(retVal);
        objCtx.setMessageObjects(replyParamList.toArray());
    }    

    private void addParts(Node xmlNode , SOAPMessageInfo messageInfo, 
                          ObjectMessageContext objCtx, boolean isOutBound) throws SOAPException {

        Method method = messageInfo.getMethod();
        JAXBEncoderDecoder encoder = 
            new JAXBEncoderDecoder(method.getDeclaringClass().getPackage().getName());

        if (isOutBound && !"void".equals(method.getReturnType().getName())) {
            Object retVal = objCtx.getReturn();
            encoder.marshall(retVal, messageInfo.getWebResult(), xmlNode);            
        }

        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;                
        int noArgs = method.getParameterTypes().length;
        Object[] args = objCtx.getMessageObjects();
        
        //Unmarshal parts of mode that should notbe ignored and are not part of the SOAP Headers
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = messageInfo.getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && !param.header()) {
                encoder.marshall(args[idx], 
                                new QName(param.targetNamespace(), param.name()),
                                xmlNode);
            }
        }
    }    
}

