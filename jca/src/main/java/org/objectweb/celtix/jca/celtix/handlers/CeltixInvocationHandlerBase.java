package org.objectweb.celtix.jca.celtix.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.jca.celtix.CeltixInvocationHandler;
import org.objectweb.celtix.jca.celtix.CeltixInvocationHandlerData;
import org.objectweb.celtix.jca.celtix.CeltixManagedConnection;

abstract class CeltixInvocationHandlerBase<T> implements CeltixInvocationHandler {

    private static final Logger LOG = Logger.getLogger(CeltixInvocationHandlerBase.class.getName());

    private CeltixInvocationHandler next;
    private CeltixInvocationHandlerData data;

    public CeltixInvocationHandlerBase(CeltixInvocationHandlerData cihd) {
        this.data = cihd;
    }

    public void setNext(CeltixInvocationHandler cih) {
        this.next = cih;
    }

    public CeltixInvocationHandler getNext() {
        return next;
    }

    public CeltixInvocationHandlerData getData() {
        return data;
    }

    protected Object invokeNext(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = null;
        if (getNext() != null) {
            ret = getNext().invoke(proxy, method, args);
        } else {
            // if the next handler is null , there could be end of the handler chains
            LOG.fine("no more invocation handler");
        }
        return ret;
    }

    protected Throwable getExceptionToThrow(InvocationTargetException ex, Method targetMethod)
        throws Throwable {
        Throwable targetException = ex.getTargetException();
        Throwable ret = null;

        if (isOkToThrow(targetMethod, targetException)) {
            ret = targetException;
        } else {
            //get the exception when call the method
            RuntimeException re = new RuntimeException("Unexpected exception from method " + targetMethod,
                                                      targetException);
            LOG.info(re.toString());
            ret = re;
        }
        return ret;
    }

    private boolean isOkToThrow(Method method, Throwable t) {
        return t instanceof RuntimeException || isCheckedException(method, t);
    }

    private boolean isCheckedException(Method method, Throwable t) {
        boolean isCheckedException = false;

        Class<?> checkExceptionTypes[] = (Class<?>[])method.getExceptionTypes();

        for (int i = 0; i < checkExceptionTypes.length; i++) {
            if (checkExceptionTypes[i].isAssignableFrom(t.getClass())) {
                isCheckedException = true;

                break;
            }
        }

        return isCheckedException;
    }

}

class CeltixInvocationHandlerDataExtend implements CeltixInvocationHandlerData {
    private Bus bus;
    private CeltixManagedConnection managedConnection;
    private Subject subject;
    private Object target;
    
    public final void setSubject(Subject sub) {
        this.subject = sub;
    }

    public final Subject getSubject() {
        return subject;
    }

    public final void setBus(final Bus b) {
        this.bus = b;
    }

    public final Bus getBus() {
        return bus;
    }

    public final void setManagedConnection(final CeltixManagedConnection celtixManagedConnection) {
        this.managedConnection = celtixManagedConnection;
    }

    public final CeltixManagedConnection getManagedConnection() {
        return managedConnection;
    }

    public void setTarget(Object t) {
        this.target = t; 
        
    }

    public Object getTarget() {        
        return target;
    }

}
