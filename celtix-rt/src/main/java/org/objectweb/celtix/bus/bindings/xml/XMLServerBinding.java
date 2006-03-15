package org.objectweb.celtix.bus.bindings.xml;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bindings.AbstractServerBinding;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.helpers.NodeUtils;
import org.objectweb.celtix.helpers.WSDLHelper;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class XMLServerBinding extends AbstractServerBinding {
    private static final Logger LOG = LogUtils.getL7dLogger(XMLServerBinding.class);
    protected final XMLBindingImpl xmlBinding;
    protected final WSDLHelper helper;
    
    public XMLServerBinding(Bus b,
                            EndpointReferenceType ref,
                            Endpoint ep,
                            ServerBindingEndpointCallback cbFactory) {
        super(b, ref, ep, cbFactory);
        xmlBinding = new XMLBindingImpl(b, ref, true);
        helper = new WSDLHelper();
    }
    
    public AbstractBindingImpl getBindingImpl() {
        return xmlBinding;
    }

    protected Method getSEIMethod(List<Class<?>> classList, MessageContext ctx) {
        XMLMessageContext xmlContext = XMLMessageContext.class.cast(ctx);
        XMLMessage msg = xmlContext.getMessage();
        SOAPBinding annotation = helper.getBindingAnnotationFromClass(classList);
        
        QName opName = null;
        Method op = null;
        try {
            if (null != annotation && annotation.style() == SOAPBinding.Style.RPC) {
                throw new XMLBindingException("Can not handle RPC style in xml binding");
            } else {
                NodeList nl = msg.getRoot().getChildNodes();
                boolean matchFound = false;
                
                for (Class<?> cl : classList) {
                    if (!cl.isInterface()) {
                        continue;
                    }
                    for (Method m : cl.getMethods()) {
                        SOAPBinding annotationMethod = annotation;
                        //SOAPBinding Annotation could be defined per SEI class or 
                        //per SEI Method in Document Style.
                        if (null == annotationMethod) {
                            annotationMethod = helper.getBindingAnnotationFromMethod(m);
                        }
                        
                        if (null != annotationMethod 
                            && annotationMethod.parameterStyle() == SOAPBinding.ParameterStyle.BARE) {
                            // BARE ParameterStyle
                            // If the size of part in message is not ONE,
                            // Then there is a root node as the wrapper.
                            
                            Annotation[][] pa = m.getParameterAnnotations();
                            if (null == pa || pa.length != 1) {
                                Node node = NodeUtils.getChildElementNode(msg.getRoot());
                                opName = new QName(node.getNamespaceURI(), node.getLocalName());
                                if (m.getName().equalsIgnoreCase(node.getLocalName())) {
                                    op = sbeCallback.getMethod(getEndpoint(), opName);
                                    matchFound = true;
                                    break;
                                } else {
                                    continue;
                                }
                            }
                            
                            int nodeIdx = 0;
                            for (Annotation[] a : pa) {
                                WebParam param = helper.getWebParamAnnotation(a);
                                if (null == param
                                    || param.mode() == WebParam.Mode.OUT
                                    || nodeIdx >= nl.getLength()) {
                                    break;
                                }
                                Node n = nl.item(nodeIdx);
                                while (n.getNodeType() != Node.ELEMENT_NODE) {
                                    n = nl.item(++nodeIdx);
                                }

                                if (isMethodMatch(n, param)) {
                                    matchFound = true;
                                    ++nodeIdx;
                                } else {
                                    matchFound = false;
                                    break;
                                }
                            }
                            
                            if (matchFound) {
                                op = m;
                                break;
                            }
                        } else {
                            //WRAPPED Style
                            Node node = NodeUtils.getChildElementNode(msg.getRoot());
                            RequestWrapper rw = helper.getRequestWrapperAnnotation(m);
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
        } catch (XMLBindingException ex) {
            ex.printStackTrace();
            LOG.log(Level.SEVERE, "OPERATION_NAME_RETREIVAL_FAILURE_MSG", ex);
            throw new ProtocolException(ex);
        }
        LOG.log(Level.INFO, "OPERATION_NAME_RETREIVAL", op);
        return op;
    }

    private boolean isMethodMatch(Node node, WebParam param) {
        boolean found = false;
        String nNS = node.getNamespaceURI();
        if (nNS != null) {
            if (param.name().equals(node.getLocalName()) && nNS.equals(param.targetNamespace())) {
                found = true;
            }
        } else if (param.name().equals(node.getLocalName())) {
            found = true;
        }
        return found;
    }

    public boolean isBindingCompatible(String address) {
        // TODO Auto-generated method stub
        return false;
    }
}
