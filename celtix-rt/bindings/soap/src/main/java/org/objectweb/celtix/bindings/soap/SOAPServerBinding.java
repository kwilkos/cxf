package org.objectweb.celtix.bindings.soap;

import java.util.Map;

import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

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

public class SOAPServerBinding extends AbstractServerBinding {
    
    protected final SOAPBindingImpl soapBinding;
    protected final WSDLHelper helper;
    
    public SOAPServerBinding(Bus b,
                             EndpointReferenceType ref,
                             ServerBindingEndpointCallback cbFactory) {
        super(b, ref, cbFactory);
        soapBinding = new SOAPBindingImpl(true);
        helper = new WSDLHelper();
    }
    
    public AbstractBindingImpl getBindingImpl() {
        return soapBinding;
    }
    public QName getOperationName(MessageContext ctx) {
        SOAPMessageContext soapContext = SOAPMessageContext.class.cast(ctx);
        SOAPMessage msg = soapContext.getMessage();
        Map<QName, ? extends DataBindingCallback> ops = sbeCallback.getOperations();


        
        //attempt the simple case first....
        Node node = null;
        try {
            node = NodeUtils.getChildElementNode(msg.getSOAPBody());
        } catch (SOAPException e) {
            return null;
        }
        QName firstNodeName = null;
        if (node != null) {
            firstNodeName = new QName(node.getNamespaceURI(), node.getLocalName());
            if (ops.containsKey(firstNodeName)) {
                DataBindingCallback cb = ops.get(firstNodeName);
                if (cb.getSOAPStyle() == SOAPBinding.Style.RPC) {
                    return firstNodeName;
                }
            }
        }

        for (Map.Entry<QName, ? extends DataBindingCallback> entry : ops.entrySet()) {
            DataBindingCallback cb = entry.getValue();
            if (cb.getSOAPStyle() == SOAPBinding.Style.RPC) {
                //RPC ones should already have been found
                continue;
            }
            if (cb.getSOAPParameterStyle() == SOAPBinding.ParameterStyle.BARE) {
                //unwrapped
                try {
                    NodeList nl = msg.getSOAPBody().getChildNodes();
                    NodeList hl = null;
                    if (msg.getSOAPHeader() != null) {
                        hl = msg.getSOAPHeader().getChildNodes();
                    }
                    if (matchParamsForDocLitBare(cb, nl, hl)) {
                        return entry.getKey();
                    }
                } catch (SOAPException e) {
                    //ignore?
                }
            } else {
                //wrapped
                if (firstNodeName != null 
                    && firstNodeName.equals(cb.getRequestWrapperQName())) {
                    return entry.getKey();
                }
            }
        }
        if (firstNodeName != null 
            && ops.containsKey(firstNodeName)) {
            return firstNodeName;
        }
        //try to see if we CAN get a callback
        if (sbeCallback.getDataBindingCallback(firstNodeName, null,
                                               sbeCallback.getServiceMode()) != null) {
            return firstNodeName;
        }
        
        throw new WebServiceException("No operation matching " + firstNodeName + " was found");
    }
    
    public boolean matchParamsForDocLitBare(DataBindingCallback cb,
                                            NodeList bodyList,
                                            NodeList headerList) {
        if (cb.getParamsLength() == 0
            && (bodyList == null || bodyList.getLength() == 0)
            && (headerList == null || headerList.getLength() == 0)) {
            return true;
        }
        int nodeIdx = 0;
        boolean matchFound = false;
        NodeList matchingList = bodyList;
        for (int x = 0; x < cb.getParamsLength(); x++) {
            WebParam param = cb.getWebParam(x);
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
