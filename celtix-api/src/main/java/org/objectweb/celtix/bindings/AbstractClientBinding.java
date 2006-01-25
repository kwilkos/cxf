package org.objectweb.celtix.bindings;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.logging.Logger;


import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;

import org.objectweb.celtix.buslifecycle.BusLifeCycleListener;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_TO_ADDRESS_PROPERTY;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_WSDL_PORT_PROPERTY;


public abstract class AbstractClientBinding implements ClientBinding {
    private static final Logger LOG = LogUtils.getL7dLogger(AbstractClientBinding.class);

    protected final Bus bus;
    protected final EndpointReferenceType reference;
    protected Port port;
    protected ClientTransport transport;
    
    public AbstractClientBinding(Bus b, EndpointReferenceType ref) throws WSDLException, IOException {
        bus = b;
        bus.getLifeCycleManager().registerLifeCycleListener(new ShutdownListener(this));
        reference = ref;
        transport = null;
    }
    
    private static class ShutdownListener 
        extends WeakReference<AbstractClientBinding> 
        implements BusLifeCycleListener {
        
        ShutdownListener(AbstractClientBinding c) {
            super(c);
        }

        public void initComplete() {
            //nothing
        }

        public void preShutdown() {
            if (get() != null) {
                get().shutdown();
                clear();
            }
        }

        public void postShutdown() {
            //nothing
        }
    }
    
    // --- BindingBase interface ---
    
    public Binding getBinding() {
        return getBindingImpl();
    }
    
    public ObjectMessageContext createObjectContext() {
        return new ObjectMessageContextImpl();
    }
    
    public HandlerInvoker createHandlerInvoker() {
        return getBindingImpl().createHandlerInvoker(); 
    }
        
    public void configureSystemHandlers(Configuration portConfiguration) {
        /*
            Configuration busConfiguration = bus.getConfiguration();
            Configuration serviceConfiguration = busConfiguration
                .getChild("http://celtix.objectweb.org/bus/jaxws/service-config",
                          EndpointReferenceUtils.getServiceName(reference));
            Configuration portConfiguration = serviceConfiguration
                .getChild("http://celtix.objectweb.org/bus/jaxws/port-config",
                          EndpointReferenceUtils.getPortName(reference));
            */
        getBindingImpl().configureSystemHandlers(portConfiguration);
    }
    
    //  --- BindingBase interface ---
    
    // --- Methods to be implemented by concrete client bindings ---
    
    protected abstract AbstractBindingImpl getBindingImpl();
    
    protected abstract void marshal(ObjectMessageContext objContext,
                                    MessageContext context,
                                    DataBindingCallback callback);
    
    protected abstract void unmarshal(MessageContext context,
                                      ObjectMessageContext objContext,
                                      DataBindingCallback callback);
    
    protected abstract void unmarshalFault(MessageContext context,
                                           ObjectMessageContext objContext,
                                           DataBindingCallback callback);
    
    
    protected abstract boolean hasFault(MessageContext context);

    protected abstract void write(MessageContext context, OutputStreamMessageContext outCtx);

    protected abstract void read(InputStreamMessageContext inCtx, MessageContext context);
    
    // --- Methods to be implemented by concrete client bindings --- 
    
    // --- ClientBinding interface ---
    
    public ObjectMessageContext invoke(ObjectMessageContext context,
                                       DataBindingCallback callback)
        throws IOException {

        HandlerInvoker handlerInvoker = createHandlerInvoker(); 
        handlerInvoker.setContext(context); 

        try { 
            MessageContext bindingContext = getBindingImpl().createBindingMessageContext(context);

            try {
                getTransport();
            } catch (WSDLException e) {
                throw (IOException)(new IOException(e.getMessage()).initCause(e));
            }
            assert transport != null : "transport is null";

            // cache To EPR & WSDL Port in context for use by WS-Addressing 
            // handlers
            storeAddress(context);
            context.put(OutputStreamMessageContext.ONEWAY_MESSAGE_TF, false);
        
            //Input Message For Client
            context.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);
            bindingContext.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);

            boolean continueProcessing = handlerInvoker.invokeLogicalHandlers(true);

