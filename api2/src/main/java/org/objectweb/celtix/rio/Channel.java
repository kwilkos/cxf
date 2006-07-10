package org.objectweb.celtix.rio;

import java.util.concurrent.Future;

public interface Channel {
    void send(Message message);
    
    <T> Future<T> sendAsync(Message message);
    
    void setMessageListener(MessageListener listener);
}
