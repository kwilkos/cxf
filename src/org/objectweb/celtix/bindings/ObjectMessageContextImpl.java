package org.objectweb.celtix.bindings;

import java.lang.reflect.Method;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;

public class ObjectMessageContextImpl extends GenericMessageContext implements ObjectMessageContext {
    public static final String METHOD_OBJ = "org.objectweb.celtix.method";
    public static final String METHOD_PARAMETERS = "org.objectweb.celtix.parameters";
    public static final String METHOD_RETURN = "org.objectweb.celtix.return";
    public static final String METHOD_FAULT = "org.objectweb.celtix.fault";
    
    private static final long serialVersionUID = 401275179632507389L;

    public Object[] getMessageObjects() {
        return (Object[])get(METHOD_PARAMETERS);
    }

    public void setMessageObjects(Object... objects) {
        put(METHOD_PARAMETERS, (Object)objects);
        setScope(METHOD_PARAMETERS, MessageContext.Scope.HANDLER);
    }

    public void setReturn(Object retVal) {
        put(METHOD_RETURN, retVal);
        setScope(METHOD_RETURN, MessageContext.Scope.HANDLER);
    }

    public Object getReturn() {
        return get(METHOD_RETURN);
    }
    
    public void setMethod(Method method) {
        put(METHOD_OBJ, method);
        setScope(METHOD_OBJ, MessageContext.Scope.HANDLER);
    }

    public Method getMethod() {
        return (Method) get(METHOD_OBJ);
    }

    public void setException(Throwable ex) {
        put(METHOD_FAULT, ex);
        setScope(METHOD_FAULT, MessageContext.Scope.HANDLER);
    }
    
    public Throwable getException() {
        return (Throwable) get(METHOD_FAULT);
    }    
}

