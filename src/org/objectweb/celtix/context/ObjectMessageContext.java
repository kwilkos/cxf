package org.objectweb.celtix.context;

import java.lang.reflect.Method;
import javax.xml.ws.handler.MessageContext;

public interface ObjectMessageContext extends MessageContext {
    
    Object[] getMessageObjects();
    
    void setMessageObjects(Object ... objects);

    void setMethod(Method method);
    
    Method getMethod();
}
