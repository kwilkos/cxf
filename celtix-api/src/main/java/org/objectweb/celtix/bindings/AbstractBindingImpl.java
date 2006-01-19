package org.objectweb.celtix.bindings;

import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.handlers.HandlerInvoker;

public abstract class AbstractBindingImpl implements Binding {

    protected List<Handler> handlerChain; 
    
    /* (non-Javadoc)
     * @see javax.xml.ws.Binding#getHandlerChain()
     */
    public List<Handler> getHandlerChain() {
        // TODO Auto-generated method stub
        return handlerChain;
    }


    /* (non-Javadoc)
     * @see javax.xml.ws.Binding#setHandlerChain(java.util.List)
     */
    public void setHandlerChain(List<Handler> chain) {
        assert chain != null;
        handlerChain = chain;
    }
    
    protected abstract MessageContext createBindingMessageContext(MessageContext orig);
    
    protected abstract HandlerInvoker createHandlerInvoker();

}
