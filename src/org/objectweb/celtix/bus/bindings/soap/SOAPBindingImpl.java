package org.objectweb.celtix.bus.bindings.soap;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding.Style;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebFault;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.objectweb.celtix.bus.bindings.AbstractBindingImpl;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.helpers.NSStack;
import org.objectweb.celtix.helpers.WrapperHelper;

public class SOAPBindingImpl extends AbstractBindingImpl implements SOAPBinding {
    private static final Logger LOG = LogUtils.getL7dLogger(SOAPBindingImpl.class);
    protected final MessageFactory msgFactory;
    protected final SOAPFactory soapFactory;
    private NSStack nsStack;

    public SOAPBindingImpl() {
        try {
            msgFactory = MessageFactory.newInstance();
            soapFactory = SOAPFactory.newInstance();
        } catch (SOAPException se) {
            LOG.log(Level.SEVERE, "SAAJ_FACTORY_CREATION_FAILURE_MSG", se);
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
            LOG.log(Level.SEVERE, "INVALID_ADDRESS_MSG", ex);
            return false;
        }
        String protocol = url.getProtocol();
        return "http".equals(protocol) || "https".equals(protocol);
    }

    public MessageFactory getMessageFactory() {
        return msgFactory;
    }

    public SOAPMessage marshalMessage(ObjectMessageContext objContext, MessageContext mc)
        throws SOAPException {

        boolean isInputMsg = (Boolean)mc.get(ObjectMessageContext.MESSAGE_INPUT);

        SOAPMessage msg = initSOAPMessage();
        SOAPMessageInfo messageInfo = new SOAPMessageInfo(objContext.getMethod());

        if (messageInfo.getSOAPStyle() == Style.RPC) {
            nsStack = new NSStack();
            nsStack.push();
        }
        
        SOAPElement soapElement = addOperationNode(msg.getSOAPBody(), messageInfo, isInputMsg);

        //add out and inout params
        addParts(soapElement, messageInfo, objContext, isInputMsg);

        if (msg.saveRequired()) {
            msg.saveChanges();
        }

        return msg;
    }

    public SOAPMessage marshalFault(ObjectMessageContext objContext, MessageContext mc) {

        SOAPMessage msg = null;

        try {
            msg = initSOAPMessage();

            //SOAPMessageInfo messageInfo = new SOAPMessageInfo(objContext.getMethod());

            Throwable t = (Throwable) objContext.getException();
            WebFault wfAnnotation = t.getClass().getAnnotation(WebFault.class);

            SOAPFault fault = msg.getSOAPBody().addFault();
            //REVIST FaultCode to handle other codes.
            fault.setFaultCode(SOAPConstants.FAULTCODE_SERVER);
            fault.setFaultString(t.getMessage());

            if (null != wfAnnotation) {

                Object faultInfo = getFaultInfo(t);
                QName elName = new QName(wfAnnotation.targetNamespace(), wfAnnotation.name());
                JAXBEncoderDecoder.marshall(faultInfo, elName, fault.addDetail());
            }

            if (msg.saveRequired()) {
                msg.saveChanges();
            }
        } catch (SOAPException se) {
            LOG.log(Level.SEVERE, "FAULT_MARSHALLING_FAILURE_MSG", se);
            //Handle UnChecked Exception, Runtime Exception.
        }

        return msg;
    }

    public void unmarshalMessage(MessageContext mc, ObjectMessageContext objContext)
        throws SOAPException {

        boolean isOutputMsg = (Boolean)mc.get(ObjectMessageContext.MESSAGE_INPUT);
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

    public void unmarshalFault(MessageContext context, ObjectMessageContext objContext)
        throws SOAPException {

        if (!SOAPMessageContext.class.isInstance(context)) {
            throw new SOAPException("SOAPMessageContext not available");
        }

        SOAPMessageContext soapContext = SOAPMessageContext.class.cast(context);
        SOAPMessage soapMessage = soapContext.getMessage();
        SOAPMessageInfo messageInfo = new SOAPMessageInfo(objContext.getMethod());

        SOAPFault fault = soapMessage.getSOAPBody().getFault();
        Object faultObj = null;
        if (fault.getDetail() != null) {
            NodeList list = fault.getDetail().getChildNodes();
            assert list.getLength() == 1;

            QName faultName = new QName(list.item(0).getNamespaceURI(), list.item(0).getLocalName());
            Class<?> clazz = messageInfo.getWebFault(faultName);
            try {
                if (clazz != null) {
                    Class<?> faultInfo = clazz.getMethod("getFaultInfo").getReturnType();
                    Object obj = JAXBEncoderDecoder.unmarshall(list.item(0), faultName, faultInfo);
                    Constructor<?> ctor = clazz.getConstructor(String.class, obj.getClass());
                    faultObj = ctor.newInstance(fault.getFaultString(), obj);
                } else {
                    SOAPFaultException sfe = new SOAPFaultException(fault);
                    faultObj = sfe;
                }
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "FAULT_UNMARSHALLING_FAILURE_MSG", ex);
                throw new SOAPException("error in unmarshal of SOAPFault", ex);
            }
        } else {
            faultObj = new ProtocolException(fault.getFaultString());
        }


        objContext.setException((Throwable)faultObj);
    }

