package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;


public abstract class AbstractClientBinding implements ClientBinding {
    private static final Logger LOG = LogUtils.getL7dLogger(AbstractClientBinding.class);

    protected final Bus bus;
    protected final EndpointReferenceType reference;
    protected ClientTransport transport;
    
    public AbstractClientBinding(Bus b, EndpointReferenceType ref) throws WSDLException, IOException {
        bus = b;
        reference = ref;
        transport = null;
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

            Port port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), ref);
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


    public ObjectMessageContext createObjectContext() {
        return new ObjectMessageContextImpl();
    }

    protected abstract MessageContext createBindingMessageContext(MessageContext orig);
    
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
    
    protected OutputStreamMessageContext createOutputStreamContext(MessageContext bindingContext)
        throws IOException {
        return transport.createOutputStreamContext(bindingContext);            
    }
    protected void finalPrepareOutputStreamContext(MessageContext bindingContext,
                                                   OutputStreamMessageContext ostreamContext) 
        throws IOException {
        transport.finalPrepareOutputStreamContext(ostreamContext);
    }
    
    public ObjectMessageContext invoke(ObjectMessageContext context,
                                       DataBindingCallback callback)
        throws IOException {
        
        HandlerInvoker handlerInvoker = createHandlerInvoker(); 
        handlerInvoker.setContext(context); 

        try { 
            MessageContext bindingContext = createBindingMessageContext(context);

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
                    InputStreamMessageContext ins = transport.invoke(ostreamContext);
                    
                    if (ostreamContext.getOutputStream() != null) {
                        ostreamContext.getOutputStream().close();
                    }
                    context.putAll(ins); 
                    bindingContext = createBindingMessageContext(context);
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
            MessageContext bindingContext = createBindingMessageContext(context);

            //Input Message For Client
            bindingContext.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);
            boolean continueProcessing = handlerInvoker.invokeLogicalHandlers(true);

            if (continueProcessing) {  

                if (null != bindingContext) {
                    marshal(context, bindingContext, callback);
                } else {
                    bindingContext = context;
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
                    ostreamContext.setOneWay(true);

                    handlerInvoker.invokeStreamHandlers(ostreamContext); 
                    finalPrepareOutputStreamContext(bindingContext, ostreamContext);

                    write(bindingContext, ostreamContext);
                    transport.invokeOneway(ostreamContext);
                    if (ostreamContext.getOutputStream() != null) { 
                        ostreamContext.getOutputStream().close(); 
                    }
                }
            }
        } finally { 
            handlerInvoker.mepComplete();
        }
    }

    public Future<ObjectMessageContext> invokeAsync(ObjectMessageContext context,
                                                    DataBindingCallback callback) {
        // TODO Auto-generated method stub
        // do not forget to set oneway operation.
        return null;
    }
}
