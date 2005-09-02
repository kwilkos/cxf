package org.objectweb.celtix.bindings;

import java.lang.reflect.Method;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;

public class ObjectMessageContextImpl extends GenericMessageContext implements ObjectMessageContext {
    public static final String METHOD_INVOKED = "org.objectweb.celtix.method";
    public static final String METHOD_PARAMETERS = "org.objectweb.celtix.parameters";
    private static final long serialVersionUID = 401275179632507389L;

    public Object[] getMessageObjects() {
        return (Object[])get(METHOD_PARAMETERS);
    }

    public void setMessageObjects(Object... objects) {
        put(METHOD_PARAMETERS, (Object)objects);
        setScope(METHOD_PARAMETERS, MessageContext.Scope.HANDLER);
    }

    public void setMethod(Method method) {
        put(METHOD_INVOKED, method);
        setScope(METHOD_INVOKED, MessageContext.Scope.HANDLER);
    }

    public Method getMethod() {
        return (Method) get(METHOD_INVOKED);
    }
}
