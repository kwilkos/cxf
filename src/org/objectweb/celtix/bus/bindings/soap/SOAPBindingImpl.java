package org.objectweb.celtix.bus.bindings.soap;

import java.io.IOException;
import java.io.InputStream;
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
import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bus.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bus.jaxws.JAXBEncoderDecoder;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.helpers.NSStack;

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

    public SOAPMessage marshalMessage(ObjectMessageContext objContext,
                                      MessageContext mc,
                                      DataBindingCallback callback)
        throws SOAPException {

        boolean isInputMsg = (Boolean)mc.get(ObjectMessageContext.MESSAGE_INPUT);

        SOAPMessage msg = initSOAPMessage();
        if (callback.getMode() == DataBindingCallback.Mode.MESSAGE) {
            //contains the entire SOAP message
            boolean found = false;
            for (Class<?> cls : callback.getSupportedFormats()) {
                if (cls == SOAPMessage.class) {
                    DataWriter<SOAPMessage> writer = callback.createWriter(SOAPMessage.class);
                    writer.write(objContext, msg);
                    found = true;
                    break;
                } else if (cls == SOAPPart.class) {
                    DataWriter<SOAPPart> writer = callback.createWriter(SOAPPart.class);
                    writer.write(objContext, msg.getSOAPPart());
                    found = true;
                    break;
                } else if (cls == SOAPElement.class) {
                    DataWriter<SOAPElement> writer = callback.createWriter(SOAPElement.class);
                    writer.write(objContext, msg.getSOAPPart().getEnvelope());
                    found = true;
                    break;
                } else if (cls == Document.class) {
                    DataWriter<Document> writer = callback.createWriter(Document.class);
                    writer.write(objContext, msg.getSOAPPart());
                    found = true;
                    break;
                } else {
                    //TODO - other formats to support? StreamSource/DOMSource/STaX/etc..
                }
            }
            if (!found) {
                throw new SOAPException("Could not figure out how to marshal data");
            }
        } else if (callback.getMode() == DataBindingCallback.Mode.PAYLOAD) {
            //contains the contents of the SOAP:Body
            boolean found = false;
            for (Class<?> cls : callback.getSupportedFormats()) {
                if (cls == SOAPBody.class) {
                    DataWriter<SOAPBody> writer = callback.createWriter(SOAPBody.class);
                    writer.write(objContext, msg.getSOAPPart().getEnvelope().getBody());
                    found = true;
                    break;
                } else if (cls == SOAPElement.class) {
                    DataWriter<SOAPElement> writer = callback.createWriter(SOAPElement.class);
                    writer.write(objContext, msg.getSOAPPart().getEnvelope().getBody());
                    found = true;
                    break;
                } else if (cls == Element.class) {
                    DataWriter<Element> writer = callback.createWriter(Element.class);
                    writer.write(objContext, msg.getSOAPPart().getEnvelope().getBody());
                    found = true;
                    break;
                } else {
                    //TODO - other formats to support? StreamSource/DOMSource/STaX/etc..
                }
            }
            if (!found) {
                throw new SOAPException("Could not figure out how to marshal data");
            }
        } else {
            if (callback.getSOAPStyle() == Style.RPC) {
                nsStack = new NSStack();
                nsStack.push();
            }
            SOAPElement soapElement = addOperationNode(msg.getSOAPBody(), callback, isInputMsg);

            //add out and inout params
            addParts(soapElement, objContext, isInputMsg, callback);

            if (msg.saveRequired()) {
                msg.saveChanges();
            }
        }
        return msg;
    }

    public SOAPMessage marshalFault(ObjectMessageContext objContext,
                                    MessageContext mc,
                                    DataBindingCallback callback) {

        SOAPMessage msg = null;

        try {
            msg = initSOAPMessage();
            Throwable t = (Throwable)objContext.getException();
            
            SOAPFault fault = msg.getSOAPBody().addFault();
            //REVIST FaultCode to handle other codes.
            fault.setFaultCode(SOAPConstants.FAULTCODE_SERVER);
            fault.setFaultString(t.getMessage());

            DataWriter<Detail> writer = callback.createWriter(Detail.class);
            if (writer == null) {
                throw new WebServiceException("Could not marshal fault details");
            }
            writer.write(t, fault.addDetail());
            if (fault.getDetail().getChildNodes().getLength() == 0) {
                fault.removeChild(fault.getDetail());
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

    public void unmarshalMessage(MessageContext mc,
                                 ObjectMessageContext objContext,
                                 DataBindingCallback callback)
        throws SOAPException {

        boolean isOutputMsg = (Boolean)mc.get(ObjectMessageContext.MESSAGE_INPUT);
        if (!SOAPMessageContext.class.isInstance(mc)) {
            throw new SOAPException("SOAPMessageContext not available");
        }

        SOAPMessageContext soapContext = SOAPMessageContext.class.cast(mc);
        SOAPMessage soapMessage = soapContext.getMessage();
        
        //Assuming No Headers are inserted.
        Node soapEl = soapMessage.getSOAPBody();

        if (callback.getSOAPStyle() == Style.RPC) {
            soapEl = soapEl.getFirstChild();
        }

        getParts(soapEl, callback, objContext, isOutputMsg);
    }

    public void unmarshalFault(MessageContext context,
                               ObjectMessageContext objContext,
                               DataBindingCallback callback)
        throws SOAPException {

        if (!SOAPMessageContext.class.isInstance(context)) {
            throw new SOAPException("SOAPMessageContext not available");
        }

        SOAPMessageContext soapContext = SOAPMessageContext.class.cast(context);
        SOAPMessage soapMessage = soapContext.getMessage();

        SOAPFault fault = soapMessage.getSOAPBody().getFault();

        DataReader<SOAPFault> reader = callback.createReader(SOAPFault.class);
        if (reader == null) {
            throw new WebServiceException("Could not unmarshal fault");
        }
        Object faultObj = reader.read(null, fault);
        if (faultObj == null) {
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

    private SOAPElement addOperationNode(SOAPElement body,
                                         DataBindingCallback callback,
                                         boolean isOutBound) throws SOAPException {

        String responseSuffix = isOutBound ? "Response" : "";

        if (callback.getSOAPStyle() == Style.RPC) {
            String  namespaceURI = callback.getTargetNamespace();
            nsStack.add(namespaceURI);
            String prefix = nsStack.getPrefix(namespaceURI);
            QName operationName = new QName(namespaceURI,
                                            callback.getOperationName() + responseSuffix, prefix);
           
            return body.addChildElement(operationName);
        }
        return body;
    }

    private void getParts(Node xmlNode , DataBindingCallback callback,
                          ObjectMessageContext objCtx, boolean isOutBound) throws SOAPException {

        if (callback.getSOAPStyle() != Style.RPC) {
            getWrappedDocLitParts(xmlNode , callback, objCtx, isOutBound);
        } else {
            getRPCLitParts(xmlNode , callback, objCtx, isOutBound);
        }
    }

    private void getWrappedDocLitParts(Node xmlNode , DataBindingCallback callback,
                                       ObjectMessageContext objCtx, boolean isOutBound) throws SOAPException {

        Method method = objCtx.getMethod();
        String wrapperType = isOutBound ? callback.getResponseWrapperType()
            : callback.getRequestWrapperType();
        
        Node childNode = xmlNode.getFirstChild();

        Object retVal = null;
        List<Object> paramList = new ArrayList<Object>();

        QName elName = isOutBound ? callback.getResponseWrapperQName()
            : callback.getRequestWrapperQName();

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
            WebParam param = callback.getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && !param.header()) {
                paramList.add(getWrappedPart(obj, method.getParameterTypes()[idx]));
            }
        }

        objCtx.setReturn(retVal);
        objCtx.setMessageObjects(paramList.toArray());
    }

    private void getRPCLitParts(Node xmlNode , DataBindingCallback callback,
                                ObjectMessageContext objCtx, boolean isOutBound) throws SOAPException {

        Method method = objCtx.getMethod();
        Node childNode = xmlNode.getFirstChild();

        Object retVal = null;
        List<Object> paramList = new ArrayList<Object>();
        if (isOutBound && !"void".equals(method.getReturnType().getName())) {
            retVal = JAXBEncoderDecoder.unmarshall(
                        childNode, 
                        new QName("", callback.getWebResultAnnotation().partName()), 
                        method.getReturnType());
            childNode = childNode.getNextSibling();
        }

        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        int noArgs = method.getParameterTypes().length;
        Class [] listOfParams = method.getParameterTypes();

        //Unmarshal parts of mode that should notbe ignored and are not part of the SOAP Headers
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = callback.getWebParam(idx);
            
            /*
            String paramNameSpace = param.targetNamespace();
            if ("".equals(param.targetNamespace())) {             
                paramNameSpace = messageInfo.getTargetNameSpace();
            } 
            */
            
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

    private void addParts(Node xmlNode,
                          ObjectMessageContext objCtx, boolean isOutBound,
                          DataBindingCallback callback) throws SOAPException {

        if (callback.getSOAPStyle() != Style.RPC) {
            addWrappedDocLitParts(xmlNode, objCtx, isOutBound, callback);
        } else {
            addRPCLitParts(xmlNode, objCtx, isOutBound, callback);
        }
    }

    private void addRPCLitParts(Node xmlNode,
                                ObjectMessageContext objCtx,
                                boolean isOutBound,
                                DataBindingCallback callback) throws SOAPException {

        Object[] args = objCtx.getMessageObjects();

        DataWriter<Node> writer = null;
        for (Class<?> cls : callback.getSupportedFormats()) {
            if (cls == Node.class) {
                writer = callback.createWriter(Node.class);
                break;
            } else {
                //TODO - other formats to support? StreamSource/DOMSource/STaX/etc..
            }
        }
        if (writer == null) {
            throw new SOAPException("Could not figure out how to marshal data");
        }
        
        
        //Add the Return Type
        if (isOutBound && callback.getWebResult() != null) {
            Object retVal = objCtx.getReturn();
            writer.write(retVal, callback.getWebResult(), xmlNode);
        }

        //Add the in,inout,out args depend on the inputMode
        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        int noArgs = callback.getParamsLength();

        //Marshal parts of mode that should notbe ignored and are not part of the SOAP Headers
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = callback.getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && !param.header()) {
                QName elName = new QName("", param.name());
                writer.write(args[idx], elName, xmlNode);
            }
        }
    }

    private void addWrappedDocLitParts(Node xmlNode,
                                       ObjectMessageContext objCtx,
                                       boolean isOutBound,
                                       DataBindingCallback callback) throws SOAPException {

        Object wrapperObj = callback.createWrapperType(objCtx, isOutBound);
        assert wrapperObj != null;

        QName elName = isOutBound ? callback.getResponseWrapperQName()
            : callback.getRequestWrapperQName();

        boolean found = false;
        for (Class<?> cls : callback.getSupportedFormats()) {
            if (cls == Node.class) {
                DataWriter<Node> writer = callback.createWriter(Node.class);
                writer.write(wrapperObj, elName, xmlNode);
                found = true;
                break;
            } else {
                //TODO - other formats to support? StreamSource/DOMSource/STaX/etc..
            }
        }
        if (!found) {
            throw new SOAPException("Could not figure out how to marshal data");
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


    public SOAPFactory getSOAPFactory() {
        return soapFactory;
    }

    private SOAPMessage initSOAPMessage() throws SOAPException {

        SOAPMessage msg = msgFactory.createMessage();
        msg.setProperty(SOAPMessage.WRITE_XML_DECLARATION,  "true");
        msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(W3CConstants.NP_SCHEMA_XSD,
                                                                W3CConstants.NU_SCHEMA_XSD);
        msg.getSOAPPart().getEnvelope().addNamespaceDeclaration(W3CConstants.NP_SCHEMA_XSI,
                                                                W3CConstants.NU_SCHEMA_XSI);

        //SOAP Headers not supported
        msg.getSOAPHeader().detachNode();
        return msg;
    }
}

