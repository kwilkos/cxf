package org.objectweb.celtix.jca.celtix.handlers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;

import org.objectweb.celtix.jca.celtix.CeltixInvocationHandler;
import org.objectweb.celtix.jca.celtix.CeltixInvocationHandlerData;

/**
 * Handles invocations for methods defined on java.lang.Object, like hashCode,
 * toString and equals
 */
public class ObjectMethodInvocationHandler extends CeltixInvocationHandlerBase {

    private static final String EQUALS_METHOD_NAME = "equals";
    private static final String TO_STRING_METHOD_NAME = "toString";

    private static final Logger LOG = Logger.getLogger(ObjectMethodInvocationHandler.class.getName());

    public ObjectMethodInvocationHandler(CeltixInvocationHandlerData data) {
        super(data);
        LOG.fine("ObjectMethodInvocationHandler instance created");
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = null;

        LOG.fine(this + " on " + method);

        if (method.getDeclaringClass().equals(Object.class)) {
            if (EQUALS_METHOD_NAME.equals(method.getName())) {
                ret = doEquals(args[0]);
            } else if (TO_STRING_METHOD_NAME.equals(method.getName())) {
                ret = doToString();
            } else {
                ret = method.invoke(getData().getTarget(), args);
            }
        } else {
            ret = invokeNext(proxy, method, args);
        }
        return ret;
    }

    /**
     * checks for equality based on the underlying target object
     */
    private Boolean doEquals(Object rhs) {

        Boolean ret = Boolean.FALSE;

        // find the target object and do comparison
        if (rhs instanceof Proxy) {
            InvocationHandler rhsHandler = Proxy.getInvocationHandler(rhs);
            if (rhsHandler instanceof CeltixInvocationHandler) {
                ret = Boolean.valueOf(getData().getTarget() == ((CeltixInvocationHandler)rhsHandler).getData()
                    .getTarget());
            }
        }
        return ret;
    }

    private String doToString() {
        return "ConnectionHandle. Associated ManagedConnection: " + getData().getManagedConnection();
    }

}
