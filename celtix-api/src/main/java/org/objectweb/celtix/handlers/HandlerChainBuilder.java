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

import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerChainType;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerInitParamType;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerType;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;

public class HandlerChainBuilder {
    static final Logger LOG = LogUtils.getL7dLogger(HandlerChainBuilder.class);

    public List<Handler> buildHandlerChainFromConfiguration(HandlerChainType hc) {
        if (null == hc) {
            return null;
        }
        return sortHandlers(buildHandlerChain(hc));
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

    private ClassLoader getHandlerClassLoader() {
        return getClass().getClassLoader();
    }

    protected List<Handler> buildHandlerChain(HandlerChainType hc) {
        List<Handler> handlerChain = new ArrayList<Handler>();
        for (HandlerType h : hc.getHandler()) {
            try {
                LOG.log(Level.FINE, "loading handler", h.getHandlerName());

                Class<? extends Handler> handlerClass = Class.forName(h.getHandlerClass(), true,
                                                                      getHandlerClassLoader())
                    .asSubclass(Handler.class);

                Handler handler = handlerClass.newInstance();
                LOG.fine("adding handler to chain: " + handler);
                configureHandler(handler, h);
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

    private void configureHandler(Handler handler, HandlerType h) {

        Map<String, String> params = new HashMap<String, String>();

        for (HandlerInitParamType param : h.getInitParam()) {
            params.put(param.getParamName(), param.getParamValue());
        }

        if (params.size() > 0) {
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
}
