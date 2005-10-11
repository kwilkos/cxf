package org.objectweb.celtix.context;

import java.lang.reflect.Method;
import javax.xml.ws.handler.MessageContext;

public interface ObjectMessageContext extends MessageContext {

    String MESSAGE_INPUT = "org.objectweb.celtix.input";
    
    Object[] getMessageObjects();
    
    void setMessageObjects(Object ... objects);

    void setMethod(Method method);
    
    Method getMethod();
    
    void setReturn(Object retVal);
    
    Object getReturn();
    
    void setException(Object retVal);
    
    Object getException();
}


