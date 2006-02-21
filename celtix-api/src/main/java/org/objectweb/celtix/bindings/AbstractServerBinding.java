package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Holder;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.context.WebServiceContextImpl;
import org.objectweb.celtix.handlers.HandlerInvoker;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_TRANSPORT_PROPERTY;
import static org.objectweb.celtix.ws.rm.JAXWSRMConstants.ABSTRACT_SERVER_BINDING_PROPERTY;

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

    // --- ServerBinding interface ---

    // --- Methods to be implemented by concrete server bindings ---

    public abstract AbstractBindingImpl getBindingImpl();

    protected abstract ServerTransport createTransport(EndpointReferenceType ref) throws WSDLException,
        IOException;

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

    protected void dispatch(InputStreamMessageContext inCtx, ServerTransport t) {
        LOG.info("Dispatched to binding on thread : " + Thread.currentThread());
        
        ObjectMessageContext objContext = null;
        HandlerInvoker invoker = null;
        Method method = null;
        MessageContext replyCtx = null;
        OutputStreamMessageContext outCtx = null;

        try {
            objContext = createObjectContext();

            storeTransport(objContext);
            invoker = createHandlerInvoker();
            invoker.setContext(objContext);
            invoker.setInbound();

            invoker.invokeStreamHandlers(inCtx);
            if (inCtx != null) {
                // this may be null during unit tests
                objContext.putAll(inCtx);
            }

            MessageContext requestCtx = getBindingImpl().createBindingMessageContext(objContext);
            // Input Message
            requestCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);

            try {
                getBindingImpl().read(inCtx, requestCtx);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "REQUEST_UNREADABLE_MSG", ex);
                throw new WebServiceException(ex);
            }

            method = getMethod(requestCtx, objContext);
            assert method != null;
            initObjectContext(objContext, method);

            if (isOneWay(method)) {
                try {
                    outCtx = processResponse(t, objContext, replyCtx, isOneWay(method));
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "RESPONSE_UNWRITABLE_MSG", ex);
                    throw new WebServiceException(ex);
                }
            }

            replyCtx = invokeOnMethod(requestCtx, objContext, invoker);
        } catch (RuntimeException ex) {
            objContext.setException(ex);
            if (replyCtx == null) {
                replyCtx = getBindingImpl().createBindingMessageContext(objContext);
                assert replyCtx != null;
            }
            getBindingImpl().marshalFault(objContext, replyCtx, getDataBindingCallback(objContext));
        } finally {
            try {
                if (!isOneWay(method)) {
                    outCtx = processResponse(t, objContext, replyCtx, isOneWay(method));
                }
                this.postDispatch(t, replyCtx, outCtx);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "RESPONSE_UNWRITABLE_MSG", ex);
                throw new WebServiceException(ex);
            }
        }

        LOG.info("Dispatch complete on thread : " + Thread.currentThread());
    }

    protected OutputStreamMessageContext processResponse(ServerTransport st,
                                                         ObjectMessageContext objectContext,
                                                         MessageContext replyContext, boolean isOneWay)
        throws IOException {
        OutputStreamMessageContext outCtx = null;
        if (isOneWay) {
            outCtx = getBindingImpl().createOutputStreamContext(st, objectContext);
            outCtx.setOneWay(isOneWay);
            finalPrepareOutputStreamContext(st, null, outCtx);
        } else {
            outCtx = getBindingImpl().createOutputStreamContext(st, replyContext);
            outCtx.setOneWay(isOneWay);

            if (isFault(objectContext, replyContext)) {
                outCtx.setFault(true);
            }

            HandlerInvoker invoker = createHandlerInvoker();
            invoker.setContext(objectContext);
            invoker.setOutbound();
            invoker.invokeStreamHandlers(outCtx);
            finalPrepareOutputStreamContext(st, replyContext, outCtx);
            getBindingImpl().write(replyContext, outCtx);
            OutputStream os = outCtx.getOutputStream();
            os.flush();
        }

        return outCtx;

    }

    protected void postDispatch(ServerTransport t, MessageContext bindingContext,
                                OutputStreamMessageContext ostreamContext) throws IOException {

        LOG.info("postDispatch from binding on thread : " + Thread.currentThread());
        t.postDispatch(bindingContext, ostreamContext);
        if (ostreamContext.getOutputStream() != null) {
            ostreamContext.getOutputStream().close();
        }
    }

    /**
     * invoke the target method. Ensure that any replies or exceptions are put
     * into the correct context for the return path
     */
    private boolean doInvocation(Method method, ObjectMessageContext objContext, MessageContext replyCtx,
                                 HandlerInvoker invoker) {

        assert method != null && objContext != null && replyCtx != null;

        boolean exceptionCaught = false;
        boolean continueProcessing = true;
        boolean payloadSet = false;
        try {

            continueProcessing = invoker.invokeLogicalHandlers(false);
            payloadSet = objContext.get(ObjectMessageContext.MESSAGE_PAYLOAD) != null;
            if (continueProcessing) {
                new WebServiceContextImpl(objContext);
                // get parameters from object context and invoke on implementor
                Object params[] = objContext.getMessageObjects();
                Object result = method.invoke(getEndpoint().getImplementor(), params);
                objContext.setReturn(result);
            }
        } catch (IllegalAccessException ex) {
            LogUtils.log(LOG, Level.SEVERE, "IMPLEMENTOR_INVOCATION_FAILURE_MSG", ex, method.getName());
            objContext.setException(ex);
            exceptionCaught = true;
        } catch (InvocationTargetException ex) {
            LogUtils.log(LOG, Level.INFO, "IMPLEMENTOR_INVOCATION_EXCEPTION_MSG", ex, method.getName());
            Throwable cause = ex.getCause();
            if (cause != null) {
                objContext.setException(cause);
            } else {
                objContext.setException(ex);
            }
            exceptionCaught = true;
        } finally {
            invoker.setOutbound();
            // invoke logical handlers from finally block to ensure
            // traversal when implementor call raises exception
            if (!isOneWay(method)) {
                switchToResponse(objContext, replyCtx);
                invoker.invokeLogicalHandlers(false);
            }
        }

        return exceptionCaught || invoker.faultRaised() || !(continueProcessing || payloadSet);
    }

    private void switchToResponse(ObjectMessageContext ctx, MessageContext replyCtx) {
        replyCtx.putAll(ctx);
        ctx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);
        ctx.remove(ObjectMessageContext.MESSAGE_PAYLOAD);
        replyCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);
    }

    private MessageContext invokeOnMethod(MessageContext requestCtx, ObjectMessageContext objContext,
                                          HandlerInvoker invoker) {

        objContext.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);

        MessageContext replyCtx = getBindingImpl().createBindingMessageContext(objContext);
        assert replyCtx != null;

        try {
            boolean continueProcessing = invoker.invokeProtocolHandlers(false, requestCtx);
            boolean requiresResponse = true;
            if (continueProcessing) {
                try {
                    getBindingImpl().unmarshal(requestCtx, objContext, getDataBindingCallback(objContext));

                    Method method = objContext.getMethod();
                    requiresResponse = !isOneWay(method);
                    // ensure response isn't marshalled if LogicalHandler
                    // suppressed dispatch to implementor, but didn't set
                    // the message payload
                    if (!(doInvocation(method, objContext, replyCtx, invoker)) && requiresResponse) {
                        switchToResponse(objContext, replyCtx);
                        if (null == objContext.getException()) {
                            getBindingImpl()
                                .marshal(objContext, replyCtx, getDataBindingCallback(objContext));
                        }
                    }
                } catch (ProtocolException pe) {

                    if (pe.getCause() != null) {
                        objContext.setException(pe.getCause());
                    } else {
                        objContext.setException(pe);
                    }
                } finally {
                    if (objContext.getException() != null) {
                        getBindingImpl().marshalFault(objContext, replyCtx,
                                                      getDataBindingCallback(objContext));
                    }
                }
            }

            if (requiresResponse && !invoker.invokeProtocolHandlers(false, replyCtx)) {
                // allow ProtocolHandlers raise faults on outbound
                // dispatch path
                switchToResponse(objContext, replyCtx);
                if (objContext.getException() != null) {
                    getBindingImpl().marshalFault(objContext, replyCtx, getDataBindingCallback(objContext));
                }
            }
        } finally {
            invoker.mepComplete();
        }

        return replyCtx;
    }

    private boolean isOneWay(Method method) {
        return method != null && (method.getAnnotation(Oneway.class) != null);
    }

    private Method getMethod(MessageContext requestCtx, ObjectMessageContext objContext) {

        WebServiceProvider wsProvider = sbeCallback.getWebServiceProvider();
        QName operationName = null;
        Method method = null;
        
        if (null != wsProvider) {
            operationName = new QName(wsProvider.targetNamespace(), "invoke");
            method = sbeCallback.getMethod(endpoint, operationName);
        } else {
            method = getSEIMethod(sbeCallback.getWebServiceAnnotatedClass(), requestCtx);
            
            if (null != method) {
                WebMethod wm = getWebMethodAnnotation(method);
                String namespace = getTargetNamespace(method.getDeclaringClass());
                operationName = null == wm ? new QName(namespace, method.getName()) 
                                           : new QName(namespace, wm.operationName());
            }
        }

        if (null == operationName) {
            LOG.severe("CONTEXT_MISSING_OPERATION_NAME_MSG");
            throw new WebServiceException("Request Context does not include operation name");
        }

        LOG.info("retrieved operation name from resuest:" + operationName);        
        if (objContext != null) {
            objContext.put(MessageContext.WSDL_OPERATION, operationName);
        }

        if (method == null) {
            LOG.log(Level.SEVERE, "IMPLEMENTOR_MISSING_METHOD_MSG", operationName);
            throw new WebServiceException("Web method: " + operationName + " not found in implementor.");
        }
        return method;
    }

    private void initObjectContext(ObjectMessageContext objCtx, Method method) {
        try {
            int idx = 0;
            Object[] methodArgs = (Object[])Array
                .newInstance(Object.class, method.getParameterTypes().length);
            for (Class<?> cls : method.getParameterTypes()) {
                if (cls.isAssignableFrom(Holder.class)) {
                    methodArgs[idx] = cls.newInstance();
                }
                idx++;
            }
            objCtx.setMessageObjects(methodArgs);
            objCtx.setMethod(method);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "INIT_OBJ_CONTEXT_FAILED");
            throw new WebServiceException(ex);
        }
    }

    private DataBindingCallback getDataBindingCallback(ObjectMessageContext objContext) {
        DataBindingCallback.Mode mode = sbeCallback.getServiceMode();
        return sbeCallback.createDataBindingCallback(objContext, mode);
    }

    private WebMethod getWebMethodAnnotation(Method m) {
        WebMethod wm = null;
        
        if (null != m) {
            wm = m.getAnnotation(WebMethod.class);
        }
        
        return wm;
    }

    private String getTargetNamespace(Class<?> cl) {
        String namespace = "";
        
        if (null != cl) {
            WebService ws = cl.getAnnotation(WebService.class);
            if (null != ws) {
                namespace = ws.targetNamespace();
            }
        }
        
        return namespace;
    }
    
    protected void storeTransport(MessageContext context) {
        context.put(SERVER_TRANSPORT_PROPERTY, transport);
        context.setScope(SERVER_TRANSPORT_PROPERTY, MessageContext.Scope.HANDLER);
    }
    
    protected final void storeBinding(MessageContext context) {
        context.put(ABSTRACT_SERVER_BINDING_PROPERTY, transport);
        context.setScope(ABSTRACT_SERVER_BINDING_PROPERTY, MessageContext.Scope.HANDLER);
    }

    protected abstract Method getSEIMethod(List<Class<?>> classList, MessageContext ctx); 

}
