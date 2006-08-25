package org.objectweb.celtix.bus.bindings.soap;




import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.Holder;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bus.bindings.AbstractBindingImpl;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.helpers.NSStack;

public class SOAPBindingImpl extends AbstractBindingImpl implements SOAPBinding {
    private static final Logger LOG = LogUtils.getL7dLogger(SOAPBindingImpl.class);
    protected final MessageFactory msgFactory;
    protected final SOAPFactory soapFactory;
    protected final boolean isServer;
    private final Collection<String> supportedProtocols = new LinkedList<String>(); 
    private NSStack nsStack;

    public SOAPBindingImpl(boolean server) {
        try {
            isServer = server;
            msgFactory = MessageFactory.newInstance();
            soapFactory = SOAPFactory.newInstance();

            // REVISIT: these should be read from configuration
            supportedProtocols.add("http");
            supportedProtocols.add("https");
            supportedProtocols.add("jms");
            supportedProtocols.add("jbi");

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
            LogUtils.log(LOG, Level.SEVERE, "INVALID_ADDRESS_MSG", ex, address);
            return false;
        }
        String protocol = url.getProtocol();

        return supportedProtocols.contains(protocol);
    }

    public MessageFactory getMessageFactory() {
        return msgFactory;
    }

    public void setHandlerChain(List<Handler> arg0) {
        super.setHandlerChain(arg0);
        handlerChain.add(new SOAPHeaderHandler(this, isServer));
    }

