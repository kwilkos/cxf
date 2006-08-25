package org.objectweb.celtix.bus.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerChainType;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerType;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.configuration.Configuration;

public class HandlerResolverImpl implements HandlerResolver {

    private static final Logger LOG = Logger.getLogger(HandlerResolverImpl.class.getName());
    
    private Map<PortInfo, List<Handler>> handlerMap = new HashMap<PortInfo, List<Handler>>();  
    private Configuration configuration;
    
    public HandlerResolverImpl(Configuration c) {
        configuration = c;
    }
    
    public HandlerResolverImpl() {
        this(null);
    }
    
    public List<Handler> getHandlerChain(PortInfo portInfo) {
        
        List<Handler> handlerChain = handlerMap.get(portInfo);
        if (handlerChain == null) {        
            handlerChain = createHandlerChain(portInfo);
            handlerMap.put(portInfo, handlerChain);
        }
        return handlerChain;
    }
    
    private List<Handler> createHandlerChain(PortInfo portInfo) {
        List<Handler> handlerChain = new ArrayList<Handler>();
        
        if (null == configuration) {
            return handlerChain;
        }
        HandlerChainType hc = (HandlerChainType)configuration.getObject("handlerChain");
        if (null == hc) {
            return handlerChain;
        }
 
        for (HandlerType h : hc.getHandler()) {
            String classname = h.getClassName();
            try {
                Class<? extends Handler> clazz =
                    Class.forName(classname).asSubclass(Handler.class);
                Handler handler = clazz.newInstance();
                
                // use init parameters to configure handler
                
                handlerChain.add(handler);
            } catch (ClassNotFoundException e) {
                throw new WebServiceException(new Message("HANDLER_INSTANTIATION_EXC", 
                                                          LOG).toString(), e);        
            } catch (InstantiationException e) {
                throw new WebServiceException(new Message("HANDLER_INSTANTIATION_EXC", 
                                                          LOG).toString(), e);
            } catch (IllegalAccessException e) {
                throw new WebServiceException(new Message("HANDLER_INSTANTIATION_EXC", 
                                                          LOG).toString(), e);
            }
        }   
        return handlerChain;
    }
}
