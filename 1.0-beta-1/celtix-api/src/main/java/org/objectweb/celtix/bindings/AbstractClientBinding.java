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
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.buslifecycle.BusLifeCycleListener;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public abstract class AbstractClientBinding extends AbstractBindingBase implements ClientBinding {
    private static final Logger LOG = LogUtils.getL7dLogger(AbstractClientBinding.class);

    protected Port port;
    protected ClientTransport transport;
    private ResponseCorrelator responseCorrelator;
    
    public AbstractClientBinding(Bus b, EndpointReferenceType ref) throws WSDLException, IOException {
        super(b, ref);
        bus.getLifeCycleManager().registerLifeCycleListener(new ShutdownListener(this));
        transport = null;
    }

    private class ShutdownListener extends WeakReference<AbstractClientBinding> implements
        BusLifeCycleListener {

        ShutdownListener(AbstractClientBinding c) {
            super(c);
        }

        public void initComplete() {
            // nothing
        }

        public void preShutdown() {
            if (get() != null) {
                get().shutdown();
                clear();
            }
        }

        public void postShutdown() {
            clearResponseCorrelator();
        }
    }

    public void clearResponseCorrelator() {
        responseCorrelator = null;
    }

    // --- Methods to be implemented by concrete client bindings ---

    public abstract AbstractBindingImpl getBindingImpl();

    // --- Methods to be implemented by concrete client bindings ---

    // --- ClientBinding interface ---

    public ObjectMessageContext invoke(ObjectMessageContext objectCtx, DataBindingCallback callback)
        throws IOException {

        getTransport();
        storeSource(objectCtx);
        BindingContextUtils.storeDataBindingCallback(objectCtx, callback);

        Request request = new Request(this, objectCtx);

        try {
            OutputStreamMessageContext ostreamCtx = request.process(null);

            if (null != ostreamCtx) {

                InputStreamMessageContext responseContext = transport.invoke(ostreamCtx);
                Response fullResponse = null;
                if (BindingContextUtils.retrieveDecoupledResponse(responseContext)) {
                    // partial response traverses complete handler chain first
                    Response partialResponse = new Response(request);
                    partialResponse.processProtocol(responseContext);
                    partialResponse.processLogical(callback);
                    
                    if (BindingContextUtils.isOnewayMethod(objectCtx)) {
                        // no full response
                        objectCtx = partialResponse.getObjectMessageContext();
                    } else {
                        // wait for decoupled full response and tarverse logical chain 
                        // (protocol chain already traversed by ResponseCorrelator)
                        fullResponse = getResponseCorrelator().getResponse(request);
                        fullResponse.setObjectMessageContext(objectCtx);
                        fullResponse.setHandlerInvoker(request.getHandlerInvoker());
                        fullResponse.processLogical(callback);
                        objectCtx = fullResponse.getObjectMessageContext();
                    }
                } else {
                    // synchronous full response
                    fullResponse = new Response(request);
                    fullResponse.processProtocol(responseContext);
                    fullResponse.processLogical(callback);
                    objectCtx = fullResponse.getObjectMessageContext();
                }
                
            }

        } finally {
            request.complete();
        }

        return objectCtx;
    }

    public void invokeOneWay(ObjectMessageContext objectCtx, DataBindingCallback callback) 
        throws IOException {
        getTransport();
        storeSource(objectCtx);
        BindingContextUtils.storeDataBindingCallback(objectCtx, callback);

        Request request = new Request(this, objectCtx);
        request.setOneway(true);

        try {
            OutputStreamMessageContext ostreamCtx = request.process(null);

            if (null != ostreamCtx) {
                // one of the (system handlers) may have indicated that it expects 
                // headers to be piggybacked in the response 
                // if this is the case, use the transports invoke rather than invokeOneway
                // to give the handlers a chance to process these headers
                
                if (BindingContextUtils.isOnewayTransport(ostreamCtx)) {
                    transport.invokeOneway(ostreamCtx);
                } else {
                    LOG.fine("Sending message as a twoway request as required by system handlers.");
                    InputStreamMessageContext istreamCtx = transport.invoke(ostreamCtx);
                    Response response = new Response(request);     
                    response.processProtocol(istreamCtx);
                    response.processLogical(null);
                }                
            }

        } finally {
            request.complete();
        }
    }

    public Future<ObjectMessageContext> invokeAsync(ObjectMessageContext objectCtx,
                                                    DataBindingCallback callback, Executor executor)
        throws IOException {
        LOG.info("AbstractClientBinding: invokeAsync");
        getTransport();
        storeSource(objectCtx);
        BindingContextUtils.storeDataBindingCallback(objectCtx, callback);

        Request request = new Request(this, objectCtx);
        AsyncFuture asyncFuture = null;

        try {
            OutputStreamMessageContext ostreamCtx = request.process(null);

            if (null != ostreamCtx) {

                Future<InputStreamMessageContext> ins = transport.invokeAsync(ostreamCtx, executor);
                asyncFuture = new AsyncFuture(ins, this, callback, request.getHandlerInvoker(), objectCtx);
            }

        } finally {
            request.complete();
        }

        return asyncFuture;
    }

    public synchronized ResponseCallback createResponseCallback() {
        return responseCorrelator = new ResponseCorrelator(this);
    }
        
    // --- ClientBinding interface ---

    // --- helpers ---

    protected void shutdown() {
        if (transport != null) {
            transport.shutdown();
            transport = null;
        }
    }

    protected synchronized ClientTransport getTransport() throws IOException {
        if (transport == null) {
            try {
                transport = createTransport(reference);
            } catch (WSDLException e) {
                throw (IOException)(new IOException(e.getMessage()).initCause(e));
            }
        }
        assert transport != null : "transport is null";
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

                TransportFactory factory = bus.getTransportFactoryManager()
                    .getTransportFactory(el.getElementType().getNamespaceURI());
                ret = factory.createClientTransport(ref, this);
            }
        } catch (BusException ex) {
            LOG.severe("TRANSPORT_FACTORY_RETREIVAL_FAILURE_MSG");
        }
        assert ret != null;
        return ret;
    }

    protected synchronized ResponseCorrelator getResponseCorrelator() {
        if (responseCorrelator == null) {
            responseCorrelator = 
                (ResponseCorrelator)transport.getResponseCallback();
        }
        return responseCorrelator;
    }
    
    protected void storeSource(MessageContext context) {
        BindingContextUtils.storeBinding(context, this);
        BindingContextUtils.storeTransport(context, transport);
        BindingContextUtils.storeBus(context, bus);
    }

    protected void finalPrepareOutputStreamContext(MessageContext bindingContext,
                                                   OutputStreamMessageContext ostreamContext)
        throws IOException {
        transport.finalPrepareOutputStreamContext(ostreamContext);
    }

    public ObjectMessageContext getObjectMessageContextAsync(InputStreamMessageContext ins,
                                                             HandlerInvoker handlerInvoker,
                                                             DataBindingCallback callback,
                                                             ObjectMessageContext objectCtx) {
        Response response = new Response(this, handlerInvoker);
        try {  
            response.setObjectMessageContext(objectCtx);
            response.processProtocol(ins);
            response.processLogical(callback);
        } finally {
            handlerInvoker.mepComplete(objectCtx);
        }

        return response.getObjectMessageContext();
    }
}
