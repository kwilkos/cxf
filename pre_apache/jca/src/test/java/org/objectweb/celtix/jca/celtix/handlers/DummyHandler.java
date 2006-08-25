package org.objectweb.celtix.jca.celtix.handlers; 

import java.lang.reflect.Method;

import org.objectweb.celtix.jca.celtix.CeltixInvocationHandler;
import org.objectweb.celtix.jca.celtix.CeltixInvocationHandlerData;


public class DummyHandler implements CeltixInvocationHandler { 

    boolean invokeCalled; 

    public CeltixInvocationHandlerData getData() {
        return new CeltixInvocationHandlerDataImpl();
    }
   
    // Implementation of java.lang.reflect.InvocationHandler

    public final Object invoke(final Object object, 
                               final Method method, 
                               final Object[] objectArray) throws Throwable {
        invokeCalled = true; 
        return null;
    }


    public void setNext(CeltixInvocationHandler next) {
        
    }


    public CeltixInvocationHandler getNext() {
        return null;
    }
    
}
