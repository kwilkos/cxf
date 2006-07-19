package org.objectweb.celtix.jaxws.handlers.soap;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;

import org.objectweb.celtix.bindings.soap2.SoapInterceptor;
import org.objectweb.celtix.bindings.soap2.SoapMessage;
import org.objectweb.celtix.jaxws.handlers.AbstractProtocolHandlerInterceptor;
import org.objectweb.celtix.jaxws.handlers.HandlerChainInvoker;
import org.objectweb.celtix.message.Message;

public class SOAPHandlerInterceptor extends AbstractProtocolHandlerInterceptor<SoapMessage>
    implements SoapInterceptor {
    
    public SOAPHandlerInterceptor(HandlerChainInvoker invoker) {
        super(invoker);
    }

    public Set<URI> getRoles() {
        Set<URI> roles = new HashSet<URI>();
        // TODO
        return roles;
    }
    
    @SuppressWarnings("unchecked")
    public Set<QName> getUnderstoodHeaders() {
        Set<QName> understood = new HashSet<QName>();  
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

    public void handleFault(SoapMessage message) {
        // TODO Auto-generated method stub
        
    }

}
