package org.objectweb.celtix.bus.handlers;




import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.HandlerChain;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;

import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerChainType;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerType;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;

public class HandlerChainBuilder {

    private static final Logger LOG = LogUtils.getL7dLogger(HandlerChainBuilder.class);

    
    /**
     * sorts the handlers into correct order.  All of the logical handlers first
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
    
    public List<Handler> buildHandlerChainFromConfiguration(Configuration configuration, String key) {
        HandlerChainType hc = (HandlerChainType)configuration.getObject(key);
        if (null == hc) {
            return null;
        }
        List<Handler> handlerChain = new ArrayList<Handler>();
        for (HandlerType h : hc.getHandler()) {
            String classname = h.getClassName();
            try {
                Class<? extends Handler> clazz = Class.forName(classname).asSubclass(Handler.class);
                Handler handler = clazz.newInstance();

                // use init parameters to configure handler

                handlerChain.add(handler);
            } catch (ClassNotFoundException e) {
                throw new WebServiceException(new Message("HANDLER_INSTANTIATION_EXC", LOG).toString(), e);
            } catch (InstantiationException e) {
                throw new WebServiceException(new Message("HANDLER_INSTANTIATION_EXC", LOG).toString(), e);
            } catch (IllegalAccessException e) {
                throw new WebServiceException(new Message("HANDLER_INSTANTIATION_EXC", LOG).toString(), e);
            }
        }
        return handlerChain;
    }

    public List<Handler> buildHandlerChainFor(Class<?> clz, List<Handler> existingHandlers) { 

        LOG.fine("building handler chain");
        HandlerChainAnnotation hcAnn = findHandlerChainAnnotation(clz); 
        List<Handler> chain = null;
        if (hcAnn == null) { 
            LOG.fine("no HandlerChain annotation on " + clz); 
            chain = new ArrayList<Handler>();
        } else {
            hcAnn.validate(); 
            
            HandlerChainConfig cfg = getHandlerChainConfig(hcAnn);

            List<HandlerConfig> handlersConfig = cfg.getHandlerConfig(hcAnn.getChainName()); 

            if (null == handlersConfig) {
                throw new WebServiceException("@HandlerChain specified a chain that is not"
                                              + " defined in the specified file");
            } 

            chain =  buildHandlerChain(handlersConfig); 
        }
        assert chain != null;
        if (existingHandlers != null) { 
            chain.addAll(existingHandlers);
        } 
        return sortHandlers(chain);
    } 

    public List<Handler> buildHandlerChainFor(Class<?> clz) { 
        return buildHandlerChainFor(clz, null);
    } 


    private HandlerChainConfig getHandlerChainConfig(HandlerChainAnnotation hcAnn) { 
        try {
            InputStream in = hcAnn.getDeclaringClass().getResourceAsStream(hcAnn.getFileName()); 
        
            if (null == in) {
                throw new WebServiceException("unable to load handler configuration (" 
                                              + hcAnn.getFileName() 
                                              + ") specified by annotation, file nout found");
            } 
        
            LOG.log(Level.INFO, "reading handler chain configuration from " + hcAnn.getFileName());
            return new HandlerChainConfig(in);
        } catch (IOException ex) { 
            throw new WebServiceException(ex);
        } 
    } 


    private List<Handler> buildHandlerChain(List<HandlerConfig> handlersConfig) { 
        
        List<Handler> chain = new ArrayList<Handler>(); 

        for (HandlerConfig hc : handlersConfig) { 
            try { 
                LOG.log(Level.FINE, "loading handler", hc.getName());

                Class<? extends Handler> handlerClass = 
                    Class.forName(hc.getClassName(), true, 
                                  getHandlerClassLoader()).asSubclass(Handler.class);
                        
                Handler h = handlerClass.newInstance(); 
                LOG.fine("adding handler to chain: " + h); 
                configureHandler(h, hc); 
                chain.add(h); 

            } catch (ClassNotFoundException ex) { 
                throw new WebServiceException("unable to load handler class [" + hc.getClassName() + "]", ex);
            } catch (InstantiationException ex) { 
                throw new WebServiceException("unable to instantiate handler", ex);
            } catch (IllegalAccessException ex) { 
                throw new WebServiceException("handler does not have a public no args constructor");
            }

        } 
        return chain;
    } 

    private ClassLoader getHandlerClassLoader() {
        return getClass().getClassLoader(); 
    }

    private void configureHandler(Handler handler, HandlerConfig config) { 
        
        Map<String, String> params = config.getInitParams(); 

        try { 
            Method init = handler.getClass().getMethod("init", Map.class);
            init.invoke(handler, params); 
        } catch (InvocationTargetException ex) {
            Throwable t = ex.getCause() != null ? ex.getCause() : ex;
            LogUtils.log(LOG, Level.WARNING, "INIT_METHOD_THREW_EXCEPTION", t, handler.getClass());
        } catch (NoSuchMethodException ex) {
            LOG.log(Level.SEVERE, "NO_INIT_METHOD_ON_HANDLER", handler.getClass()); 
        } catch (IllegalAccessException ex) {
            LOG.log(Level.SEVERE, "CANNOT_ACCESS_INIT", handler.getClass()); 
        } 
    } 


    private HandlerChainAnnotation findHandlerChainAnnotation(Class<?> clz) { 

        HandlerChain ann = clz.getAnnotation(HandlerChain.class); 
        Class<?> declaringClass = clz; 

        if (ann == null) { 
            for (Class<?> iface : clz.getInterfaces()) { 
                if (LOG.isLoggable(Level.FINE)) { 
                    LOG.fine("checking for HandlerChain annoation on " + iface.getName());
                }
                ann = iface.getAnnotation(HandlerChain.class);
                if (ann != null) { 
                    declaringClass = iface;
                    break;
                }
            }
        } 
        if (ann != null) { 
            return new HandlerChainAnnotation(ann, declaringClass);
        } else { 
            return null;
        }
    } 

    private static class HandlerChainAnnotation { 
        private final Class<?> declaringClass; 
        private final HandlerChain ann; 
        
        HandlerChainAnnotation(HandlerChain hc, Class<?> clz) { 
            ann = hc; 
            declaringClass = clz;
        } 

        public Class<?> getDeclaringClass() { 
            return declaringClass; 
        } 

        public String getFileName() { 
            return ann.file(); 
        } 

        public String getChainName() { 
            return ann.name(); 
        }
        
        public void validate() { 
            if (null == ann.file() || "".equals(ann.file())) {
                throw new WebServiceException("@HandlerChain annotation does not contain a file name or url");
            } 
            if (null == ann.name() || "".equals(ann.name())) {
                LOG.fine("no handler name specified, defaulting to first declared");
            } 
        } 

        public String toString() { 
            return "[" + declaringClass + "," + ann + "]";
        }
    } 
}
