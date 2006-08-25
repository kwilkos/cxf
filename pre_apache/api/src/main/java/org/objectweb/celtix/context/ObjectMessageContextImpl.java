package org.objectweb.celtix.context;

import java.lang.reflect.Method;
import javax.xml.ws.handler.MessageContext;

public class ObjectMessageContextImpl extends GenericMessageContext implements ObjectMessageContext {
    private static final long serialVersionUID = 401275179632507389L;

    public Object[] getMessageObjects() {
        return (Object[])get(ObjectMessageContext.METHOD_PARAMETERS);
    }

    public void setMessageObjects(Object... objects) {
        put(ObjectMessageContext.METHOD_PARAMETERS, objects);
        setScope(ObjectMessageContext.METHOD_PARAMETERS, MessageContext.Scope.HANDLER);
    }
    

    public void setReturn(Object retVal) {
        put(ObjectMessageContext.METHOD_RETURN, retVal);
        setScope(ObjectMessageContext.METHOD_RETURN, MessageContext.Scope.HANDLER);
    }

    public Object getReturn() {
        return get(ObjectMessageContext.METHOD_RETURN);
    }
    
    public void setMethod(Method method) {
        put(ObjectMessageContext.METHOD_OBJ, method);
        setScope(ObjectMessageContext.METHOD_OBJ, MessageContext.Scope.HANDLER);
    }

    public Method getMethod() {
        return (Method)get(ObjectMessageContext.METHOD_OBJ);
    }

    public void setException(Throwable ex) {
        put(ObjectMessageContext.METHOD_FAULT, ex);
        setScope(ObjectMessageContext.METHOD_FAULT, MessageContext.Scope.HANDLER);
    }
    
    public Throwable getException() {
        return (Throwable)get(ObjectMessageContext.METHOD_FAULT);
    }    

    public void setRequestorRole(boolean requestor) {
        put(ObjectMessageContext.REQUESTOR_ROLE_PROPERTY, Boolean.valueOf(requestor));
        setScope(ObjectMessageContext.REQUESTOR_ROLE_PROPERTY, MessageContext.Scope.HANDLER);
    }
    
    public boolean isRequestorRole() {
        Boolean b = (Boolean)get(ObjectMessageContext.REQUESTOR_ROLE_PROPERTY); 
        return null == b ? true : b.booleanValue();
    }
}

