package org.apache.cxf.binding;

import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.message.Message;

public interface Binding extends InterceptorProvider {
    
    Message createMessage();

    Message createMessage(Message m);
    
}
