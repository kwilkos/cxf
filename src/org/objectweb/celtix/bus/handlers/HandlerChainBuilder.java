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
import org.objectweb.celtix.common.logging.LogUtils;

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

    public List<Handler> buildHandlerChainFor(Object obj) { 

        LOG.fine("building handler chain");

        try {
            HandlerChain hcAnn = obj.getClass().getAnnotation(HandlerChain.class); 
            if (hcAnn == null) { 
                LOG.fine("no HandlerChain annotation on " + obj.getClass()); 
                return new ArrayList<Handler>();
            } 
        
            String fileName = hcAnn.file(); 
            String chainName = hcAnn.name();

            if (null == fileName || "".equals(fileName)) {
                throw new WebServiceException("@HandlerChain annotation does not contain a file name or url");
            } 

            if (null == chainName || "".equals(chainName)) {
                throw new WebServiceException("@HandlerChain annotation does not contain a chain name");
            } 

            InputStream in = obj.getClass().getResourceAsStream(fileName); 
            
            if (null == in) {
                throw new WebServiceException("unable to load handler configuration (" 
                                              + fileName + ") specified by annotation");
            } 

            HandlerChainConfig cfg = new HandlerChainConfig(in);
            LOG.log(Level.INFO, "reading handler chain configuration from " + fileName);

            List<HandlerConfig> handlersConfig = cfg.getHandlerConfig(chainName); 

            if (null == handlersConfig) {
                throw new WebServiceException("@HandlerChain specified a chain that is not"
                                              + " defined in the specified file");
            } 

            return buildHandlerChain(handlersConfig); 

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
}
