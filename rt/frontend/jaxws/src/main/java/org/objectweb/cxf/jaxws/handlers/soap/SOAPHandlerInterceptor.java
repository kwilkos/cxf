package org.apache.cxf.jaxws.handlers.soap;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;

import org.apache.cxf.bindings.soap2.SoapInterceptor;
import org.apache.cxf.bindings.soap2.SoapMessage;
import org.apache.cxf.jaxws.handlers.AbstractProtocolHandlerInterceptor;
import org.apache.cxf.message.Message;

public class SOAPHandlerInterceptor extends AbstractProtocolHandlerInterceptor<SoapMessage>
    implements SoapInterceptor {
    
    public SOAPHandlerInterceptor(Binding binding) {
        super(binding);
    }

    public Set<URI> getRoles() {
        Set<URI> roles = new HashSet<URI>();
        // TODO
        return roles;
    }
    
    @SuppressWarnings("unchecked")
    public Set<QName> getUnderstoodHeaders() {
        Set<QName> understood = new HashSet<QName>();  
        for (Handler h : getBinding().getHandlerChain()) {
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
