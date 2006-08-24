package org.apache.cxf.bindings;

import org.apache.cxf.interceptors.InterceptorProvider;
import org.apache.cxf.message.Message;

public interface Binding extends InterceptorProvider {
    
    Message createMessage();

    Message createMessage(Message m);
    
}
