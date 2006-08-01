package org.objectweb.celtix.message;

import java.util.HashMap;

import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.Destination;

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
    }

    public void setConduit(Conduit c) {
        conduit = c;
    }

    public void setOutMessage(Message m) {
        outMessage = m;
    }

}
