package org.objectweb.celtix.context;

import java.lang.reflect.Method;
import javax.xml.ws.handler.MessageContext;

public interface ObjectMessageContext extends MessageContext {

    String MESSAGE_INPUT = "org.objectweb.celtix.input";
    String MESSAGE_PAYLOAD = "org.objectweb.celtix.payload";
    String REQUEST_PROXY = "org.objectweb.celtix.proxy"; 
    String REQUESTOR_ROLE_PROPERTY = "org.objectweb.celtix.role.requestor";
    String METHOD_OBJ = "org.objectweb.celtix.method";
    String METHOD_PARAMETERS = "org.objectweb.celtix.parameters";
    String METHOD_MESSAGE = "org.objectweb.celtix.method.message";
    String METHOD_PAYLOAD = "org.objectweb.celtix.method.payload";
    String METHOD_RETURN = "org.objectweb.celtix.return";
    String METHOD_FAULT = "org.objectweb.celtix.fault";
    String CORRELATION_OUT = "org.objectweb.celtix.correlation.out";
    String CORRELATION_IN = "org.objectweb.celtix.correlation.in";
    
    Object[] getMessageObjects();
    
    void setMessageObjects(Object ... objects);

    void setMethod(Method method);
    
    Method getMethod();
    
    void setReturn(Object retVal);
    
    Object getReturn();
    
    void setException(Throwable retVal);
    
    Throwable getException();

    void setRequestorRole(boolean requestor);
    
    boolean isRequestorRole();
}


