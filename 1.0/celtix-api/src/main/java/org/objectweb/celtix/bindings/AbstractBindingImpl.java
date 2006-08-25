package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bus.jaxws.configuration.types.SystemHandlerChainType;
import org.objectweb.celtix.common.injection.ResourceInjector;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.handlers.HandlerChainBuilder;
import org.objectweb.celtix.handlers.HandlerInvoker;
import org.objectweb.celtix.handlers.StreamHandler;

public abstract class AbstractBindingImpl implements Binding {

    private static final String SYSTEM_HANDLER_CHAIN_PROPERTY_NAME = "systemHandlerChain";
    protected List<Handler> handlerChain;
    protected List<Handler> preLogicalSystemHandlers;
    protected List<Handler> postLogicalSystemHandlers;
    protected List<Handler> preProtocolSystemHandlers;
    protected List<Handler> postProtocolSystemHandlers;
    
    
    /* (non-Javadoc)
     * @see javax.xml.ws.Binding#getHandlerChain()
     */
    public List<Handler> getHandlerChain() {
        // TODO Auto-generated method stub
        return handlerChain;
    }
    
    /**
     * Returns the list of configured JAX-WS and system handlers - used by the 
     * abstract client and server binding implementations in processing outgoing and
     * incoming messages.
     * 
     * @param includeSystemHandlers
     * @return
     */
    public List<Handler> getHandlerChain(boolean includeSystemHandlers) {
        if (!includeSystemHandlers) {
            return getHandlerChain();
        }
        List<Handler> allHandlers = new ArrayList<Handler>();
        if (null != preLogicalSystemHandlers) {
            allHandlers.addAll(preLogicalSystemHandlers);
        }
        // jaxws handlers are sorted by logical and system handlers
        List<Handler> userHandlers = handlerChain;
        if (null == userHandlers) {
            userHandlers = new ArrayList<Handler>();
        }
        Iterator<Handler> it = userHandlers.iterator();
        while (it.hasNext()) {
            Handler h = it.next();
            if (h instanceof LogicalHandler) { 
                allHandlers.add(h);
            }
        }
        if (null != postLogicalSystemHandlers) {
            allHandlers.addAll(postLogicalSystemHandlers);
        }
        if (null != preProtocolSystemHandlers) {
            allHandlers.addAll(preProtocolSystemHandlers);
        }
        it = userHandlers.iterator();
        while (it.hasNext()) {
            Handler h = it.next();
            if (!(h instanceof StreamHandler || h instanceof LogicalHandler)) { 
                allHandlers.add(h);
            }
        }
        if (null != postProtocolSystemHandlers) {
            allHandlers.addAll(postProtocolSystemHandlers);
        }
        it = userHandlers.iterator();
        while (it.hasNext()) {
            Handler h = it.next();
            if (h instanceof StreamHandler) { 
                allHandlers.add(h);
            }
        }
        return allHandlers;
    }


    /* (non-Javadoc)
     * @see javax.xml.ws.Binding#setHandlerChain(java.util.List)
     */
    public void setHandlerChain(List<Handler> chain) {
        assert chain != null;
        handlerChain = chain;
    }
    
    
    
    /**
     * Configure the system handlers specified in configuration
     *
     */
    public void configureSystemHandlers(Configuration c) {
        HandlerChainBuilder builder = new HandlerChainBuilder();
        SystemHandlerChainType sh = 
            (SystemHandlerChainType)c.getObject(SYSTEM_HANDLER_CHAIN_PROPERTY_NAME);
        if (null != sh) {
            preLogicalSystemHandlers = builder.buildHandlerChainFromConfiguration(sh.getPreLogical());
            postLogicalSystemHandlers = builder.buildHandlerChainFromConfiguration(sh.getPostLogical());
            preProtocolSystemHandlers = builder.buildHandlerChainFromConfiguration(sh.getPreProtocol());
            postProtocolSystemHandlers = builder.buildHandlerChainFromConfiguration(sh.getPostProtocol());
        }        
    }
    
    /**
     * Returns the list of logical system handlers that should be executed
     * before any user supplied logical handlers. The returned list is NOT a
     * copy and thus can be modified directly.
     * 
     * @return the list of logical system handlers.
     */
    public List<Handler> getPreLogicalSystemHandlers() {
        if (null == preLogicalSystemHandlers) {
            preLogicalSystemHandlers = new ArrayList<Handler>();
        }
        return preLogicalSystemHandlers;
    }
    
    /**
     * Returns the list of logical system handlers that should be
     * executed after any user supplied logical handlers.
     * The returned list is NOT a copy and thus can be modified directly. 
     * @return the list of logical system handlers.
     */
    public List<Handler> getPostLogicalSystemHandlers() {
        if (null == postLogicalSystemHandlers) {
            postLogicalSystemHandlers = new ArrayList<Handler>();
        }
        return postLogicalSystemHandlers;
    }
    
    /**
     * Returns the list of protocol system handlers that should be
     * executed before any user supplied protocol handlers.
     * @return the list of protocol system handlers.
     */
    public List<Handler> getPreProtocolSystemHandlers() {
        if (null == preProtocolSystemHandlers) {
            preProtocolSystemHandlers = new ArrayList<Handler>();
        }
        return preProtocolSystemHandlers;
    }
    
    /**
     * Returns the list of protocl system handlers that should be
     * executed after any user supplied protocl handlers.
     * @return the list of protocol system handlers.
     */
    public List<Handler> getPostProtocolSystemHandlers() {
        if (null == postProtocolSystemHandlers) {
            postProtocolSystemHandlers = new ArrayList<Handler>();
        }
        return postProtocolSystemHandlers;
    }
    
    public void injectSystemHandlers(ResourceInjector injector) {
        if (null != preLogicalSystemHandlers) {
            for (Handler h : preLogicalSystemHandlers) {
                injector.inject(h);
            }
        }
        if (null != postLogicalSystemHandlers) {
            for (Handler h : postLogicalSystemHandlers) {
                injector.inject(h);
            }
        }
        
        if (null != preProtocolSystemHandlers) {
            for (Handler h : preProtocolSystemHandlers) {
                injector.inject(h);
            }
        }
        
        if (null != postProtocolSystemHandlers) {
            for (Handler h : postProtocolSystemHandlers) {
                injector.inject(h);
            }
        }
    }
    
    public abstract MessageContext createBindingMessageContext(MessageContext orig);
    
    public abstract HandlerInvoker createHandlerInvoker();
    
    public abstract void marshal(ObjectMessageContext objContext,
                                    MessageContext msgContext,
                                    DataBindingCallback callback);
    
    public abstract void marshalFault(ObjectMessageContext objContext,
                                    MessageContext msgContext,
                                    DataBindingCallback callback);
    
    public abstract void unmarshal(MessageContext msgContext,
                                      ObjectMessageContext objContext,
                                      DataBindingCallback callback);
    
    public abstract void unmarshalFault(MessageContext msgContext,
                                           ObjectMessageContext objContext,
                                           DataBindingCallback callback);
    
    public abstract void write(MessageContext msgContext, OutputStreamMessageContext outContext) 
        throws IOException;
    
    public abstract void read(InputStreamMessageContext inContext, MessageContext msgContext) 
        throws IOException;
    
    public abstract boolean hasFault(MessageContext msgContext);
    
    public abstract void updateMessageContext(MessageContext msgContext);
    
}