    public void parseMessage(InputStream in, MessageContext mc)
        throws SOAPException, IOException {

        if (!SOAPMessageContext.class.isInstance(mc)) {
            throw new SOAPException("SOAPMessageContext not available");
        }

        SOAPMessageContext soapContext = SOAPMessageContext.class.cast(mc);
        SOAPMessage soapMessage = null;
        try {
            MimeHeaders headers = new MimeHeaders();
            Map<?, ?> httpHeaders = (Map<?, ?>)mc.get(MessageContext.HTTP_REQUEST_HEADERS);

            if (httpHeaders == null) {
                httpHeaders = (Map<?, ?>)mc.get(MessageContext.HTTP_RESPONSE_HEADERS);
            }
            if (httpHeaders != null) {
                for (Object key : httpHeaders.keySet()) {
                    if (null != key) {
                        List<?> values = (List<?>)httpHeaders.get(key);
                        for (Object value : values) {
                            headers.addHeader(key.toString(),
                                              value == null ? null : value.toString());
                        }
                    }
                }
            }
            
            soapMessage = msgFactory.createMessage(headers, in);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            LOG.log(Level.INFO, "error in creating soap message", ex);
        }
        soapContext.setMessage(soapMessage);
    }

    private SOAPElement addOperationNode(SOAPElement body, SOAPMessageInfo messageInfo,
                                         boolean isOutBound) throws SOAPException {

        String responseSuffix = isOutBound ? "Response" : "";

        if (messageInfo.getSOAPStyle() == Style.RPC) {
            String  namespaceURI = messageInfo.getTargetNameSpace();
            nsStack.add(namespaceURI);
            String prefix = nsStack.getPrefix(namespaceURI);
            QName operationName = new QName(namespaceURI,
                                            messageInfo.getOperationName() + responseSuffix, prefix);
           
            return body.addChildElement(operationName);
        }
        return body;
    }

    private void getParts(Node xmlNode , SOAPMessageInfo messageInfo,
                          ObjectMessageContext objCtx, boolean isOutBound) throws SOAPException {

        if (messageInfo.getSOAPStyle() != Style.RPC) {
            getWrappedDocLitParts(xmlNode , messageInfo, objCtx, isOutBound);
        } else {
            getRPCLitParts(xmlNode , messageInfo, objCtx, isOutBound);
        }
    }

