package org.objectweb.celtix.bus.bindings.soap;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Endpoint;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bindings.AbstractServerBinding;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.helpers.NodeUtils;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class SOAPServerBinding extends AbstractServerBinding {
    
    private static final Logger LOG = LogUtils.getL7dLogger(SOAPServerBinding.class);
    
    protected final SOAPBindingImpl soapBinding;
    
    public SOAPServerBinding(Bus b,
                             EndpointReferenceType ref,
                             Endpoint ep,
                             ServerBindingEndpointCallback cbFactory) {
        super(b, ref, ep, cbFactory);
        soapBinding = new SOAPBindingImpl(true);
    }
    
    public AbstractBindingImpl getBindingImpl() {
        return soapBinding;
    }

    protected ServerTransport createTransport(EndpointReferenceType ref) throws WSDLException, IOException {
        
        try {
            Port port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), ref);
            List<?> exts = port.getExtensibilityElements();
            if (exts.size() > 0) {                
                ExtensibilityElement el = (ExtensibilityElement)exts.get(0);
                TransportFactory tf = 
                    bus.getTransportFactoryManager().
                        getTransportFactory(el.getElementType().getNamespaceURI());
                return tf.createServerTransport(ref);
            }
        } catch (BusException ex) {
            LOG.severe("TRANSPORT_FACTORY_RETREIVAL_FAILURE_MSG");
        }
        return null;
    }
    protected OutputStreamMessageContext createOutputStreamContext(ServerTransport t,
                                                                   MessageContext bindingContext)
        throws IOException {
        if (bindingContext instanceof SOAPMessageContext) {
            SOAPMessage msg = ((SOAPMessageContext)bindingContext).getMessage();
            try {
                soapBinding.updateHeaders(bindingContext, msg);
            } catch (SOAPException ex) {
                IOException io = new IOException(ex.getMessage());
                io.initCause(ex);
                throw io;
            }
        }
        return t.createOutputStreamContext(bindingContext);            
    }
    protected boolean isFault(ObjectMessageContext objCtx, MessageContext bindingContext) {
        if (bindingContext instanceof SOAPMessageContext) {
            SOAPMessage msg = ((SOAPMessageContext)bindingContext).getMessage();
            try {
                return msg.getSOAPPart().getEnvelope().getBody().hasFault();
            } catch (SOAPException e) {
                return false;
            }
        }
        return super.isFault(objCtx, bindingContext);
    }
    
    protected Method getSEIMethod(List<Class<?>> classList, MessageContext ctx) {

        SOAPMessageContext soapContext = SOAPMessageContext.class.cast(ctx);
        SOAPMessage msg = soapContext.getMessage();
        
        SOAPBinding soapAnnotation = getSOAPBindingAnnotationFromClass(classList);
        
        QName opName = null;
        Method op = null;
        try {
            if (null != soapAnnotation && soapAnnotation.style() == SOAPBinding.Style.RPC) {
                //RPC Style - first child element node of SOAPBody is the operation name
                Node node = NodeUtils.getChildElementNode(msg.getSOAPBody());
                opName = new QName(node.getNamespaceURI(), node.getLocalName());
                op = sbeCallback.getMethod(getEndpoint(), opName);
            } else {
                //DOC Stylle, Literal Use 
                NodeList nl = msg.getSOAPBody().getChildNodes();
                boolean matchFound = false;
                
                for (Class<?> cl : classList) {
                    for (Method m : cl.getMethods()) {
                        SOAPBinding soapAnnotationMethod = soapAnnotation;
                        //SOAPBinding Annotation could be defined per SEI class or 
                        //per SEI Method in Document Style.
                        if (null == soapAnnotationMethod) {
                            soapAnnotationMethod = getSOAPBindingAnnotationFromMethod(m);
                        }

                        if (null != soapAnnotationMethod 
                            && soapAnnotationMethod.parameterStyle() == SOAPBinding.ParameterStyle.BARE) {
                            //BARE ParameterStyle
                            Annotation[][] pa = m.getParameterAnnotations();
                            if (null == pa
                                || pa.length == 0) {
                                continue;
                            }
                            
                            int nodeIdx = 0;
                            for (Annotation[] a : pa) {
                                WebParam param = getWebParamAnnotation(a);
                                if (null == param
                                    || param.mode() == WebParam.Mode.OUT
                                    || nodeIdx >= nl.getLength()) {
                                    break;
                                }
                                Node n = nl.item(nodeIdx);
                                while (n.getNodeType() != Node.ELEMENT_NODE) {
                                    n = nl.item(++nodeIdx);
                                }
                                
                                if (n.getLocalName().equals(param.name())) {
                                    matchFound = true;
                                    ++nodeIdx;
                                } else {
                                    break;
                                }
                            }
                            
                            if (matchFound) {
                                //TODO Check for asyncronouse operation.
                                op = m;
                                break;
                            }
                        } else {
                            //WRAPPED Style
                            Node node = NodeUtils.getChildElementNode(msg.getSOAPBody());
                            RequestWrapper rw = getRequestWrapperAnnotation(m);
                            //Check for the RequestWrapper name followed by 
                            //Method Name (To avoid asyncronous operations)
                            //The method name check can be removed once JSR181 comes up
                            //with annotations for asynchronous operation. JAX-WS spec 2.3.4
                            if (rw != null
                                && rw.localName().equals(node.getLocalName())
                                && rw.targetNamespace().equals(node.getNamespaceURI())
                                && m.getName().equalsIgnoreCase(node.getLocalName())) {
                                op = m;
                                break;
                            }
                            //TODO handle default cases for RequestWrapper Annotation
                        }
                    }
                }
                
            }
        } catch (SOAPException ex) {
            LOG.log(Level.SEVERE, "OPERATION_NAME_RETREIVAL_FAILURE_MSG", ex);
            throw new ProtocolException(ex);
        }
        
        return op;
    }
    
    private SOAPBinding getSOAPBindingAnnotationFromClass(List<Class<?>> classList) {
        SOAPBinding sb = null;
        for (Class<?> c : classList) {
            sb = c.getAnnotation(SOAPBinding.class);
            if (null != sb)  {
                break;
            }
        }
        return sb;
    }

    private SOAPBinding getSOAPBindingAnnotationFromMethod(Method m) {
        SOAPBinding sb = null;
        if (null != m) {
            sb = m.getAnnotation(SOAPBinding.class);
        }
        return sb;
    }
    
    private WebParam getWebParamAnnotation(Annotation[] pa) {
        WebParam wp = null;
        
        if (null != pa) {
            for (Annotation annotation : pa) {
                if (WebParam.class.equals(annotation.annotationType())) {
                    wp = (WebParam) annotation;
                    break;
                }
            }
        }
        return wp;
    }
    
    private RequestWrapper getRequestWrapperAnnotation(Method m) {
        RequestWrapper rw = null;
        
        if (null != m) {
            rw = m.getAnnotation(RequestWrapper.class);
        }
        return rw;        
    }
}
