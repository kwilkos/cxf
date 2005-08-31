package org.objectweb.celtix.bindings;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransport;

public class ServerBindingCallback implements Runnable {

    private static Logger logger = Logger.getLogger(ServerBindingCallback.class.getPackage().getName());

    private InputStreamMessageContext context;
    private ServerTransport transport;
    private ServerBinding binding;

    public ServerBindingCallback(InputStreamMessageContext ctx, ServerTransport t, ServerBinding b) {
        context = ctx;
        transport = t;
        binding = b;
    }

    public void run() {

        // use ServerBinding to read the SAAJ model and insert it into a SOAPMessageContext
        // (via SOAPMessageContext.setMessage())
        
        // invoke handlers
        
        // traverse SAAJ model to obtain operation/method name
        
        // unmarshal arguments for method call
        
        // invoke on implementor
        Object implementor = binding.getEndpoint().getImplementor();

        // get method name and parameters from context

        Method method = null;
        Object args[] = null;
        
        logger.info("Should now invoke method on implementor");
        /*
        try {
            method.invoke(implementor, args);
        } catch (IllegalAccessException ex) {
            logger.severe("Failed to invoke method " + method.getName() + " on implementor:\n"
                          + ex.getMessage());
        } catch (InvocationTargetException ex) {
            logger.severe("Failed to invoke method " + method.getName() + " on implementor:\n"
                          + ex.getMessage());
        }
        */
        
        
        // marshal objects into new SSAJ model (new model for response)
        
        // insert this model into Message
        
        // write message to transport
    }

}
