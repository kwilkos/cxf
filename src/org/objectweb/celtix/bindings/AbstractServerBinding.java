package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bus.EndpointImpl;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;


public abstract class AbstractServerBinding implements ServerBinding {

    private static Logger logger = Logger.getLogger(AbstractServerBinding.class.getName());

    protected final Bus bus;
    protected final EndpointReferenceType reference;
    protected ServerTransport transport;
    protected Endpoint endpoint;

    public AbstractServerBinding(Bus b, EndpointReferenceType ref, Endpoint ep) {
        bus = b;
        reference = ref;
        endpoint = ep;
    }

    public ObjectMessageContext createObjectContext() {
        return new ObjectMessageContextImpl();
    }
    
    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void activate() throws WSDLException, IOException {
        String address = EndpointReferenceUtils.getAddress(reference);

        TransportFactory tf = getDefaultTransportFactory(address);
        transport = tf.createServerTransport(reference);

        transport.activate(new ServerTransportCallback() {

            public void dispatch(InputStreamMessageContext ctx, ServerTransport t) {
                Runnable r = new ServerBindingCallback(ctx, t, AbstractServerBinding.this);
                // wait for documentation and implementation of JAX-WS RI to
                // sync up
                Endpoint ep = AbstractServerBinding.this.getEndpoint();
                EndpointImpl epi = (EndpointImpl)ep;
                Executor ex = epi.getExecutor();

                // REVISIT: exceptions thrown on the thread executing the
                // Runnable
                // may go unreported.
                // Probably need to add something similar to
                // ThreadPoolExecutor.afterExecute()
                // to the AutomaticWorkQueueInterface.
                // Even then the executor may be one set by the application in
                // which case we cannot
                // do anything about these exceptions.

                ex.execute(r);

            }

        });
    }

    void dispatch(InputStreamMessageContext ictx, ServerTransport t) {
   
        MessageContext bindingContext = createBindingMessageContext();
        
        // use ServerBinding to read the SAAJ model and insert it into a
        // SOAPMessageContext

        try {
            read(ictx, bindingContext);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        // invoke handlers

        // get operation name from message context and identify method
        // in implementor

        Method method = getMethod(bindingContext);
        if (method == null) {
            logger.severe("Web method: " + getOperationName(bindingContext)
                          + " not found in implementor.");
            return;
        }

        // unmarshal arguments for method call - includes transferring the
        // operationName
        // from the message context into the object context

        ObjectMessageContext objContext = createObjectContext();
        unmarshal(bindingContext, objContext);

        // get parameters from object context

        Object args[] = (Object[])objContext.getMessageObjects();
        
        // REVISIT: check for correct number and type
        
        // invoke on implementor

        Object result = null;
        try {
            result = method.invoke(getEndpoint().getImplementor(), args);
        } catch (IllegalAccessException ex) {
            logger.severe("Failed to invoke method " + method.getName() + " on implementor:\n"
                          + ex.getMessage());
        } catch (InvocationTargetException ex) {
            logger.severe("Failed to invoke method " + method.getName() + " on implementor:\n"
                          + ex.getMessage());
        }

        objContext.put("org.objectweb.celtix.return", result);
        
        bindingContext = createBindingMessageContext();
        // marshal objects into new SSAJ model (new model for response)
        marshal(objContext, bindingContext);
        try {
            OutputStreamMessageContext ostreamContext = transport.createOutputStreamContext(bindingContext);
            transport.finalPrepareOutputStreamContext(ostreamContext);

            write(bindingContext, ostreamContext);
        } catch (IOException ioe) {
            logger.severe("Failed to write response for " + method.getName() + "\n"
                          + ioe.getMessage());            
        }
    }

    public void deactivate() throws IOException {
        transport.deactivate();
    }

    protected abstract TransportFactory getDefaultTransportFactory(String address);

    protected abstract MessageContext createBindingMessageContext();

    protected abstract void read(InputStreamMessageContext inCtx, 
                                 MessageContext context) throws IOException;

    protected void unmarshal(MessageContext context, ObjectMessageContext objContext) {
        QName operationName = getOperationName(context);
        objContext.put(MessageContext.WSDL_OPERATION, operationName);
    }
    
    protected abstract void marshal(ObjectMessageContext objContext, MessageContext context);

    protected abstract void write(MessageContext context, OutputStreamMessageContext outCtx)
        throws IOException;

    private QName getOperationName(MessageContext ctx) {
        return (QName)ctx.get(MessageContext.WSDL_OPERATION);
    }

    private Method getMethod(MessageContext ctx) {
        QName operation = getOperationName(ctx);
        String operationName = operation.getLocalPart();
        Method method = null;
        Object implementor = getEndpoint().getImplementor();
        Method[] methods = implementor.getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(operationName)) {
                method = m;
                WebMethod wm = (WebMethod)m.getAnnotation(WebMethod.class);
                if (wm != null && wm.operationName().equals(operation)) {
                    break;
                }
                // assume this is an overloaded version of the method we are
                // looking for
                // continue searching for a better match
            }
        }
        return method;

    }
}
