package org.objectweb.celtix.bindings;

import java.util.Collection;

import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.message.Message;

public interface Binding {
    
    Collection<Interceptor> getInInterceptors();
    
    Collection<Interceptor> getOutInterceptors();
    
    Collection<Interceptor> getFaultInterceptors();
    
    Message createMessage();
    
    javax.xml.ws.Binding createBinding();
    
}
