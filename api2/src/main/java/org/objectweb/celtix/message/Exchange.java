package org.objectweb.celtix.message;

import java.util.Map;

import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.Destination;

public interface Exchange extends Map<String, Object> {
    Message getInMessage();
    void setInMessage(Message m);
    
    Message getOutMessage();
    void setOutMessage(Message m);
    
    /**
     * @return the associated incoming Destination (may be anonymous)
     */
    Destination getDestination();
    
    /**
     * @param destination the associated incoming Destination
     */    
    void setDestination(Destination destination);

    /**
     * @return the associated outgoing Conduit (may be anonymous)
     */
    Conduit getConduit();

    /**
     * @param conduit the associated outgoing Conduit 
     */
    void setConduit(Conduit conduit);
}
