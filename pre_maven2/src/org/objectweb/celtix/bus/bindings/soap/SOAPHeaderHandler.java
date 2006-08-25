package org.objectweb.celtix.bus.bindings.soap;


import java.lang.reflect.Method;
//import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Holder;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.context.ObjectMessageContext;

/*
 * This SOAPHandler will marsahll/unmarshall the 
 * - soap:header parts defined in the wsdl.
 */
public class SOAPHeaderHandler implements SOAPHandler<SOAPMessageContext> {

    //private final SOAPBinding binding;
    private final boolean isServer;
    private SOAPMessageContext soapCtx;

    public SOAPHeaderHandler(SOAPBinding arg0, boolean server) {
        //binding = arg0;
        isServer = server;
        soapCtx = null;
    }
    
    public void init(Map c) {
        System.out.println("LoggingHandler : init() Called....");
    }

    public Set<QName> getHeaders() {
        Set<QName> set = new HashSet<QName>();
        if (soapCtx != null) {
            //TODO get the set of Header that will be processed.
        }
        return set;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        Boolean isInputMessage = (Boolean) smc.get(ObjectMessageContext.MESSAGE_INPUT);
        Method method = (Method) smc.get(ObjectMessageContext.METHOD_OBJ);
        DataBindingCallback callback = 
            new JAXBDataBindingCallback(method, DataBindingCallback.Mode.PARTS, null);

        if (isServer) {
            if (isInputMessage.booleanValue()) {
                writeHeaders(smc, callback);
            } else {
                readHeaders(smc, callback);
            }
        } else {
            if (isInputMessage.booleanValue()) {
                readHeaders(smc, callback);
            } else {
                writeHeaders(smc, callback);
            }
        }
        return true;
    }
      
    public boolean handleFault(SOAPMessageContext smc) {
        //System.out.println("LoggingHandler : handleFault Called....");
        //logToSystemOut(smc);
        return true;
    }

    // nothing to clean up
    public void close(MessageContext messageContext) {
        //System.out.println("LoggingHandler : close() Called....");
        soapCtx = null;
    }

    // nothing to clean up
    public void destroy() {
        //System.out.println("LoggingHandler : destroy() Called....");
    }

    protected void writeHeaders(SOAPMessageContext smc, DataBindingCallback callback) {
        try {
            SOAPMessage msg = smc.getMessage();
            //TODO Should throw a exception or ignore
            if (msg.getSOAPHeader() != null) {
                addHeaderParts(msg.getSOAPHeader(), smc, callback);
                msg.saveChanges();
            }
        } catch (SOAPException se) {
            throw new ProtocolException("SoapHeaderHandler fail to marshall header parts.", se);
        }
    }
    
    protected void readHeaders(SOAPMessageContext smc, DataBindingCallback callback) {
        try {
            SOAPMessage msg = smc.getMessage();        
            getHeaderParts(msg.getSOAPHeader(), callback, smc);
        } catch (SOAPException se) {
            throw new ProtocolException("SoapHeaderHandler fail to unmarshall header parts.", se);
        }        
    }
    
    private void getHeaderParts(Element header, 
                                DataBindingCallback callback,
                                SOAPMessageContext objCtx) throws SOAPException {

        if (header == null || !header.hasChildNodes()) {
            return;
        }
        
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
        
        boolean isOutBound = 
            ((Boolean) objCtx.get(ObjectMessageContext.MESSAGE_INPUT)).booleanValue();
        if (isOutBound 
            && callback.getWebResult() != null
            && callback.getWebResult().header()) {
            
            QName elName = callback.getWebResultQName();
            NodeList headerElems =
                header.getElementsByTagNameNS(elName.getNamespaceURI(), elName.getLocalPart());
            assert headerElems.getLength() == 1;
            Node childNode = headerElems.item(0);
            
            Object retVal = reader.read(elName, -1, childNode);
            objCtx.put(ObjectMessageContext.METHOD_RETURN, retVal);
        }

        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        int noArgs = callback.getParamsLength();
        
        //Unmarshal parts of mode that should notbe ignored and are not part of the SOAP Headers
        Object[] methodArgs = (Object[])objCtx.get(ObjectMessageContext.METHOD_PARAMETERS);
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = callback.getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && param.header()) {
                QName elName = new QName(param.targetNamespace(), param.name());
                NodeList headerElems =
                        header.getElementsByTagNameNS(elName.getNamespaceURI(), elName.getLocalPart());
                assert headerElems.getLength() == 1;
                Node childNode = headerElems.item(0);

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
            }
        }
    }
    
    private void addHeaderParts(SOAPElement header,
                                SOAPMessageContext objCtx,
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

        boolean isOutBound = (Boolean) objCtx.get(ObjectMessageContext.MESSAGE_INPUT);        
        if (isOutBound 
            && callback.getWebResult() != null
            && callback.getWebResult().header()) {
            
            writer.write(objCtx.get(ObjectMessageContext.METHOD_RETURN), 
                         callback.getWebResultQName(), 
                         header);
            addSOAPHeaderAttributes(header, callback.getWebResultQName(), true);
        }

        //Add the in,inout,out args depend on the inputMode
        WebParam.Mode ignoreParamMode = isOutBound ? WebParam.Mode.IN : WebParam.Mode.OUT;
        int noArgs = callback.getParamsLength();

        //Marshal parts of mode that should notbe ignored and are not part of the SOAP Headers
        Object[] args = (Object[])objCtx.get(ObjectMessageContext.METHOD_PARAMETERS);
        for (int idx = 0; idx < noArgs; idx++) {
            WebParam param = callback.getWebParam(idx);
            if ((param.mode() != ignoreParamMode) && param.header()) {
                Object partValue = args[idx];
                if (param.mode() != WebParam.Mode.IN) {
                    partValue = ((Holder)args[idx]).value;
                }
    
                QName elName = new QName(param.targetNamespace(), param.name());
                writer.write(partValue, elName, header);
                
                addSOAPHeaderAttributes(header, elName, true);
            }
        }

        if (!header.hasChildNodes()) {
            header.detachNode();
        }
    }
 
    private void addSOAPHeaderAttributes(Element header, QName elName, boolean mustUnderstand) {
        //Set mustUnderstand Attribute on header parts.
        NodeList children =
                header.getElementsByTagNameNS(elName.getNamespaceURI(), elName.getLocalPart());
        assert children.getLength() == 1;
        //Set the mustUnderstand attribute
        if (children.item(0) instanceof Element) {
            Element child = (Element)(children.item(0));
            child.setAttribute(SOAPConstants.HEADER_MUSTUNDERSTAND, String.valueOf(mustUnderstand));
        }

        //TODO Actor/Role Attribute.
    }  
}
