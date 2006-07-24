package org.objectweb.celtix.interceptors;

import java.util.ListIterator;

import org.objectweb.celtix.message.Message;

public interface InterceptorChain  {
    
    enum State {
        PAUSED,
        EXECUTING,
        COMPLETE,
        ABORTED
    };
    
    void add(Interceptor i);
    
    void remove(Interceptor i);
    
    boolean doIntercept(Message message);
    
    void pause();
    
    void resume();
    
    ListIterator<Interceptor> getIterator();

}
