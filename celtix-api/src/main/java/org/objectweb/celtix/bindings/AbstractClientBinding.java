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

import static org.objectweb.celtix.context.ObjectMessageContext.CORRELATION_IN;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.BINDING_PROPERTY;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.TRANSPORT_PROPERTY;

public abstract class AbstractClientBinding extends AbstractBindingBase implements ClientBinding {
    private static final Logger LOG = LogUtils.getL7dLogger(AbstractClientBinding.class);
    private static ResponseCorrelator responseCorrelator;

    protected Port port;
    protected ClientTransport transport;

    public AbstractClientBinding(Bus b, EndpointReferenceType ref) throws WSDLException, IOException {
        super(b, ref);
        bus.getLifeCycleManager().registerLifeCycleListener(new ShutdownListener(this));
        transport = null;
    }

    private static class ShutdownListener extends WeakReference<AbstractClientBinding> implements
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

    public static void clearResponseCorrelator() {
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

        Request request = new Request(this, objectCtx);

        try {
            OutputStreamMessageContext ostreamCtx = request.process(callback, null);

            if (null != ostreamCtx) {

                InputStreamMessageContext syncResponseContext = transport.invoke(ostreamCtx);
                Response response = null;
                if (null != syncResponseContext) {
                    response = new Response(request);
                    response.processProtocol(syncResponseContext);
                } else {
                    response = getResponseCorrelator().getResponse(request);
                    response.setObjectMessageContext(objectCtx);
                    response.setHandlerInvoker(request.getHandlerInvoker());
                }
                response.processLogical(callback);
                objectCtx = response.getObjectMessageContext();

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

        Request request = new Request(this, objectCtx);
        request.setOneway(true);

        try {
            OutputStreamMessageContext ostreamCtx = request.process(callback, null);

            if (null != ostreamCtx) {
                transport.invokeOneway(ostreamCtx);
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

        Request request = new Request(this, objectCtx);
        AsyncFuture asyncFuture = null;

        try {
            OutputStreamMessageContext ostreamCtx = request.process(callback, null);

            if (null != ostreamCtx) {

                Future<InputStreamMessageContext> ins = transport.invokeAsync(ostreamCtx, executor);
                asyncFuture = new AsyncFuture(ins, this, callback, request.getHandlerInvoker(), objectCtx);
            }

        } finally {
            request.complete();
        }

        return asyncFuture;
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
                factory.setResponseCallback(getResponseCorrelator());
                ret = factory.createClientTransport(ref);
            }
        } catch (BusException ex) {
            LOG.severe("TRANSPORT_FACTORY_RETREIVAL_FAILURE_MSG");
        }
        assert ret != null;
        return ret;
    }

    protected synchronized ResponseCorrelator getResponseCorrelator() {
        if (responseCorrelator == null) {
            responseCorrelator = new ResponseCorrelator(this);
        }
        return responseCorrelator;
    }

    protected void storeSource(MessageContext context) {
        context.put(BINDING_PROPERTY, this);
        context.setScope(BINDING_PROPERTY, MessageContext.Scope.HANDLER);
        context.put(TRANSPORT_PROPERTY, transport);
        context.setScope(TRANSPORT_PROPERTY, MessageContext.Scope.HANDLER);
    }

    protected String retrieveCorrelationID(MessageContext context) {
        return (String)context.get(CORRELATION_IN);
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