    public SOAPMessage marshalMessage(ObjectMessageContext objContext,
                                      MessageContext mc,
                                      DataBindingCallback callback)
        throws SOAPException {

        boolean isInputMsg = (Boolean)mc.get(ObjectMessageContext.MESSAGE_INPUT);

        SOAPMessage msg = initSOAPMessage();
        if (!"".equals(callback.getSOAPAction())) {
            msg.getMimeHeaders().setHeader("SOAPAction",
                                           "\"" + callback.getSOAPAction() + "\"");
        }
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
            //add in, out and inout non-header params
            addParts(soapElement, objContext, isInputMsg, callback);
        }
        return msg;
    }
    
    public SOAPMessage marshalFault(ObjectMessageContext objContext,
                                    MessageContext mc,
                                    DataBindingCallback callback) {

        SOAPMessage msg = null;

        try {
            msg = SOAPMessageContext.class.isInstance(mc)
                  && ((SOAPMessageContext)mc).getMessage() != null
                  ? ((SOAPMessageContext)mc).getMessage()
                  : initSOAPMessage();
            Throwable t = objContext.getException();

            SOAPFault fault = msg.getSOAPBody().addFault();
            //REVIST FaultCode to handle other codes.
            if (t instanceof SOAPFaultException) {
                SOAPFault f = ((SOAPFaultException)t).getFault();
                fault.setFaultCode(f.getFaultCodeAsName());
                fault.setFaultString(f.getFaultString());
            } else {
                fault.setFaultCode(SOAPConstants.FAULTCODE_SERVER);
                fault.setFaultString(t.getMessage());
            }

            DataWriter<Detail> writer = callback.createWriter(Detail.class);
            if (writer == null) {
                throw new WebServiceException("Could not marshal fault details");
            }
            writer.write(t, fault.addDetail());
            if (fault.getDetail().getChildNodes().getLength() == 0) {
                fault.removeChild(fault.getDetail());
            }
        } catch (SOAPException se) {
            LOG.log(Level.SEVERE, "FAULT_MARSHALLING_FAILURE_MSG", se);
            //Handle UnChecked Exception, Runtime Exception.
        }

        return msg;
    }

    @SuppressWarnings("unchecked")
    public void updateHeaders(MessageContext ctx, SOAPMessage msg) throws SOAPException {
        if (msg.saveRequired()) {
            msg.saveChanges();
        }
        MimeHeaders headers = msg.getMimeHeaders();
        Map<String, List<String>> reqHead;
        String inOutKey = MessageContext.HTTP_REQUEST_HEADERS;
        if (isServer) {
            inOutKey = MessageContext.HTTP_RESPONSE_HEADERS;
        }
        reqHead = (Map<String, List<String>>)ctx.get(inOutKey);
        if (reqHead == null) {
            reqHead = new HashMap<String, List<String>>();
            ctx.put(inOutKey, reqHead);
        }
        Iterator it = headers.getAllHeaders();
        while (it.hasNext()) {
            MimeHeader header = (MimeHeader)it.next();
            if (!"Content-Length".equals(header.getName())) {
                List<String> vals = reqHead.get(header.getName());
                if (null == vals) {
                    vals = new ArrayList<String>();
                    reqHead.put(header.getName(), vals);
                }
                vals.add(header.getValue());
            }
        }
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


        if (callback.getMode() == DataBindingCallback.Mode.MESSAGE) {
            //TODO - unmarshal to a full message
        } else if (callback.getMode() == DataBindingCallback.Mode.PAYLOAD) {
            //TODO - unmarshal to payload
        } else {
            //Assuming No Headers are inserted.
            Node soapEl = soapMessage.getSOAPBody();

            if (callback.getSOAPStyle() == Style.RPC) {
                soapEl = soapEl.getFirstChild();
            }

            getParts(soapEl, callback, objContext, isOutputMsg);
        }
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
        Object faultObj = reader.read(null, 0, fault);
        if (faultObj == null) {
            faultObj = new ProtocolException(fault.getFaultString());
        }

        objContext.setException((Throwable)faultObj);
    }

    @SuppressWarnings("unchecked")
    public void parseMessage(InputStream in, MessageContext mc)
        throws SOAPException, IOException {

        if (!SOAPMessageContext.class.isInstance(mc)) {
            throw new SOAPException("SOAPMessageContext not available");
        }

        SOAPMessageContext soapContext = SOAPMessageContext.class.cast(mc);
        SOAPMessage soapMessage = null;
        MimeHeaders headers = new MimeHeaders();
        try {
            Map<String, List<String>> httpHeaders;
            if (isServer) {
                httpHeaders = (Map<String, List<String>>)mc.get(MessageContext.HTTP_REQUEST_HEADERS);
            } else {
                httpHeaders = (Map<String, List<String>>)mc.get(MessageContext.HTTP_RESPONSE_HEADERS);
            }
            if (httpHeaders != null) {
                for (String key : httpHeaders.keySet()) {
                    if (null != key) {
                        List<String> values = httpHeaders.get(key);
                        for (String value : values) {
                            headers.addHeader(key, value);
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

        DataReader<Node> reader = null;
        for (Class<?> cls : callback.getSupportedFormats()) {
            if (cls == Node.class) {
                reader = callback.createReader(Node.class);
                break;
            } else {
                //TODO - other formats to support? StreamSource/DOMSource/STaX/etc..
            }
        }

        if (reader == null) {
            throw new SOAPException("Could not figure out how to marshal data");
        }

        if (callback.getSOAPStyle() == Style.DOCUMENT
            && callback.getSOAPParameterStyle() == ParameterStyle.WRAPPED) {
            reader.readWrapper(objCtx, isOutBound, xmlNode);
            return;
        }

        Node childNode = xmlNode.getFirstChild();
        if (isOutBound 
            && callback.getWebResult() != null 
            && !callback.getWebResult().header()) {
            
            Object retVal = reader.read(callback.getWebResultQName(), -1, childNode);
            objCtx.setReturn(retVal);
            childNode = childNode.getNextSibling();
        }

        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        int noArgs = callback.getParamsLength();

        //Unmarshal parts of mode that should notbe ignored and are not part of the SOAP Headers
        Object[] methodArgs = objCtx.getMessageObjects();
        
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = callback.getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && !param.header()) {

                QName elName = (callback.getSOAPStyle() == Style.DOCUMENT)
                                ? new QName(param.targetNamespace(), param.name())
                                : new QName("", param.partName());

                Object obj = reader.read(elName, idx, childNode);
                if (param.mode() != WebParam.Mode.IN) {
                    try {
                        //TO avoid type safety warning the Holder
                        //needs tobe set as below.
                        methodArgs[idx].getClass().getField("value").set(methodArgs[idx], obj);
                    } catch (Exception ex) {
                        throw new SOAPException("Can not set the part value into the Holder field.");
                    }
                } else {
                    methodArgs[idx] = obj;
                }
                childNode = childNode.getNextSibling();
            }
        }
    }

    private void addParts(Node xmlNode,
                           ObjectMessageContext objCtx,
                           boolean isOutBound,
                           DataBindingCallback callback) throws SOAPException {

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

        if (callback.getSOAPStyle() == Style.DOCUMENT
            && callback.getSOAPParameterStyle() == ParameterStyle.WRAPPED) {
            writer.writeWrapper(objCtx, isOutBound, xmlNode);
            return;
        }

        //Add the Return Type
        if (isOutBound 
            && callback.getWebResult() != null
            && !callback.getWebResult().header()) {
            writer.write(objCtx.getReturn(), callback.getWebResultQName(), xmlNode);
        }

        //Add the in,inout,out args depend on the inputMode
        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        int noArgs = callback.getParamsLength();

        //Marshal parts of mode that should notbe ignored and are not part of the SOAP Headers
        Object[] args = objCtx.getMessageObjects();
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = callback.getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && !param.header()) {
                Object partValue = args[idx];
                if (param.mode() != WebParam.Mode.IN) {
                    partValue = ((Holder)args[idx]).value;
                }

                QName elName = (callback.getSOAPStyle() == Style.DOCUMENT)
                                ? new QName(param.targetNamespace(), param.name())
                                : new QName("", param.partName());
                writer.write(partValue, elName, xmlNode);
            }
        }
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

        return msg;
    }
}

