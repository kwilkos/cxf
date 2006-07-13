package org.objectweb.celtix.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerChainType;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerInitParamType;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerType;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.injection.ResourceInjector;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.resource.ResourceManager;
import org.objectweb.celtix.resource.ResourceResolver;

public class HandlerChainBuilder {
    static final Logger LOG = LogUtils.getL7dLogger(HandlerChainBuilder.class);

    private Bus bus; 
    
    private boolean handlerInitEnabled; 
   
    private ClassLoader handlerLoader;

    public HandlerChainBuilder(Bus aBus) {
        bus = aBus;
    }
    
    public HandlerChainBuilder() {
        this(null);
    }
    
    public List<Handler> buildHandlerChainFromConfiguration(HandlerChainType hc) {
        if (null == hc) {
            return null;
        }
        return sortHandlers(buildHandlerChain(hc, getHandlerClassLoader()));
    }

    /**
     * sorts the handlers into correct order. All of the logical handlers first
     * followed by the protocol handlers
     * 
     * @param handlers
     * @return sorted list of handlers
     */
    public List<Handler> sortHandlers(List<Handler> handlers) {

        List<LogicalHandler> logicalHandlers = new ArrayList<LogicalHandler>();
        List<Handler> protocolHandlers = new ArrayList<Handler>();

        for (Handler handler : handlers) {
            if (handler instanceof LogicalHandler) {
                logicalHandlers.add((LogicalHandler)handler);
            } else {
                protocolHandlers.add(handler);
            }
        }

        List<Handler> sortedHandlers = new ArrayList<Handler>();
        sortedHandlers.addAll(logicalHandlers);
        sortedHandlers.addAll(protocolHandlers);
        return sortedHandlers;
    }


    public void setHandlerInitEnabled(boolean b) {
        handlerInitEnabled = b;
    }
    
    public boolean isHandlerInitEnabled() {
        return handlerInitEnabled;
    }
    
    //will add container class loader configuration item later
    protected ClassLoader getHandlerClassLoader() {
        if (handlerLoader != null) {
            return handlerLoader;
        } else {
            return getClass().getClassLoader();
        }
    }
    
    public void setHandlerClassLoader(ClassLoader loader) {
        handlerLoader = loader;
    }

    protected List<Handler> buildHandlerChain(HandlerChainType hc, ClassLoader classLoader) {
        List<Handler> handlerChain = new ArrayList<Handler>();
        for (HandlerType ht : hc.getHandler()) {
            try {
                LOG.log(Level.FINE, "loading handler", trimString(ht.getHandlerName()));

                Class<? extends Handler> handlerClass = Class.forName(trimString(ht.getHandlerClass()), true,
                                                                      classLoader).asSubclass(Handler.class);

                Handler handler = handlerClass.newInstance();
                LOG.fine("adding handler to chain: " + handler);
                configureHandler(handler, ht);
                handlerChain.add(handler);
            } catch (Exception e) {
                throw new WebServiceException(new Message("HANDLER_INSTANTIATION_EXC", LOG).toString(), e);
            } 
        }
        return handlerChain;
    }

    private void configureHandler(Handler handler, HandlerType h) {

        if (!handlerInitEnabled) { 
            return;
        }
        
        if (h.getInitParam().size() == 0) {
            return;
        }

        Map<String, String> params = new HashMap<String, String>();
        
        for (HandlerInitParamType param : h.getInitParam()) {
            params.put(trimString(param.getParamName()), trimString(param.getParamValue()));
        }

        Method initMethod = getInitMethod(handler);
        if (initMethod != null) {
            initializeViaInitMethod(handler, params, initMethod);
        } else {
            initializeViaInjection(handler, params);
        }
    }
    
    private void initializeViaInjection(Handler handler, final Map<String, String> params) {

        ResourceManager resMgr = bus.getResourceManager();
        List<ResourceResolver> resolvers = resMgr.getResourceResolvers();
        resolvers.add(new InitParamResourceResolver(params));
        ResourceInjector resInj = new ResourceInjector(resMgr, resolvers);
        resInj.inject(handler);
    }
    
    private void initializeViaInitMethod(Handler handler, Map<String, String> params, Method init) {
        
        try {
            init.invoke(handler, params);
        } catch (InvocationTargetException ex) {
            Throwable t = ex.getCause() != null ? ex.getCause() : ex;
            LogUtils.log(LOG, Level.WARNING, "INIT_METHOD_THREW_EXCEPTION", t, handler.getClass());
        } catch (IllegalAccessException ex) {
            LOG.log(Level.SEVERE, "CANNOT_ACCESS_INIT", handler.getClass());
        }
    }
    
    private Method getInitMethod(Handler handler) {

        Method m = null;
        try {
            m = handler.getClass().getMethod("init", Map.class);
        } catch (NoSuchMethodException ex) {
            // emtpy
        }        
        return m;
    }

    private String trimString(String str) {
        return str != null ? str.trim() : null;
    }
}
