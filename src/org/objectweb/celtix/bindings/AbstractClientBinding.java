package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.lang.reflect.Method;
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
import org.objectweb.celtix.bus.handlers.HandlerChainInvoker;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
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
        transport = createTransport(reference);
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

    protected abstract  MessageContext createBindingMessageContext(MessageContext orig);
    
    protected abstract void marshal(ObjectMessageContext objContext, MessageContext context);

    protected abstract void unmarshal(MessageContext context, ObjectMessageContext objContext);
    
    protected abstract void unmarshalFault(MessageContext context, ObjectMessageContext objContext);    
    
    protected abstract boolean hasFault(MessageContext context);

    protected abstract void write(MessageContext context, OutputStreamMessageContext outCtx);

    protected abstract void read(InputStreamMessageContext inCtx, MessageContext context);
    
    public ObjectMessageContext invoke(ObjectMessageContext context) throws IOException {
        
        HandlerChainInvoker handlerInvoker = new HandlerChainInvoker(getBinding().getHandlerChain(), context);

        try { 
            MessageContext bindingContext = createBindingMessageContext(context);

            //Input Message For Client
            bindingContext.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);
            boolean continueProcessing = handlerInvoker.invokeLogicalHandlers(true);

            if (continueProcessing) {  

                if (null != bindingContext) {
                    marshal(context, bindingContext);
                } else {
                    bindingContext = context;
                }

                assert transport != null : "transport is null";

                OutputStreamMessageContext ostreamContext = 
                    transport.createOutputStreamContext(bindingContext);

                // TODO - invoke output stream handlers
                transport.finalPrepareOutputStreamContext(ostreamContext);

                write(bindingContext, ostreamContext);
                
                InputStreamMessageContext ins = transport.invoke(ostreamContext);
                
                bindingContext = createBindingMessageContext(ins);
                if (null == bindingContext) {
                    bindingContext = ins;
                }
                
                //Output Message For Client
                bindingContext.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);   
                // TODO - invoke input stream handlers
                read(ins, bindingContext);
                
                // TODO - invoke binding handlers
                Method m = context.getMethod();
                context = createObjectContext();
                context.setMethod(m);
                handlerInvoker.setContext(context); 

                if (!hasFault(bindingContext)) {
                    unmarshal(bindingContext, context);
                } else {
                    unmarshalFault(bindingContext, context);
                }
            }
            handlerInvoker.setInbound();
            handlerInvoker.invokeLogicalHandlers(true);
        } finally { 
            handlerInvoker.mepComplete();
        }
        return context;
    }

    
    public void invokeOneWay(ObjectMessageContext context) throws IOException {
        // TODO Auto-generated method stub

    }

    public Future<ObjectMessageContext> invokeAsync(ObjectMessageContext context) {
        // TODO Auto-generated method stub
        return null;
    }
}
