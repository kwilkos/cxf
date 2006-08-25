package org.apache.cxf.interceptor;

import java.util.ListIterator;

import org.apache.cxf.message.Message;

public interface InterceptorChain extends Iterable<Interceptor<? extends Message>> {
    
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
    
    ListIterator<Interceptor<? extends Message>> getIterator();

}
