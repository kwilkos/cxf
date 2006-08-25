package org.apache.cxf.message;

import java.util.HashMap;

import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;

public class ExchangeImpl extends HashMap<String, Object> implements Exchange {

    private Destination destination;
    private Conduit conduit;
    
    private Message inMessage;
    private Message outMessage;
    
    
    public Destination getDestination() {
        return destination;
    }

    public Message getInMessage() {
        return inMessage;
    }

    public Conduit getConduit() {
        return conduit;
    }

    public Message getOutMessage() {
        return outMessage;
    }

    public void setDestination(Destination d) {
        destination = d;
    }

    public void setInMessage(Message m) {
        inMessage = m;
        m.setExchange(this);
    }

    public void setConduit(Conduit c) {
        conduit = c;
    }

    public void setOutMessage(Message m) {
        outMessage = m;
        m.setExchange(this);
    }
    
    public <T> T get(Class<T> key) {
        return key.cast(get(key.getName()));
    }

    public <T> void put(Class<T> key, T value) {
        put(key.getName(), value);
    }

}