    private void getWrappedDocLitParts(Node xmlNode , SOAPMessageInfo messageInfo,
                                       ObjectMessageContext objCtx, boolean isOutBound) throws SOAPException {

        Method method = messageInfo.getMethod();
        String wrapperType = isOutBound ? messageInfo.getResponseWrapperType()
            : messageInfo.getRequestWrapperType();
        
        Node childNode = xmlNode.getFirstChild();

        Object retVal = null;
        List<Object> paramList = new ArrayList<Object>();

        QName elName = isOutBound ? messageInfo.getResponseWrapperQName()
            : messageInfo.getRequestWrapperQName();

        Object obj = null;

        try {
            obj = JAXBEncoderDecoder.unmarshall(childNode, elName, Class.forName(wrapperType));
        } catch (ClassNotFoundException e) {
            throw new SOAPException("Could not unmarshall wrapped type.");
        }

        if (isOutBound && !"void".equals(method.getReturnType().getName())) {
            retVal = getWrappedPart(obj, method.getReturnType());
        }

        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        int noArgs = method.getParameterTypes().length;
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = messageInfo.getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && !param.header()) {
                paramList.add(getWrappedPart(obj, method.getParameterTypes()[idx]));
            }
        }

        objCtx.setReturn(retVal);
        objCtx.setMessageObjects(paramList.toArray());
    }

    private void getRPCLitParts(Node xmlNode , SOAPMessageInfo messageInfo,
                                ObjectMessageContext objCtx, boolean isOutBound) throws SOAPException {

        Method method = messageInfo.getMethod();
        Node childNode = xmlNode.getFirstChild();

        Object retVal = null;
        List<Object> paramList = new ArrayList<Object>();
        if (isOutBound && !"void".equals(method.getReturnType().getName())) {
            retVal = JAXBEncoderDecoder.unmarshall(
                        childNode, 
                        new QName("", messageInfo.getWebResultAnnotation().partName()), 
                        method.getReturnType());
            childNode = childNode.getNextSibling();
        }

        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        int noArgs = method.getParameterTypes().length;
        Class [] listOfParams = method.getParameterTypes();

        //Unmarshal parts of mode that should notbe ignored and are not part of the SOAP Headers
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = messageInfo.getWebParam(idx);
            String paramNameSpace = param.targetNamespace();
            
            if ("".equals(param.targetNamespace())) {             
                paramNameSpace = messageInfo.getTargetNameSpace();
            } 
            
            if ((param.mode() != ignoreParamMode) && !param.header()) {
                QName elName = new QName("", param.partName());
                Object obj = JAXBEncoderDecoder.unmarshall(childNode, elName, listOfParams[idx]);
                paramList.add(obj);
                childNode = childNode.getNextSibling();
            }
        }

        objCtx.setReturn(retVal);
        objCtx.setMessageObjects(paramList.toArray());
    }

    private void addParts(Node xmlNode , SOAPMessageInfo messageInfo,
                          ObjectMessageContext objCtx, boolean isOutBound) throws SOAPException {

        if (messageInfo.getSOAPStyle() != Style.RPC) {
            addWrappedDocLitParts(xmlNode , messageInfo, objCtx, isOutBound);

        } else {
            addRPCLitParts(xmlNode , messageInfo, objCtx, isOutBound);
        }
    }

    private void addRPCLitParts(Node xmlNode , SOAPMessageInfo messageInfo,
                                ObjectMessageContext objCtx, boolean isOutBound) throws SOAPException {

        Method method = messageInfo.getMethod();
        Object[] args = objCtx.getMessageObjects();
        //Add the Return Type
        if (isOutBound && !"void".equals(method.getReturnType().getName())) {
            Object retVal = objCtx.getReturn();          
            JAXBEncoderDecoder.marshall(retVal, 
                                        new QName("",  messageInfo.getWebResultAnnotation().partName()), 
                                        xmlNode);
        }

        //Add the in,inout,out args depend on the inputMode
        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        int noArgs = method.getParameterTypes().length;

        //Unmarshal parts of mode that should notbe ignored and are not part of the SOAP Headers
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = messageInfo.getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && !param.header()) {
                QName elName = new QName("",  param.partName());
                JAXBEncoderDecoder.marshall(args[idx], elName, xmlNode);             
            }
        }
    }

    private void addWrappedDocLitParts(Node xmlNode , SOAPMessageInfo messageInfo,
                                       ObjectMessageContext objCtx, boolean isOutBound) throws SOAPException {

        Method method = messageInfo.getMethod();
        String wrapperType = isOutBound ? messageInfo.getResponseWrapperType()
            : messageInfo.getRequestWrapperType();
        
        Object wrapperObj = null;
        try {
            wrapperObj = Class.forName(wrapperType).newInstance();
        } catch (Exception ex) {
            throw new SOAPException("Could not create the wrapper element", ex);
        }

        if (isOutBound && !"void".equals(method.getReturnType().getName())) {
            setWrappedPart(messageInfo.getWebResult().getLocalPart(), wrapperObj, objCtx.getReturn());
        }

        //Add the in,inout,out args depend on the inputMode
        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        int noArgs = method.getParameterTypes().length;

        //Unmarshal parts of mode that should notbe ignored and are not part of the SOAP Headers
        Object[] args = objCtx.getMessageObjects();
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = messageInfo.getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && !param.header()) {
                setWrappedPart(param.partName(), wrapperObj, args[idx]);
            }
        }

        QName elName = isOutBound ? messageInfo.getResponseWrapperQName()
            : messageInfo.getRequestWrapperQName();

        JAXBEncoderDecoder.marshall(wrapperObj, elName, xmlNode);
    }

    private void setWrappedPart(String name, Object wrapperType, Object part) throws SOAPException {

        try {
            WrapperHelper.setWrappedPart(name, wrapperType, part);
        } catch (Exception ex) {
            throw new SOAPException("Could not set parts into wrapper element", ex);
        }
    }


    Object getWrappedPart(Object wrapperType, Class<?> part) throws SOAPException {
        try {
            Method elMethods[] = wrapperType.getClass().getMethods();
            for (Method method : elMethods) {
                if (method.getParameterTypes().length == 0
                    && method.getReturnType().equals(part)) {
                    return method.invoke(wrapperType);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "PART_RETREIVAL_FAILURE_MSG", ex);
            throw new SOAPException("Could not get part out of wrapper element", ex);
        }
        return null;
    }

    private Object getFaultInfo(Throwable fault) throws SOAPException {
        try {
            Method faultInfoMethod = fault.getClass().getMethod("getFaultInfo");
            if (faultInfoMethod != null) {
                return faultInfoMethod.invoke(fault);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "FAULT_INFO_RETREIVAL_FAILURE_MSG", ex);
            throw new SOAPException("Could not get faultInfo out of Exception", ex);
        }

        return null;
    }

    public SOAPFactory getSOAPFactory() {
        return soapFactory;
    }

    private SOAPMessage initSOAPMessage() throws SOAPException {

        SOAPMessage msg = msgFactory.createMessage();
        msg.setProperty(SOAPMessage.WRITE_XML_DECLARATION,  "true");
        msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(
                                                                W3CConstants.NP_SCHEMA_XSD,
                                                                W3CConstants.NU_SCHEMA_XSD);
        msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(
                                                                W3CConstants.NP_SCHEMA_XSI,
                                                                W3CConstants.NU_SCHEMA_XSI);

        //SOAP Headers not supported
        msg.getSOAPHeader().detachNode();
        return msg;
    }
}

