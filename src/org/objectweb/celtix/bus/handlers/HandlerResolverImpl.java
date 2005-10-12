package org.objectweb.celtix.bus.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

public class HandlerResolverImpl implements HandlerResolver {

    private Map<PortInfo, List<Handler>> handlerMap = new HashMap<PortInfo, List<Handler>>();  
    
    public List<Handler> getHandlerChain(PortInfo portInfo) {
        
        List<Handler> handlerChain = handlerMap.get(portInfo);
        if (handlerChain == null) {
            handlerChain = new ArrayList<Handler>();
            handlerMap.put(portInfo, handlerChain);
        }
        return handlerChain;
    }
}
