package org.objectweb.celtix.channels;

import java.util.concurrent.Future;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.message.MessageListener;

public interface Channel {
    void send(Message message);
    
    <T> Future<T> sendAsync(Message message);
    
    void setMessageListener(MessageListener listener);
}
