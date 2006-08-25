package org.apache.cxf.jaxws.binding;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;

public class BindingImpl implements Binding {
    private List<Handler> handlerChain;
    
    public List<Handler> getHandlerChain() {
        if (handlerChain == null) {
            return new ArrayList<Handler>();
        }
        return new ArrayList<Handler>(handlerChain);
    }

    public void setHandlerChain(List<Handler> hc) {
        handlerChain = hc;
    }   
}
