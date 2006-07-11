package org.objectweb.celtix.message;

import java.util.Map;

import org.objectweb.celtix.channels.Channel;

public interface Exchange extends Map<String, Object> {
    Message getInMessage();
    void setInMessage(Message m);
    
    Message getOutMessage();
    void setOutMessage(Message m);
    
    Channel getInChannel();
    void setInChannel(Channel c);
    
    Channel getOutChannel();
    void setOutChannel(Channel c);
}
