package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.common.injection.ResourceInjector;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.resource.DefaultResourceManager;
import org.objectweb.celtix.resource.ResourceManager;
import org.objectweb.celtix.resource.ResourceResolver;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

import static org.objectweb.celtix.bindings.JAXWSConstants.BUS_PROPERTY;
import static org.objectweb.celtix.bindings.JAXWSConstants.SERVER_BINDING_PROPERTY;
import static org.objectweb.celtix.bindings.JAXWSConstants.SERVER_TRANSPORT_PROPERTY;

public abstract class AbstractServerBinding extends AbstractBindingBase implements ServerBinding {

    private static final Logger LOG = LogUtils.getL7dLogger(AbstractServerBinding.class);

    protected ServerBindingEndpointCallback sbeCallback;

    public AbstractServerBinding(Bus b, EndpointReferenceType ref,
                                 ServerBindingEndpointCallback sbcb) {
        super(b, ref);
        sbeCallback = sbcb;
    }

    // --- ServerBinding interface ---

    public void activate() throws WSDLException, IOException {
        transport = createTransport(reference);

        ServerTransportCallback tc = new ServerTransportCallback() {

            public void dispatch(InputStreamMessageContext ctx, ServerTransport t) {
                AbstractServerBinding.this.dispatch(ctx, t);
            }

            public Executor getExecutor() {
                return sbeCallback.getExecutor();
            }
        };
        serverTransport().activate(tc);
        
        injectSystemHandlers();
    }

    public void deactivate() throws IOException {
        serverTransport().deactivate();
    }

    /**
     * Make an initial partial response to an incoming request. The partial
     * response may only contain 'header' information, and not a 'body'.
     * 
     * @param outputContext object message context
     * @param callback callback for data binding
     */
    public void partialResponse(OutputStreamMessageContext outputContext, 
                                DataBindingCallback callback) throws IOException {
        ObjectMessageContext objectMessageContext = createObjectContext();
        objectMessageContext.putAll(outputContext);
        BindingContextUtils.storeDataBindingCallback(objectMessageContext, callback);

        if (callback != null) {
            Request request = new Request(this, transport, objectMessageContext);
            request.setOneway(true);

            try {
                request.process(outputContext);
                terminateOutputContext(outputContext);
            } finally {
                request.complete();
            }
        } else {
            transport.finalPrepareOutputStreamContext(outputContext);
            terminateOutputContext(outputContext);
        }
    }
    
    // --- ServerBinding interface ---

    // --- Methods to be implemented by concrete server bindings ---

    public abstract AbstractBindingImpl getBindingImpl();
    
    public abstract QName getOperationName(MessageContext ctx);
    

    // --- Methods to be implemented by concrete server bindings ---

    protected void finalPrepareOutputStreamContext(ServerTransport t, MessageContext bindingContext,
                                                   OutputStreamMessageContext ostreamContext)
        throws IOException {
        t.finalPrepareOutputStreamContext(ostreamContext);
    }

    protected boolean isFault(ObjectMessageContext objCtx, MessageContext bindingCtx) {
        if (getBindingImpl().hasFault(bindingCtx)) {
            return true;
        }
        return objCtx.getException() != null;
    }
    
