package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.BINDING_PROPERTY;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.TRANSPORT_PROPERTY;

public abstract class AbstractServerBinding extends AbstractBindingBase implements ServerBinding {

    private static final Logger LOG = LogUtils.getL7dLogger(AbstractServerBinding.class);

    protected ServerTransport transport;
    protected Endpoint endpoint;
    protected ServerBindingEndpointCallback sbeCallback;

    public AbstractServerBinding(Bus b, EndpointReferenceType ref, Endpoint ep,
                                 ServerBindingEndpointCallback sbcb) {
        super(b, ref);
        endpoint = ep;
        sbeCallback = sbcb;
    }

    // --- ServerBinding interface ---

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void activate() throws WSDLException, IOException {
        transport = createTransport(reference);

        ServerTransportCallback tc = new ServerTransportCallback() {

            public void dispatch(InputStreamMessageContext ctx, ServerTransport t) {
                AbstractServerBinding.this.dispatch(ctx, t);
            }

            public Executor getExecutor() {
                return AbstractServerBinding.this.getEndpoint().getExecutor();
            }
        };
        transport.activate(tc);
    }

    public void deactivate() throws IOException {
        transport.deactivate();
    }

    /**
     * Make an initial partial response to an incoming request. The partial
     * response may only contain 'header' information, and not a 'body'.
     * 
     * @param context object message context
     * @param callback callback for data binding
     */
    public void partialResponse(OutputStreamMessageContext outputContext, 
                                DataBindingCallback callback) throws IOException {
        ObjectMessageContext objectMessageContext = createObjectContext();
        objectMessageContext.putAll(outputContext);
        BindingContextUtils.storeDataBindingCallback(objectMessageContext, callback);

        if (callback != null) {
            Request request = new Request(this, objectMessageContext);
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
    
    protected abstract Method getSEIMethod(List<Class<?>> classList, MessageContext ctx); 

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
    
    protected void dispatch(InputStreamMessageContext istreamCtx, ServerTransport t) {
        LOG.info("Dispatched to binding on thread : " + Thread.currentThread());
        storeSource(istreamCtx, t);
        BindingContextUtils.storeServerBindingEndpointCallback(istreamCtx, sbeCallback);
        BindingContextUtils.storeEndpoint(istreamCtx, endpoint);
        
        ServerRequest inMsg = new ServerRequest(this, istreamCtx);         
        
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
        
        LOG.log(Level.INFO, "Before invoking on implementor");
        
        assert null != inMsg.getObjectCtx();
        // doInvocation(inMsg.getObjectCtx()); 
        inMsg.doInvocation(endpoint);
        
        LOG.log(Level.INFO, "After invoking on implementor");
        
        if (inMsg.isOneway()) {
            return;
        }
        
        // process response 
        
        inMsg.processOutbound(t, null); 
        
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

    private void storeSource(MessageContext context, ServerTransport st) {
        context.put(BINDING_PROPERTY, this);
        context.setScope(BINDING_PROPERTY, MessageContext.Scope.HANDLER);
        context.put(TRANSPORT_PROPERTY, st);
        context.setScope(TRANSPORT_PROPERTY, MessageContext.Scope.HANDLER);
    }
    
    private void terminateOutputContext(OutputStreamMessageContext outputContext) 
        throws IOException {
        outputContext.getOutputStream().flush();
        outputContext.getOutputStream().close();
    }  

}
