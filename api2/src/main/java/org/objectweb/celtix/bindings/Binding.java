package org.objectweb.celtix.bindings;

import org.objectweb.celtix.interceptors.InterceptorProvider;
import org.objectweb.celtix.message.Message;

public interface Binding extends InterceptorProvider {
    
    Message createMessage();
    
}
