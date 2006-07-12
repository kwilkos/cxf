package org.objectweb.celtix.bindings.soap2.jaxws.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPHandler;

import org.objectweb.celtix.bindings.soap2.SoapInterceptor;
import org.objectweb.celtix.jaxws.handlers.AbstractProtocolHandlerInterceptor;
import org.objectweb.celtix.jaxws.handlers.HandlerChainInvoker;

public class SOAPHandlerInterceptor extends AbstractProtocolHandlerInterceptor 
    implements SoapInterceptor {
    
    public SOAPHandlerInterceptor(HandlerChainInvoker invoker) {
        super(invoker);
    }

    public List<QName> getRoles() {
        List<QName> roles = new ArrayList<QName>();
        // TODO
        return roles;
    }
    
    @SuppressWarnings("unchecked")
    public List<QName> getUnderstoodHeaders() {
        List<QName> understood = new ArrayList<QName>();  
        for (Handler h : invoker.getProtocolHandlers()) {
            if (h instanceof SOAPHandler) {
                understood.addAll(((SOAPHandler)h).getHeaders());
            }
        }
        return understood;
    }
    
    
    
    
}
