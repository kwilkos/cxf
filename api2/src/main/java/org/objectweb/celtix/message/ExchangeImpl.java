package org.objectweb.celtix.message;

import java.util.HashMap;

import org.objectweb.celtix.channels.Channel;

public class ExchangeImpl extends HashMap<String, Object> implements Exchange {

    private Channel inChannel;
    private Channel outChannel;
    
    private Message inMessage;
    private Message outMessage;
    
    
    public Channel getInChannel() {
        return inChannel;
    }

    public Message getInMessage() {
        return inMessage;
    }

    public Channel getOutChannel() {
        return outChannel;
    }

    public Message getOutMessage() {
        return outMessage;
    }

    public void setInChannel(Channel c) {
        inChannel = c;
        
    }

    public void setInMessage(Message m) {
        inMessage = m;
    }

    public void setOutChannel(Channel c) {
        outChannel = c;
    }

    public void setOutMessage(Message m) {
        outMessage = m;
    }

}
