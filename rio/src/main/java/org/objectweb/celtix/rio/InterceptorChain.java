package org.objectweb.celtix.rio;

public interface InterceptorChain  {
    
    void add(Interceptor i);
    
    void remove(Interceptor i);
    
    /**
     * Executes the next filter in the chain.
     * @param message
     */
    void doIntercept(Message message);

}
