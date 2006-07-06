package org.objectweb.celtix.jca.celtix;

import java.lang.reflect.InvocationHandler;

public interface CeltixInvocationHandler extends InvocationHandler {

    void setNext(CeltixInvocationHandler next); 
    CeltixInvocationHandler getNext(); 

    CeltixInvocationHandlerData getData(); 
}
