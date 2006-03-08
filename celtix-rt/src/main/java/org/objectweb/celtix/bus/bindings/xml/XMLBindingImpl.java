package org.objectweb.celtix.bus.bindings.xml;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.*;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bus.handlers.HandlerChainInvoker;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;
import org.objectweb.celtix.helpers.NodeUtils;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class XMLBindingImpl extends AbstractBindingImpl {
    private static final Logger LOG = LogUtils.getL7dLogger(XMLBindingImpl.class);
    protected final XMLMessageFactory msgFactory;
    protected final boolean isServer;
    private final XMLUtils xmlUtils = new XMLUtils();

    private Bus bus;
    private EndpointReferenceType endpointRef;
    
    public XMLBindingImpl(boolean server) {
        isServer = server;
        msgFactory = XMLMessageFactory.newInstance();
    }

    public XMLBindingImpl(Bus b, EndpointReferenceType ert, boolean server) {
        this(server);
        this.bus = b;
        this.endpointRef = ert;
    }

    public Bus getBus() {
        return this.bus;
    }

    public EndpointReferenceType getEndpointReference() {
        return this.endpointRef;
    }
    
    // --- AbstractBindingImpl interface ---

    public MessageContext createBindingMessageContext(MessageContext srcCtx) {
        return new XMLMessageContextImpl(srcCtx);
    }

    public HandlerInvoker createHandlerInvoker() {
        return new HandlerChainInvoker(getHandlerChain(true));
    }

    public XMLMessageFactory getMessageFactory() {
        return this.msgFactory;
    }

    private XMLMessage initXMLMessage() {
        return msgFactory.createMessage();
    }

    public void marshal(ObjectMessageContext objContext, MessageContext mc, DataBindingCallback callback) {
        try {
            LOG.entering(getClass().getName(), "marshal");
            boolean isInputMsg = (Boolean)mc.get(ObjectMessageContext.MESSAGE_INPUT);
            XMLMessage msg = initXMLMessage();
            LOG.log(Level.INFO, "XML_MARSHALLING_START", xmlUtils.toString(msg.getRoot()));
            if (callback.getMode() == DataBindingCallback.Mode.PARTS) {
                if (callback.getSOAPStyle() == Style.DOCUMENT
                    && callback.getSOAPParameterStyle() == ParameterStyle.BARE) {
                    if (isInputMsg) {
                        addReturnWrapperRoot(msg, callback);
                        LOG.log(Level.INFO, "XML_MARSHALLING_BARE_OUT", xmlUtils.toString(msg.getRoot()));
                    } else {
                        addWrapperRoot(msg, callback);
                        LOG.log(Level.INFO, "XML_MARSHALLING_BARE_IN", xmlUtils.toString(msg.getRoot()));

                    }
                }
                addParts(msg.getRoot(), objContext, isInputMsg, callback);
            } else if (callback.getMode() == DataBindingCallback.Mode.MESSAGE) {
                throw new XMLBindingException("Could not figure out how to marshal data");
            } else if (callback.getMode() == DataBindingCallback.Mode.PAYLOAD) {
                throw new XMLBindingException("Could not figure out how to marshal data");
            }
            LOG.log(Level.INFO, "XML_MARSHALLING_END", xmlUtils.toString(msg.getRoot()));
            ((XMLMessageContext)mc).setMessage(msg);
            LOG.exiting(getClass().getName(), "marshal", "XML binding Mashal OK");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "XML_MARSHALLING_FAILURE_MSG", e);
            throw new XMLBindingException("XML binding marshal exception ", e);
        }
    }
    
    public void marshalFault(ObjectMessageContext objContext, MessageContext mc, 
                             DataBindingCallback callback) {
        // TODO
    }

    public void unmarshal(MessageContext mc, ObjectMessageContext objContext, DataBindingCallback callback) {
        try {
            LOG.entering(getClass().getName(), "unmarshal");

            boolean isOutputMsg = (Boolean)mc.get(ObjectMessageContext.MESSAGE_INPUT);
            if (!XMLMessageContext.class.isInstance(mc)) {
                throw new XMLBindingException("XMLMessageContext not available");
            }
            
            XMLMessageContext xmlContext = XMLMessageContext.class.cast(mc);
            XMLMessage xmlMessage = xmlContext.getMessage();
            
            if (callback.getMode() == DataBindingCallback.Mode.PARTS) {
                Node root = xmlMessage.getRoot();
                LOG.log(Level.INFO, "XML_UNMARSHALLING_START", xmlUtils.toString(root));
                getParts(root, callback, objContext, isOutputMsg);
                LOG.log(Level.INFO, "XML_UNMARSHALLING_END", xmlUtils.toString(root));
            } else if (callback.getMode() == DataBindingCallback.Mode.MESSAGE) {
                throw new XMLBindingException("Could not figure out how to marshal data");
            } else if (callback.getMode() == DataBindingCallback.Mode.PAYLOAD) {
                throw new XMLBindingException("Could not figure out how to marshal data");
            }
            LOG.exiting(getClass().getName(), "unmarshal", "XML binding Unmashal OK");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "XML_UNMARSHALLING_FAILURE_MSG", e);
            throw new XMLBindingException("XML binding unmarshal exception", e);
        }
    }
    
    public void unmarshalFault(MessageContext context, ObjectMessageContext objContext,
                               DataBindingCallback callback) {
        // TODO
    }

    public void write(MessageContext msgContext, OutputStreamMessageContext outContext) throws IOException {
        XMLMessageContext xmlContext = (XMLMessageContext) msgContext;
        try {
            xmlContext.getMessage().writeTo(outContext.getOutputStream());
            
            if (LOG.isLoggable(Level.FINE)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                xmlContext.getMessage().writeTo(baos);
                LOG.log(Level.FINE, baos.toString());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "XML_WRITE_FAILURE_MSG", e);
            throw new XMLBindingException("XML binding write exception ", e);
        }
    }
    
    public void read(InputStreamMessageContext inContext, MessageContext context) throws IOException {
        if (!XMLMessageContext.class.isInstance(context)) {
            throw new XMLBindingException("XMLMessageContext not available");
        }
        try {
            XMLMessageContext xmlContext = XMLMessageContext.class.cast(context);
            xmlContext.setMessage(msgFactory.createMessage(inContext.getInputStream()));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "XML_READ_FAILURE_MSG", e);
            throw new XMLBindingException("XML binding read exception ", e);
        }
    }

    public boolean hasFault(MessageContext msgContext) {
        // TODO
        return false;
    }

    public void updateMessageContext(MessageContext msgContext) {
        // TODO
    }

    private void getParts(Node xmlNode, DataBindingCallback callback, ObjectMessageContext objCtx,
                          boolean isOutBound) throws XMLBindingException {
        try {
            DataReader<Node> reader = null;
            for (Class<?> cls : callback.getSupportedFormats()) {
                if (cls == Node.class) {
                    reader = callback.createReader(Node.class);
                    break;
                }
            }

            if (reader == null) {
                throw new XMLBindingException("Could not figure out how to marshal data");
            }
            if (callback.getSOAPStyle() == Style.DOCUMENT
                && callback.getSOAPParameterStyle() == ParameterStyle.WRAPPED) {
                reader.readWrapper(objCtx, isOutBound, xmlNode);
                return;
            }

            Node childNode = NodeUtils.getChildElementNode(xmlNode);

            if (isOutBound && callback.getWebResult() != null) {
                Object retVal = reader.read(callback.getWebResultQName(), -1, childNode);
                objCtx.setReturn(retVal);
                childNode = childNode.getNextSibling();
            }

            WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
            int noArgs = callback.getParamsLength();
            Object[] methodArgs = objCtx.getMessageObjects();

            for (int idx = 0; idx < noArgs; idx++) {
                WebParam param = callback.getWebParam(idx);
                if (param.mode() != ignoreParamMode) {
                    QName elName = (callback.getSOAPStyle() == Style.DOCUMENT) 
                        ? new QName(param.targetNamespace(), param.name()) 
                        : new QName("", param.partName());

                    Object obj = reader.read(elName, idx, childNode);
                    if (param.mode() != WebParam.Mode.IN) {
                        try {
                            // TO avoid type safety warning the Holder
                            // needs tobe set as below.
                            methodArgs[idx].getClass().getField("value").set(methodArgs[idx], obj);
                        } catch (Exception ex) {
                            throw new XMLBindingException("Can not set the part value into the Holder field.",
                                                          ex);
                        }
                    } else {
                        methodArgs[idx] = obj;
                    }
                    childNode = childNode.getNextSibling();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addWrapperRoot(XMLMessage xmlMessage, DataBindingCallback callback) {
        WSDLHelper helper = new WSDLHelper();
        BindingOperation operation = helper.getBindingOperation(this.bus,
                                                                this.endpointRef,
                                                                callback.getOperationName());
        BindingInput input = operation.getBindingInput();

        XMLBinding xmlBinding = null;
        Iterator ite = input.getExtensibilityElements().iterator();
        while (ite.hasNext()) {
            Object obj = ite.next();
            if (obj instanceof XMLBinding) {
                xmlBinding = (XMLBinding)obj;
            }
        }

        if (needRootNode(operation, false)) {
            if (xmlBinding == null || xmlBinding.getRootNode() == null) {
                throw new XMLBindingException("Bare style must define the rootNode in this case!");
            }
            QName rootNode = xmlBinding.getRootNode();
            Document doc = xmlMessage.getRoot();
            String targetNamespace = rootNode.getNamespaceURI() == null
                ? callback.getTargetNamespace() : rootNode.getNamespaceURI();
            Element operationNode = doc.createElementNS(targetNamespace, rootNode.getLocalPart());
            xmlMessage.appendChild(operationNode);
        }
    }

    private void addReturnWrapperRoot(XMLMessage xmlMessage, DataBindingCallback callback) {
        try {
            WSDLHelper helper = new WSDLHelper();
            BindingOperation operation = helper.getBindingOperation(this.bus,
                                                                    this.endpointRef,
                                                                    callback.getOperationName());
            BindingOutput output = operation.getBindingOutput();
            XMLBinding xmlBinding = null;
            Iterator ite = output.getExtensibilityElements().iterator();
            while (ite.hasNext()) {
                Object obj = ite.next();
                if (obj instanceof XMLBinding) {
                    xmlBinding = (XMLBinding)obj;
                }
            }
            if (needRootNode(operation, true)) {
                if (xmlBinding == null || xmlBinding.getRootNode() == null) {
                    throw new XMLBindingException("Bare style must define the rootNode in this case!");
                }
                QName rootNode = xmlBinding.getRootNode();
                Document doc = xmlMessage.getRoot();
                String targetNamespace = rootNode.getNamespaceURI() == null
                    ? callback.getTargetNamespace() : rootNode.getNamespaceURI();
                Element operationNode = doc.createElementNS(targetNamespace, rootNode.getLocalPart());
                xmlMessage.appendChild(operationNode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean needRootNode(BindingOperation operation, boolean out) {
        WSDLHelper helper = new WSDLHelper();
        Map parts = helper.getParts(operation.getOperation(), out);
        return parts.size() != 1;
    }

    private void addParts(Node xmlNode,
                          ObjectMessageContext objCtx,
                          boolean isOutBound,
                          DataBindingCallback callback) {
        try {
            DataWriter<Node> writer = callback.createWriter(Node.class);
            if (writer == null) {
                throw new XMLBindingException("Could not figure out how to marshal data");
            }

            if (callback.getSOAPStyle() == Style.DOCUMENT
                && callback.getSOAPParameterStyle() == ParameterStyle.WRAPPED) {
                writer.writeWrapper(objCtx, isOutBound, xmlNode);
                return;
            }

            // Add the Return Type
            if (isOutBound && callback.getWebResult() != null) {
                writer.write(objCtx.getReturn(), callback.getWebResultQName(), xmlNode);
            }
            // Add the in,inout,out args depend on the inputMode
            WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
            int noArgs = callback.getParamsLength();
            Object[] args = objCtx.getMessageObjects();
            for (int idx = 0; idx < noArgs; idx++) {
                WebParam param = callback.getWebParam(idx);
                if (param.mode() != ignoreParamMode) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
