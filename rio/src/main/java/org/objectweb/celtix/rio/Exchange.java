package org.objectweb.celtix.rio;

import java.util.Map;

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
