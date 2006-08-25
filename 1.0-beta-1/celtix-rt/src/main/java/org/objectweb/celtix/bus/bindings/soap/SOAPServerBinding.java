package org.objectweb.celtix.bus.bindings.soap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
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
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bindings.AbstractServerBinding;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.helpers.NodeUtils;
import org.objectweb.celtix.helpers.WSDLHelper;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class SOAPServerBinding extends AbstractServerBinding {
    
    private static final Logger LOG = LogUtils.getL7dLogger(SOAPServerBinding.class);
    
    protected final SOAPBindingImpl soapBinding;
    protected final WSDLHelper helper;
    
    public SOAPServerBinding(Bus b,
                             EndpointReferenceType ref,
                             Endpoint ep,
                             ServerBindingEndpointCallback cbFactory) {
        super(b, ref, ep, cbFactory);
        soapBinding = new SOAPBindingImpl(true);
        helper = new WSDLHelper();
    }
    
    public AbstractBindingImpl getBindingImpl() {
        return soapBinding;
    }
    
    protected Method getSEIMethod(List<Class<?>> classList, MessageContext ctx) {

        SOAPMessageContext soapContext = SOAPMessageContext.class.cast(ctx);
        SOAPMessage msg = soapContext.getMessage();
        
        SOAPBinding soapAnnotation = helper.getBindingAnnotationFromClass(classList);
        
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
                
                NodeList hl = null;
                if (msg.getSOAPHeader() != null) {
                    hl = msg.getSOAPHeader().getChildNodes();
                }
                
                for (Class<?> cl : classList) {
                    for (Method m : cl.getMethods()) {
                        SOAPBinding soapAnnotationMethod = helper.getBindingAnnotationFromMethod(m);
                        //SOAPBinding Annotation could be defined per SEI class or 
                        //per SEI Method in Document Style.
                        if (null == soapAnnotationMethod) {
                            soapAnnotationMethod = soapAnnotation;
                        }

                        if (null != soapAnnotationMethod 
                            && soapAnnotationMethod.parameterStyle() == SOAPBinding.ParameterStyle.BARE) {
                            //BARE ParameterStyle
                            Annotation[][] pa = m.getParameterAnnotations();
                            if (pa.length == 0 && nl.getLength() == 0 
                                && ((hl != null && hl.getLength() == 0) || hl == null)) {
                                return m;
                            }

                            //TODO: Following logic needs to be changed to check the header list also.
                            //
                            
                            if (matchParamsForDocLitBare(pa, nl, hl)) {
                                return m;
                            }

                        } else {
                            //WRAPPED Style
                            Node node = NodeUtils.getChildElementNode(msg.getSOAPBody());
                            // RequestWrapper rw = getRequestWrapperAnnotation(m);
                            RequestWrapper rw = helper.getRequestWrapperAnnotation(m);
                            //Check for the RequestWrapper name followed by 
                            //Method Name (To avoid asyncronous operations)
                            //The method name check can be removed once JSR181 comes up
                            //with annotations for asynchronous operation. JAX-WS spec 2.3.4
                            if (rw != null
                                && rw.localName().equals(node.getLocalName())
                                && rw.targetNamespace().equals(node.getNamespaceURI())
                                && m.getName().equalsIgnoreCase(node.getLocalName())) {
                                return m;
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
    
    public boolean matchParamsForDocLitBare(Annotation[][] pa, NodeList bodyList, NodeList headerList) {
        int nodeIdx = 0;
        boolean matchFound = false;
        NodeList matchingList = bodyList;
        
        for (Annotation[] a : pa) {
            WebParam param = helper.getWebParamAnnotation(a);
            
            if (null != param && param.header()) {
                if (headerList != null) {
                    matchingList = headerList;    
                } else {
                    return matchFound;
                }
            }
            
            if (null == param
                || param.mode() == WebParam.Mode.OUT
                || nodeIdx >= matchingList.getLength()) {
                break;
            }
            
            Node n = matchingList.item(nodeIdx);
            while (n.getNodeType() != Node.ELEMENT_NODE) {
                n = matchingList.item(++nodeIdx);
            }
            
            if (n.getLocalName().equals(param.name()) 
                && n.getNamespaceURI().equals(param.targetNamespace())) {
                matchFound = true;
                ++nodeIdx;
            } else {
                matchFound = false;
                break;
            }
        }

        return matchFound;
    }
    
    public boolean isBindingCompatible(String address) {
        return address.contains("http:");
    }
}