            if (continueProcessing) {  

                if (null == bindingContext) {
                    bindingContext = context;
                } else {
                    marshal(context, bindingContext, callback);
                }

                continueProcessing = handlerInvoker.invokeProtocolHandlers(true, bindingContext); 

                if (continueProcessing) {
                    OutputStreamMessageContext ostreamContext = 
                        createOutputStreamContext(bindingContext);
                    
                    handlerInvoker.invokeStreamHandlers(ostreamContext); 

                    finalPrepareOutputStreamContext(bindingContext, ostreamContext);
                    
                    write(bindingContext, ostreamContext);
                    
                    InputStreamMessageContext ins = transport.invoke(ostreamContext);
                    assert ins != null;

                    context.putAll(ins); 
                    bindingContext = getBindingImpl().createBindingMessageContext(context);
                    if (null == bindingContext) {
                        bindingContext = ins;
                    }

                    //Output Message For Client
                    bindingContext.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);   
                    handlerInvoker.setInbound();
                    handlerInvoker.setFault(ins.isFault()); 
                    handlerInvoker.invokeStreamHandlers(ins); 
                    
                    read(ins, bindingContext);
                    
                    
                } else {
                    //Output Message For Client
                    bindingContext.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);   
                }

                handlerInvoker.invokeProtocolHandlers(true, bindingContext);

                if (!hasFault(bindingContext)) {
                    unmarshal(bindingContext, context, callback);
                } else {
                    unmarshalFault(bindingContext, context, callback);
                }
            }
            
            context.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);
            handlerInvoker.invokeLogicalHandlers(true);
        } finally { 
            handlerInvoker.mepComplete();
        }        
        return context;
    }
    
    public void invokeOneWay(ObjectMessageContext context,
                             DataBindingCallback callback) throws IOException {
        
        HandlerInvoker handlerInvoker = createHandlerInvoker(); 
        handlerInvoker.setContext(context); 
        
        try { 
            MessageContext bindingContext = getBindingImpl().createBindingMessageContext(context);

            //Input Message For Client
            bindingContext.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);

            try {
                getTransport();
            } catch (WSDLException e) {
                throw (IOException)(new IOException(e.getMessage()).initCause(e));
            }
            assert transport != null : "transport is null";

            // cache To EPR & WSDL Port in context for use by WS-Addressing 
            // handlers
            storeAddress(context);
            context.put(OutputStreamMessageContext.ONEWAY_MESSAGE_TF, true);

            boolean continueProcessing = handlerInvoker.invokeLogicalHandlers(true);

            if (continueProcessing) {  

                if (null != bindingContext) {
                    marshal(context, bindingContext, callback);
                } else {
                    bindingContext = context;
                }

                continueProcessing = handlerInvoker.invokeProtocolHandlers(true, bindingContext);

                if (continueProcessing) { 
                    OutputStreamMessageContext ostreamContext = 
                        createOutputStreamContext(bindingContext);

                    continueProcessing = handlerInvoker.invokeStreamHandlers(ostreamContext); 
                    if (continueProcessing) { 
                        finalPrepareOutputStreamContext(bindingContext, ostreamContext);

                        write(bindingContext, ostreamContext);
                        transport.invokeOneway(ostreamContext);
                    } 
                }
            }
        } finally { 
            handlerInvoker.mepComplete();
        }
    }

    public Future<ObjectMessageContext> invokeAsync(ObjectMessageContext context,
                                                    DataBindingCallback callback,
                                                    Executor executor) throws IOException {
        
        LOG.info("AbstractClientBinding: invokeAsync");
        HandlerInvoker handlerInvoker = createHandlerInvoker();
        handlerInvoker.setContext(context);
        AsyncFuture asyncFuture = null;

        try { 
            MessageContext bindingContext = getBindingImpl().createBindingMessageContext(context);
            

            //Input Message For Client
            context.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);
            bindingContext.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);
            boolean continueProcessing = handlerInvoker.invokeLogicalHandlers(true);

            if (continueProcessing) {  

                if (null == bindingContext) {
                    bindingContext = context;
                } else {
                    marshal(context, bindingContext, callback);
                }

                continueProcessing = handlerInvoker.invokeProtocolHandlers(true, bindingContext); 

                if (continueProcessing) {
                    try {
                        getTransport();
                    } catch (WSDLException e) {
                        throw (IOException)(new IOException(e.getMessage()).initCause(e));
                    }
                    assert transport != null : "transport is null";
                    
                    OutputStreamMessageContext ostreamContext = 
                        createOutputStreamContext(bindingContext);
                    ostreamContext.setOneWay(false);
                    
                    handlerInvoker.invokeStreamHandlers(ostreamContext);
                    
                    finalPrepareOutputStreamContext(bindingContext, ostreamContext);
                    
                    write(bindingContext, ostreamContext);
                    
                    Future<InputStreamMessageContext> ins = transport.invokeAsync(ostreamContext, executor);
                    asyncFuture = new AsyncFuture(ins, this, callback, handlerInvoker, context);         
                }
            }
        } finally {
            handlerInvoker.mepComplete();

        }   
        return (Future<ObjectMessageContext>)asyncFuture;
                    
    }
    
    // --- ClientBinding interface ---
    
    // --- helpers ---
    
    protected void shutdown() {
        if (transport != null) {
            transport.shutdown();
            transport = null;
        }
    }
    
    protected synchronized ClientTransport getTransport() throws WSDLException, IOException {
        if (transport == null) {
            transport = createTransport(reference);
        }
        return transport;
    } 
    
    protected ClientTransport createTransport(EndpointReferenceType ref) throws WSDLException, IOException {
        ClientTransport ret = null;
        try {
            LOG.info("creating client transport for " + ref);
          
            port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), ref);
            List<?> exts = port.getExtensibilityElements();
            if (exts.size() > 0) {
                ExtensibilityElement el = (ExtensibilityElement)exts.get(0);
                
                TransportFactory factory = bus.getTransportFactoryManager().
                        getTransportFactory(el.getElementType().getNamespaceURI()); 
                ret = factory.createClientTransport(ref);
            }
        } catch (BusException ex) {
            LOG.severe("TRANSPORT_FACTORY_RETREIVAL_FAILURE_MSG");
        }
        assert ret != null; 
        return ret;
    }

    protected void storeAddress(MessageContext context) {
        context.put(CLIENT_TO_ADDRESS_PROPERTY, reference);
        context.setScope(CLIENT_TO_ADDRESS_PROPERTY,
                         MessageContext.Scope.HANDLER);
        context.put(CLIENT_WSDL_PORT_PROPERTY, port);
        context.setScope(CLIENT_WSDL_PORT_PROPERTY,
                         MessageContext.Scope.HANDLER);
    }


    
    protected OutputStreamMessageContext createOutputStreamContext(MessageContext bindingContext)
        throws IOException {
        return transport.createOutputStreamContext(bindingContext);            
    }
    protected void finalPrepareOutputStreamContext(MessageContext bindingContext,
                                                   OutputStreamMessageContext ostreamContext) 
        throws IOException {
        transport.finalPrepareOutputStreamContext(ostreamContext);
    }
    

    
    public ObjectMessageContext getObjectMessageContextAsync(InputStreamMessageContext ins, 
                                                             HandlerInvoker handlerInvoker, 
                                                             DataBindingCallback callback, 
                                                             ObjectMessageContext context) {     
        
        context.putAll(ins); 
        
        try {
            MessageContext bindingContext = getBindingImpl().createBindingMessageContext(context);
            if (null == bindingContext) {
                bindingContext = ins;
            }
            //Output Message For Client
            bindingContext.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);   
            handlerInvoker.setInbound();
            handlerInvoker.setFault(ins.isFault()); 
            handlerInvoker.invokeStreamHandlers(ins); 

            read(ins, bindingContext);
    
            handlerInvoker.invokeProtocolHandlers(true, bindingContext);
    
            if (!hasFault(bindingContext)) {
                unmarshal(bindingContext, context, callback);
            } else {
                unmarshalFault(bindingContext, context, callback);
            }
            context.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);
            
        } finally { 
            handlerInvoker.mepComplete();
        }
        
        return context;
    }

}
