package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;

import static org.objectweb.celtix.context.ObjectMessageContext.CORRELATION_IN;

public class Response {

    private static final Logger LOG = LogUtils.getL7dLogger(Response.class);

    AbstractBindingBase binding;
    ObjectMessageContext objectCtx;
    HandlerInvoker handlerInvoker;
    MessageContext bindingCtx;

    public Response(Request request) {
        binding = request.getBinding();
        objectCtx = request.getObjectMessageContext();
        bindingCtx = request.getBindingContext();
        handlerInvoker = request.getHandlerInvoker();
    }

    public Response(AbstractBindingBase b, HandlerInvoker h) {
        binding = b;
        handlerInvoker = h;
    }

    public String getCorrelationId() {
        if (null != bindingCtx) {
            return (String)bindingCtx.get(CORRELATION_IN);
        }
        return null;
    }

    public ObjectMessageContext getObjectMessageContext() {
        return objectCtx;
    }
    
    public MessageContext getBindingMessageContext() {
        return bindingCtx;
    }
    
    public void setObjectMessageContext(ObjectMessageContext o) {
        objectCtx = o;
        if (null != bindingCtx) {
            objectCtx.putAll(bindingCtx);
        }
    }
    
    public void setHandlerInvoker(HandlerInvoker h) {
        h.setInbound();
        handlerInvoker = h;
    }

    /**
     * Handle an incoming message at the stream and protocol level. 
     * 
     * @param istreamCtx the inut stream messsage context
     */
    public void processProtocol(InputStreamMessageContext istreamCtx) { 

        // Output Message For Client    
        
        handlerInvoker.setInbound();
        handlerInvoker.setFault(istreamCtx.isFault());
        handlerInvoker.invokeStreamHandlers(istreamCtx);
        
        if (null == bindingCtx) {
            bindingCtx = binding.getBindingImpl()
                .createBindingMessageContext(istreamCtx);
        } else {
            bindingCtx.putAll(istreamCtx);
        }
        if (null == bindingCtx) {
            bindingCtx = istreamCtx;
        }
        
        bindingCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);
        
        safeRead(istreamCtx, bindingCtx);

        handlerInvoker.invokeProtocolHandlers(true, bindingCtx);
    }
    
    /**
     * Unmarshal and process an incoming message at the logical level.
     * 
     * @param callback the data binding callback
     */

    public void processLogical(DataBindingCallback callback) {
        assert null != bindingCtx;
        
        if (null != objectCtx) {
            objectCtx.putAll(bindingCtx);
        } else {
            objectCtx.putAll(bindingCtx);
        }

        if (!BindingContextUtils.retrieveDecoupledResponse(bindingCtx)) {
            if (!binding.getBindingImpl().hasFault(bindingCtx)) {
                binding.getBindingImpl().unmarshal(bindingCtx, objectCtx, callback);
            } else {
                binding.getBindingImpl().unmarshalFault(bindingCtx, objectCtx, callback);
            }
        }

        objectCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);
        handlerInvoker.invokeLogicalHandlers(true, objectCtx);
    }

    private void safeRead(InputStreamMessageContext inContext, MessageContext msgContext) {
        try {
            binding.getBindingImpl().read(inContext, msgContext);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "READ_IO_FAILURE_MSG", ex);
            throw new ProtocolException(ex);
        }
    }
}
