package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.context.WebServiceContextImpl;
import org.objectweb.celtix.handlers.HandlerInvoker;
import org.objectweb.celtix.transports.ServerTransport;

public class ServerRequest {
    
    public enum ServerRequestState {
        STREAM_HANDLERS_INVOKED (1), 
        STREAM_READ (2), 
        PROTOCOL_HANDLERS_INVOKED (3), 
        UNMARSHALLED (4), 
        LOGICAL_HANDLERS_INVOKED (5),
        DISPATCHED (6); 
        
        private final int val;
       
        ServerRequestState(int v) {
            this.val = v;
        }
        
        public int value() {
            return val;
        }
    }
    
    private static final Logger LOG = LogUtils.getL7dLogger(ServerRequest.class);

    private final AbstractBindingBase binding;
    private HandlerInvoker handlerInvoker;

    private InputStreamMessageContext istreamCtx;
    private MessageContext bindingCtx;
    private ObjectMessageContext objectCtx;
    private ServerRequestState state;
    private boolean isOneway;
    private boolean isOnewayDetermined;

    public ServerRequest(AbstractBindingBase b, InputStreamMessageContext i) {
        binding = b;
        istreamCtx = i;
        istreamCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);
    }

    /**
     * Used to create a ServerRequest to represent a resent outgoing
     * message. Hence the state is set up to allow us to proceed directly
     * to the processOutbound() phase.
     *  
     * @param b the underlying binding
     * @param objectContext the object message context
     */
    public ServerRequest(AbstractBindingBase b, ObjectMessageContext objectContext) {
        binding = b;
        objectCtx = objectContext;
        handlerInvoker = binding.createHandlerInvoker();
        state = ServerRequestState.DISPATCHED;
    }

    public AbstractBindingBase getBinding() {
        return binding;
    }

    public HandlerInvoker getHandlerInvoker() {
        return handlerInvoker;
    }

    public void setHandlerInvoker(HandlerInvoker h) {
        handlerInvoker = h;
    }

    public MessageContext getBindingCtx() {
        return bindingCtx;
    }

    public ObjectMessageContext getObjectCtx() {
        return objectCtx;
    }
   
    public ServerRequestState getState() {
        return state;
    }

    public void processInbound() {

        if (null == handlerInvoker) {
            handlerInvoker = binding.createHandlerInvoker();
        }
        handlerInvoker.setInbound();

        handlerInvoker.invokeStreamHandlers(istreamCtx);
        state = ServerRequestState.STREAM_HANDLERS_INVOKED;

        if (bindingCtx == null) {
            bindingCtx = binding.getBindingImpl().createBindingMessageContext(istreamCtx);
        } else {
            bindingCtx.putAll(istreamCtx);
        }
        bindingCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);

        try {
            binding.getBindingImpl().read(istreamCtx, bindingCtx);
            state = ServerRequestState.STREAM_READ;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "REQUEST_UNREADABLE_MSG", ex);
            throw new WebServiceException(ex);
        }

        boolean continueProcessing = handlerInvoker.invokeProtocolHandlers(isRequestor(), bindingCtx);
        state = ServerRequestState.PROTOCOL_HANDLERS_INVOKED;
        if (!continueProcessing) {
            return;
        }

        // store method and operation name in binding context if not already there -
        // using server binding endpoint callback

        storeOperationName();

        if (null == objectCtx) {
            objectCtx = binding.createObjectContext();  
            initObjectContext(objectCtx);
            objectCtx.putAll(bindingCtx);   
        } 

        binding.getBindingImpl().unmarshal(bindingCtx, objectCtx, getDataBindingCallback());
        state = ServerRequestState.UNMARSHALLED;
        objectCtx.put(OutputStreamMessageContext.ONEWAY_MESSAGE_TF, isOneway());

        handlerInvoker.invokeLogicalHandlers(isRequestor(), objectCtx);

        state = ServerRequestState.LOGICAL_HANDLERS_INVOKED;
    }

    public void doInvocation() {
        LOG.fine("doInvocation");
        QName operationName = (QName)objectCtx.get(MessageContext.WSDL_OPERATION);
        if (null == operationName) {
            Message msg = new Message("CONTEXT_MISSING_OPERATION_NAME_EXC", LOG);
            LOG.log(Level.SEVERE, msg.toString());
            objectCtx.setException(new WebServiceException(msg.toString()));
            return;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("operation name: " + operationName);
        }

        
        
        ServerDataBindingCallback method = (ServerDataBindingCallback)
            BindingContextUtils.retrieveDataBindingCallback(objectCtx);
        if (null == method) {
            Message msg = new Message("IMPLEMENTOR_MISSING_METHOD_EXC", LOG, operationName);
            LOG.log(Level.SEVERE, msg.toString());
            objectCtx.setException(new WebServiceException(msg.toString()));
            return;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("method: " + method);
        }

        try {
            new WebServiceContextImpl(objectCtx);
            
            method.invoke(objectCtx);
        } catch (WebServiceException wex) {
            Throwable cause = wex.getCause();
            if (cause != null) {
                objectCtx.setException(cause);
            } else {
                objectCtx.setException(wex);
            }
        } catch (InvocationTargetException ex) {
            LogUtils.log(LOG, Level.FINE, "IMPLEMENTOR_INVOCATION_EXCEPTION_MSG",
                         ex, method.getOperationName());
            Throwable cause = ex.getCause();
            if (cause != null) {
                objectCtx.setException(cause);
            } else {
                objectCtx.setException(ex);
            }
        }
        
        state = ServerRequestState.DISPATCHED;
    }

    public void processOutbound(ServerTransport st, Exception inboundException) {
        processOutbound(st, inboundException, false);
    }

    public void processOutbound(ServerTransport st, 
                                Exception inboundException,
                                boolean logicalChainTraversed) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.info("Reverse processing inbound message, exception: " + inboundException);
        }

        handlerInvoker.setOutbound();

        ObjectMessageContext replyObjectCtx = objectCtx;
        if (null == replyObjectCtx) {
            replyObjectCtx = binding.createObjectContext();
        }
        replyObjectCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);        
        
        if (null != inboundException) {
            replyObjectCtx.setException(inboundException);
        }

        // If protocol handlers were invoked inbound, then also invoke them
        // outbound - except when message is oneway.
        // TODO: relax this restriction to allow outbound processing of system
        // handlers.
        
        if (!logicalChainTraversed
            && state.value() >= ServerRequestState.LOGICAL_HANDLERS_INVOKED.value()
            && !isOneway()) {
            
            // Protocol and runtime exceptions have already been caught by 
            // handler invoker and stored in the object context.
            
            handlerInvoker.invokeLogicalHandlers(isRequestor(), replyObjectCtx);
        }
        
        // If on the inbound path we managed to construct a binding message
        // context use it - otherwise create a new one.

        MessageContext replyBindingCtx = bindingCtx;
        if (null == replyBindingCtx) {
            bindingCtx = binding.getBindingImpl().createBindingMessageContext(replyObjectCtx);
            replyBindingCtx = bindingCtx;
        } else if (null != replyObjectCtx) {
            replyBindingCtx.putAll(replyObjectCtx);
        }
                

        // The following will only succeed if we have a data binding callback.
        
        if (handlerInvoker.faultRaised(replyObjectCtx)) {
            LOG.fine("Marshalling fault.");
            marshalFault(replyObjectCtx, replyBindingCtx);
        } else if (null != replyObjectCtx.get(ObjectMessageContext.MESSAGE_PAYLOAD)
            || state.value() >= ServerRequestState.DISPATCHED.value()
            || null != replyObjectCtx.get(ObjectMessageContext.METHOD_RETURN)) {
            LOG.fine("Marshalling.");
            marshal(replyObjectCtx, replyBindingCtx);
        }

        // If protocol handlers were invoked inbound, then also invoke them
        // outbound - except when message is oneway.
        // TODO: relax this restriction to allow outbound processing of system
        // handlers.
        // Note we may not be able to find out if the message is oneway (in case where
        // inbound processing failed while invoking stream handlers).

        if (state.value() >= ServerRequestState.PROTOCOL_HANDLERS_INVOKED.value() && !isOneway()) {

            // Protocol and runtime exceptions have already been caught by
            // handler invoker and stored in binding context
            // As marshalling took place prior to invoking the
            // protocol handlers we need to go back and marshal this fault.
            
            handlerInvoker.invokeProtocolHandlers(isRequestor(), replyBindingCtx);
            
            if (handlerInvoker.faultRaised(replyBindingCtx)
                && !binding.getBindingImpl().hasFault(replyBindingCtx)) {
                LOG.fine("Marshalling fault raised by protocol handlers.");
                replyObjectCtx.setException((Exception)replyBindingCtx.get(
                    ObjectMessageContext.METHOD_FAULT));
                marshalFault(replyObjectCtx, replyBindingCtx);
            }
        }

        // create an output stream message context

        binding.getBindingImpl().updateMessageContext(replyBindingCtx);

        try {

            OutputStreamMessageContext ostreamCtx = st.createOutputStreamContext(replyBindingCtx);
            ostreamCtx.setOneWay(isOneway());

            if (isOneway()) {
                st.finalPrepareOutputStreamContext(ostreamCtx);
            } else {
                
                if (binding.getBindingImpl().hasFault(replyBindingCtx)) {
                    ostreamCtx.setFault(true);
                }

                handlerInvoker.invokeStreamHandlers(ostreamCtx);
                st.finalPrepareOutputStreamContext(ostreamCtx);
                binding.getBindingImpl().write(replyBindingCtx, ostreamCtx);
                OutputStream os = ostreamCtx.getOutputStream();
                os.flush();
            }

            LOG.fine("postDispatch from binding on thread : " + Thread.currentThread());
            st.postDispatch(replyBindingCtx, ostreamCtx);
            if (ostreamCtx.getOutputStream() != null) {
                ostreamCtx.getOutputStream().close();
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "RESPONSE_UNWRITABLE_MSG", ex);
            throw new WebServiceException(ex);
        } finally {
            complete();
        }
    }

    public void complete() {
        handlerInvoker.mepComplete(istreamCtx);
    }

    public boolean isRequestor() {
        return false;
    }

    public boolean isOneway() {
        if (!isOnewayDetermined) {
            isOneway = BindingContextUtils.isOnewayMethod(bindingCtx);
        }
        return isOneway;
    }
    
    public void setOneway(boolean oneway) {
        isOneway = oneway;
        isOnewayDetermined = true;
    }
    
    public boolean doDispatch() {
        return state.value() >= ServerRequestState.LOGICAL_HANDLERS_INVOKED.value()
            && handlerInvoker.isInbound() 
            && objectCtx != null
            && BindingContextUtils.retrieveDispatch(objectCtx);
    }

    protected void storeOperationName() {
        AbstractServerBinding sb = (AbstractServerBinding)binding;
        if (bindingCtx.containsKey(MessageContext.WSDL_OPERATION)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Determined operation using pre-existing operation name: "
                         + bindingCtx.get(MessageContext.WSDL_OPERATION));
            }
            return;
        }

        QName operationName = sb.getOperationName(bindingCtx);

        if (null != operationName) {
            bindingCtx.put(MessageContext.WSDL_OPERATION, operationName);
        } else {
            throw new WebServiceException("No operation matching message was found");
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Determined operation name using server binding endpoint callback: "
                     + operationName);
        }
    }

    public ServerDataBindingCallback getDataBindingCallback() {
        ServerDataBindingCallback callback = 
            (ServerDataBindingCallback)BindingContextUtils.retrieveDataBindingCallback(bindingCtx);
        if (null == callback) {
            assert null != objectCtx;
            ServerBindingEndpointCallback sbeCallback = BindingContextUtils
                .retrieveServerBindingEndpointCallback(bindingCtx);
            DataBindingCallback.Mode mode = sbeCallback.getServiceMode();
            callback = sbeCallback
                .getDataBindingCallback((QName)bindingCtx.get(MessageContext.WSDL_OPERATION),
                                        objectCtx, mode);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Using data binding callback constructed by server endpoint callback: " + callback);
            }
            BindingContextUtils.storeDataBindingCallback(bindingCtx, callback);
        } else if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Using data binding callback stored in context.");
        }
        return callback;
    }


    private void initObjectContext(ObjectMessageContext octx) {
        getDataBindingCallback().initObjectContext(octx);
    }
    
    
    private void marshalFault(ObjectMessageContext octx, MessageContext bctx) {
        DataBindingCallback callback = 
            BindingContextUtils.retrieveDataBindingCallback(bindingCtx);
        if (null == callback) {
            ServerBindingEndpointCallback sbeCallback = BindingContextUtils
                .retrieveServerBindingEndpointCallback(bindingCtx);
            callback = sbeCallback.getFaultDataBindingCallback(octx);
        }
        if (null == callback) {
            // TODO
            LOG.log(Level.SEVERE, "NO_DATA_BINDING_CALLBACK");
        }
        try {
            binding.getBindingImpl().marshalFault(octx, bctx, callback);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "COULD_NOT_MARSHAL_FAULT_MSG", ex);
        }
    }
    
    private void marshal(ObjectMessageContext octx, MessageContext bctx) {
        DataBindingCallback callback = getDataBindingCallback();
        if (null == callback) {
            // TODO
            LOG.log(Level.SEVERE, "NO_DATA_BINDING_CALLBACK");         
        } else {
            try {
                binding.getBindingImpl().marshal(octx, bctx, callback);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "COULD_NOT_MARSHAL_MSG", ex);
            }
        }
    }

}
