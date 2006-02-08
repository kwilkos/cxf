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

    private static final Logger LOG = LogUtils.getL7dLogger(Request.class);

    AbstractBindingBase binding;
    ObjectMessageContext objectCtx;
    HandlerInvoker handlerInvoker;
    MessageContext bindingCtx;

    protected Response(Request request) {
        binding = request.getBinding();
        objectCtx = request.getObjectMessageContext();
        bindingCtx = request.getBindingContext();
        handlerInvoker = request.getHandlerInvoker();
    }

    protected Response(AbstractBindingBase b, HandlerInvoker h) {
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
    
    public void setObjectMessageContext(ObjectMessageContext o) {
        objectCtx = o;
        if (null != bindingCtx) {
            objectCtx.putAll(bindingCtx);
        }
    }
    
    public void setHandlerInvoker(HandlerInvoker h) {
        handlerInvoker = h;
    }

    /**
     * Handle an incoming response to the extent required for correlation with
     * the corresponding request. Currently this include traversal of the stream
     * and protocol handler chains, though this will be replaced the appropriate
     * system handler logic.
     * 
     * @param outContext the outgoing context if available
     * @param inContext the incoming context
     * @param handlerInvoker the HanlderInvoker to use for chain traversal
     * @return the binding-specific context for the incoming dispatch
     */
    public void processProtocol(InputStreamMessageContext istreamCtx) {
        if (null != objectCtx) {
            objectCtx.putAll(istreamCtx);
        }

        if (null == bindingCtx) {
            bindingCtx = binding.getBindingImpl()
                .createBindingMessageContext(objectCtx != null ? objectCtx : istreamCtx);
        }
        if (null == bindingCtx) {
            bindingCtx = istreamCtx;
        }

        // Output Message For Client
        bindingCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);
        handlerInvoker.setInbound();
        handlerInvoker.setFault(istreamCtx.isFault());
        handlerInvoker.invokeStreamHandlers(istreamCtx);
        safeRead(istreamCtx, bindingCtx);

        // REVISIT replace with system handler traversal
        handlerInvoker.invokeProtocolHandlers(true, bindingCtx);
    }

    public void processLogical(DataBindingCallback callback) {
        // REVISIT allow for system handlers to "consume" the
        // incoming (presumably out-of-band) message

        if (!binding.getBindingImpl().hasFault(bindingCtx)) {
            binding.getBindingImpl().unmarshal(bindingCtx, objectCtx, callback);
        } else {
            binding.getBindingImpl().unmarshalFault(bindingCtx, objectCtx, callback);
        }

        objectCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);
        handlerInvoker.invokeLogicalHandlers(true);
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