    protected void dispatch(InputStreamMessageContext istreamCtx, final ServerTransport t) {
        LOG.info("Dispatched to binding on thread : " + Thread.currentThread());
        // storeSource(istreamCtx, t);
        BindingContextUtils.storeServerBindingEndpointCallback(istreamCtx, sbeCallback);
        
        final ServerRequest inMsg = new ServerRequest(this, istreamCtx);         
        
        Exception inboundException = null;
        
        try {
            inMsg.processInbound();   
            if (!inMsg.doDispatch()) {
                LOG.log(Level.INFO, 
                        "handlers have halted inbound message processing or specifically prevent dispatch");
            }
        } catch (Exception ex) {
            inboundException = ex;
            LOG.log(Level.INFO, "inbound message processing resulted in exception: ", ex);
        } 
        
        // if an error occured during processing of the inbound request
        // or if the processing direction was halted by one of the handlers
        // or if this is a one-way operation: send response (but traverse
        // system handlers only if operation is one-way).
        
        boolean doDispatch = null == inboundException && inMsg.doDispatch();
        
        if (!doDispatch || inMsg.isOneway()) {
     
            inMsg.processOutbound(t, inboundException); 
            
            if (!doDispatch) {
                return;
            }
        }
   
        // everything was OK: dispatch to implementor
        
        Runnable invoker = new Runnable() {
            public void run() {
                LOG.log(Level.INFO, "Before invoking on implementor");
                assert null != inMsg.getObjectCtx();
                inMsg.doInvocation();
                LOG.log(Level.INFO, "After invoking on implementor");
                if (!inMsg.isOneway()) {
                    // process response 
                    inMsg.processOutbound(t, null);
                }
            }
        };

        // the dispatch must be async if the request is decoupled or oneway and the
        // transport is unable to proceed to the next request until this thread
        // is freed up
        if ((BindingContextUtils.retrieveDecoupledResponse(inMsg.getObjectCtx())
             || inMsg.isOneway()) 
            && BindingContextUtils.retrieveAsyncOnewayDispatch(istreamCtx)) {
            // invoke implementor asynchronously
            executeAsync(invoker);
        } else {
            // invoke implementor directly
            invoker.run();
        }
    }
    
    protected ServerTransport createTransport(EndpointReferenceType ref) throws WSDLException, IOException {
        
        try {
            Port port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), ref);
            List<?> exts = port.getExtensibilityElements();
            if (exts.size() > 0) {                
                ExtensibilityElement el = (ExtensibilityElement)exts.get(0);
                TransportFactory tf = 
                    bus.getTransportFactoryManager().
                        getTransportFactory(el.getElementType().getNamespaceURI());
                return tf.createServerTransport(ref);
            }
        } catch (BusException ex) {
            LOG.severe("TRANSPORT_FACTORY_RETRIEVAL_FAILURE_MSG");
        }
        return null;
    }
    
    protected ServerTransport serverTransport() {
        return (ServerTransport)transport;
    }

    /*
    protected void storeSource(MessageContext context, ServerTransport st) {
        BindingContextUtils.storeBinding(context, this);
        BindingContextUtils.storeTransport(context, st);
        BindingContextUtils.storeBus(context, bus);
    }
    */
    
    private void injectSystemHandlers() {
        ResourceManager rm = new DefaultResourceManager();
        rm.addResourceResolver(new ResourceResolver() {
            @SuppressWarnings("unchecked")
            public <T> T resolve(String resourceName, Class<T> resourceType) {
                if (BUS_PROPERTY.equals(resourceName)) {
                    return  (T)AbstractServerBinding.this.getBus();
                } else if (SERVER_BINDING_PROPERTY.equals(resourceName)) {
                    return  (T)AbstractServerBinding.this;
                } else if (SERVER_TRANSPORT_PROPERTY.equals(resourceName)) {
                    return (T)transport;
                }
                return null;
            }
            
            public InputStream getAsStream(String name) {
                return null;
            }            
        });
        ResourceInjector injector = new ResourceInjector(rm); 
        
        getBindingImpl().injectSystemHandlers(injector);
    }
    
    private void terminateOutputContext(OutputStreamMessageContext outputContext) 
        throws IOException {
        outputContext.getOutputStream().flush();
        outputContext.getOutputStream().close();
    }  

    private void executeAsync(Runnable command) {
        Executor executor = 
            sbeCallback.getExecutor() != null
            ? sbeCallback.getExecutor() 
            : getBus().getWorkQueueManager().getAutomaticWorkQueue(); 
        try {
            executor.execute(command);
        } catch (RejectedExecutionException ree) {
            LOG.log(Level.WARNING, "ONEWAY_FALLBACK_TO_DIRECT_MSG", ree);
            command.run();
        }
    }    
}
