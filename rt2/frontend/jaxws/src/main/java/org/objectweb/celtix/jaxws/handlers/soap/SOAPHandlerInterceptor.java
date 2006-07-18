package org.objectweb.celtix.jaxws.handlers.soap;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;

import org.objectweb.celtix.bindings.soap2.SoapInterceptor;
import org.objectweb.celtix.jaxws.handlers.AbstractProtocolHandlerInterceptor;
import org.objectweb.celtix.jaxws.handlers.HandlerChainInvoker;
import org.objectweb.celtix.message.Message;

public class SOAPHandlerInterceptor extends AbstractProtocolHandlerInterceptor 
    implements SoapInterceptor {
    
    public SOAPHandlerInterceptor(HandlerChainInvoker invoker) {
        super(invoker);
    }

    public List<URI> getRoles() {
        List<URI> roles = new ArrayList<URI>();
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

    @Override
    protected MessageContext createProtocolMessageContext(Message message) {
        return new SOAPMessageContextImpl(message);
    }
    
    
    
    
    
    
}
