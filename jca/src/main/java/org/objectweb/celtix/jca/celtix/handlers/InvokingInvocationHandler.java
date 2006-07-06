package org.objectweb.celtix.jca.celtix.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import org.objectweb.celtix.jca.celtix.CeltixInvocationHandlerData;

/**
 * delegates invocations to the target object 
 */
public class InvokingInvocationHandler extends CeltixInvocationHandlerBase {

    private static final Logger LOG = Logger.getLogger(InvokingInvocationHandler.class.getName());

    public InvokingInvocationHandler(CeltixInvocationHandlerData data) {
        super(data);
    }

    public Object invoke(Object proxy, Method method , Object[] args) throws Throwable { 
        
        Object ret = null;
        if (!isConnectionCloseMethod(method)) {
            ret = invokeTargetMethod(proxy, method, args);
        } else {
            closeConnection(proxy);
        }

        return ret;
    } 


    private boolean isConnectionCloseMethod(Method m) {
        return "close".equals(m.getName());
    }

    private void closeConnection(Object handle) throws ResourceException {
        LOG.fine("calling close on managed connection with handle");
        getData().getManagedConnection().close(handle);
    }
    
    private Object invokeTargetMethod(Object proxy, Method method, Object args[]) throws Throwable {

        Object ret = null;

        try {
            ret = method.invoke(getData().getTarget(), args);
        } catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        }
        return ret;
    }

}
