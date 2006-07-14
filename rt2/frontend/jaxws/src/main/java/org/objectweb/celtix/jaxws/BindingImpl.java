package org.objectweb.celtix.jaxws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;

public class BindingImpl implements Binding {
    private List<Handler> handlerChain;

    
    public List<Handler> getHandlerChain() {
        return new ArrayList<Handler>(handlerChain);
    }

    public void setHandlerChain(List<Handler> hc) {
        handlerChain = hc;
    }
    
    
   
    
}
