package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;


import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bus.jaxws.EndpointUtils;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;


public abstract class AbstractServerBinding implements ServerBinding {

    private static final Logger LOG = LogUtils.getL7dLogger(AbstractServerBinding.class);

    protected final Bus bus;
    protected final EndpointReferenceType reference;
    protected ServerTransport transport;
    protected Endpoint endpoint;

    public AbstractServerBinding(Bus b, EndpointReferenceType ref, Endpoint ep) {
        bus = b;
        reference = ref;
        endpoint = ep;
    }
    
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

    public ObjectMessageContext createObjectContext() {
        return new ObjectMessageContextImpl();
    }

    protected abstract ServerTransport createTransport(EndpointReferenceType ref) 
        throws WSDLException, IOException;

    protected abstract MessageContext createBindingMessageContext(MessageContext orig);

    protected abstract void marshal(ObjectMessageContext objCtx, MessageContext replyCtx);
    
    protected abstract void marshalFault(ObjectMessageContext objCtx, MessageContext replyCtx);

    protected void unmarshal(MessageContext requestCtx, ObjectMessageContext objCtx) {
        QName operationName = getOperationName(requestCtx);
        objCtx.put(MessageContext.WSDL_OPERATION, operationName);
    }

    protected abstract void write(MessageContext replyCtx, OutputStreamMessageContext outCtx)
        throws IOException;

    protected abstract void read(InputStreamMessageContext inCtx, MessageContext context) throws IOException;

    protected abstract MessageContext invokeOnProvider(MessageContext requestCtx, ServiceMode mode)
        throws RemoteException;

    protected void dispatch(InputStreamMessageContext inCtx, ServerTransport t) {
        LOG.info("Dispatched to binding on thread : " + Thread.currentThread());
        MessageContext requestCtx = createBindingMessageContext(inCtx);

        //Input Message
        requestCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.FALSE);
        // use ServerBinding to read the SAAJ model and insert it into a
        // SOAPMessageContext
        
        try {
            read(inCtx, requestCtx);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "REQUEST_UNREADABLE_MSG", ex);
            throw new WebServiceException(ex);
        }

        // invoke handlers

        MessageContext replyCtx = null;

        ServiceMode mode = EndpointUtils.getServiceMode(endpoint);
        try {
            if (null != mode) {
                replyCtx = invokeOnProvider(requestCtx, mode);
            } else {
                replyCtx = invokeOnMethod(requestCtx);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "PROVIDER_INVOCATION_FAILURE_MSG", ex);
            throw new WebServiceException(ex);
        }

        try {
            OutputStreamMessageContext outCtx = t.createOutputStreamContext(inCtx);
            // TODO - invoke output stream handlers
            t.finalPrepareOutputStreamContext(outCtx);

            write(replyCtx, outCtx);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "RESPONSE_UNWRITABLE_MSG", ex);
            throw new WebServiceException(ex);
        }
        
        LOG.info("Dispatch complete on thread : " + Thread.currentThread());
    }

    private MessageContext invokeOnMethod(MessageContext requestCtx) {

        // get operation name from message context and identify method
        // in implementor
        //REVISIT replyCtx should be created once the method is invoked
        MessageContext replyCtx = createBindingMessageContext(new GenericMessageContext());
        assert replyCtx != null;
        
        QName operationName = getOperationName(requestCtx);

        if (null == operationName) {
            LOG.severe("CONTEXT_MISSING_OPERATION_NAME_MSG");
            throw new WebServiceException("Request Context does not include operation name");
        }
        
        // get implementing method
        Method method = EndpointUtils.getMethod(endpoint, operationName);
        if (method == null) {
            LOG.log(Level.SEVERE, "IMPLEMENTOR_MISSING_METHOD_MSG", operationName);
            throw new WebServiceException("Web method: " + operationName + " not found in implementor.");
        }

        // unmarshal arguments for method call - includes transferring the
        // operationName from the message context into the object context

        ObjectMessageContext objContext = createObjectContext();
        objContext.setMethod(method);
       
        unmarshal(requestCtx, objContext);
        
        // get parameters from object context and invoke on implementor

        Object result = null;
        Object params[] = (Object[])objContext.getMessageObjects();

        try {
            result = method.invoke(getEndpoint().getImplementor(), params);
            objContext.setReturn(result);

            replyCtx.put(ObjectMessageContext.MESSAGE_INPUT, Boolean.TRUE);
            if (null != replyCtx) {
                marshal(objContext, replyCtx);
            }
        } catch (IllegalAccessException ex) {
            LogUtils.log(LOG, Level.SEVERE, "IMPLEMENTOR_INVOCATION_FAILURE_MSG", ex, method.getName());
            objContext.setException(ex);
        } catch (InvocationTargetException ex) {
            LogUtils.log(LOG, Level.SEVERE, "IMPLEMENTOR_INVOCATION_EXCEPTION_MSG", ex, method.getName());
            Throwable cause = ex.getCause();
            if (cause != null) {
                objContext.setException(cause);
            } else {
                objContext.setException(ex);
            }            
        } finally {
            if (null != objContext.getException()) {
                marshalFault(objContext, replyCtx);
            }
        }
        return replyCtx;
    }
    
    protected abstract QName getOperationName(MessageContext ctx);

}
