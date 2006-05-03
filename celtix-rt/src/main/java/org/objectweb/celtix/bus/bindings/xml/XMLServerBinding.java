package org.objectweb.celtix.bus.bindings.xml;

import java.util.Map;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.AbstractBindingImpl;
import org.objectweb.celtix.bindings.AbstractServerBinding;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.helpers.NodeUtils;
import org.objectweb.celtix.helpers.WSDLHelper;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class XMLServerBinding extends AbstractServerBinding {
    protected final XMLBindingImpl xmlBinding;
    protected final WSDLHelper helper;
    
    public XMLServerBinding(Bus b,
                            EndpointReferenceType ref,
                            ServerBindingEndpointCallback cbFactory) {
        super(b, ref, cbFactory);
        xmlBinding = new XMLBindingImpl(b, ref, true);
        helper = new WSDLHelper();
    }
    
    public AbstractBindingImpl getBindingImpl() {
        return xmlBinding;
    }
    
    public QName getOperationName(MessageContext ctx) {
        XMLMessageContext xmlContext = XMLMessageContext.class.cast(ctx);
        XMLMessage msg = xmlContext.getMessage();
        Map<QName, ? extends DataBindingCallback> ops = sbeCallback.getOperations();
        
        if (sbeCallback.getStyle() == SOAPBinding.Style.RPC) {
            throw new XMLBindingException("Can not handle RPC style in xml binding");
        }
        
        NodeList nl = msg.getRoot().getChildNodes();
        boolean matchFound = false;
        
        for (Map.Entry<QName, ? extends DataBindingCallback> entry : ops.entrySet()) {
            DataBindingCallback callback = entry.getValue();
            if (callback.getSOAPParameterStyle() == SOAPBinding.ParameterStyle.BARE) {
                int nodeIdx = 0;
                if (callback.getParamsLength() != 1) {
                    // If the size of part in message is not ONE,
                    // Then there is a root node as the wrapper.
                    Node node = NodeUtils.getChildElementNode(msg.getRoot());
                    if (callback.getOperationName().equals(node.getLocalName())) {
                        matchFound = true;
                    } else {
                        continue;
                    }
                }
                
                for (int x = 0; x < callback.getParamsLength(); x++) {
                    WebParam param = callback.getWebParam(x);
                    if (param.mode() != WebParam.Mode.OUT) {
                        
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
                }
                if (matchFound) {
                    return entry.getKey();
                }
            } else {
                //WRAPPED Style
                Node node = NodeUtils.getChildElementNode(msg.getRoot());
                QName rw = callback.getRequestWrapperQName();
                //Check for the RequestWrapper name followed by 
                //Method Name (To avoid asyncronous operations)
                //The method name check can be removed once JSR181 comes up
                //with annotations for asynchronous operation. JAX-WS spec 2.3.4
                if (rw != null
                    && rw.getLocalPart().equals(node.getLocalName())
                    && rw.getNamespaceURI().equals(node.getNamespaceURI())
                    && callback.getOperationName().equalsIgnoreCase(node.getLocalName())) {
                    return entry.getKey();
                }
            }
        }
        
        //try to see if we CAN get a callback
        Node node = NodeUtils.getChildElementNode(msg.getRoot());
        QName qn = new QName(node.getNamespaceURI(), node.getNamespaceURI());
        if (sbeCallback.getDataBindingCallback(qn, null,
                                               sbeCallback.getServiceMode()) != null) {
            return qn;
        }

        return null;
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
