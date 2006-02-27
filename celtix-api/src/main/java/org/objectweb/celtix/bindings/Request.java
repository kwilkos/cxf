package org.objectweb.celtix.bindings;

import java.io.IOException;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;
import org.objectweb.celtix.transports.Transport;

import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.TRANSPORT_PROPERTY;

public class Request {
    
    private final AbstractBindingBase binding;
    private final Transport transport;
    private final ObjectMessageContext objectCtx;
    private MessageContext bindingCtx;
    private final HandlerInvoker handlerInvoker;

    public Request(AbstractBindingBase b, ObjectMessageContext o) {
        binding = b;
        objectCtx = o;

        transport = (Transport)objectCtx.get(TRANSPORT_PROPERTY);

        handlerInvoker = binding.createHandlerInvoker();

        objectCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);
        setOneway(false);
    }
    
    public String getCorrelationId() {
        return (String)objectCtx.get(ObjectMessageContext.CORRELATION_OUT);
    }

    public AbstractBindingBase getBinding() {
        return binding;
    }
    
    public Transport getTransport() {
        return transport;
    }
    
    public HandlerInvoker getHandlerInvoker() {
        return handlerInvoker;
    }
    
    public ObjectMessageContext getObjectMessageContext() {
        return objectCtx;
    }
    
    public MessageContext getBindingContext() {
        return bindingCtx;
    }

    public final void setOneway(boolean oneWay) {
        objectCtx.put(OutputStreamMessageContext.ONEWAY_MESSAGE_TF, oneWay);
    }
    
    public final boolean isOneway() {
        return ((Boolean)objectCtx.get(OutputStreamMessageContext.ONEWAY_MESSAGE_TF)).booleanValue();
    }

    public OutputStreamMessageContext process(DataBindingCallback callback,
                                              OutputStreamMessageContext ostreamCtx) throws IOException {
        if (handlerInvoker.invokeLogicalHandlers(true, objectCtx)) {
            bindingCtx = binding.getBindingImpl().createBindingMessageContext(objectCtx);
            bindingCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);
            if (null == bindingCtx) {
                bindingCtx = objectCtx;
            } else {
                binding.getBindingImpl().marshal(objectCtx, bindingCtx, callback);
            }  

            if (handlerInvoker.invokeProtocolHandlers(true, bindingCtx)) {
                binding.getBindingImpl().updateMessageContext(bindingCtx);
                if (ostreamCtx == null) {
                    ostreamCtx = transport.createOutputStreamContext(bindingCtx);
                }
                
                handlerInvoker.invokeStreamHandlers(ostreamCtx);
               
                transport.finalPrepareOutputStreamContext(ostreamCtx);

                binding.getBindingImpl().write(bindingCtx, ostreamCtx);
                
                return ostreamCtx;
            } else if (!isOneway()) {
                handlerInvoker.invokeProtocolHandlers(true, bindingCtx);
                bindingCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);
            }
        } else if (!isOneway()) {
            objectCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);
            handlerInvoker.invokeLogicalHandlers(true, objectCtx);
        }
        return null;
    }    

    public void complete() {
        handlerInvoker.mepComplete(objectCtx);
    }

}
