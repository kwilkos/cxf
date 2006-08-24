package org.apache.cxf.message;

import java.util.Map;

import org.apache.cxf.messaging.Conduit;
import org.apache.cxf.messaging.Destination;

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
    
    
    /**
     * Convienience method for storing/retrieving typed objects from the map.
     * equivilent to:  (T)get(key.getName());
     * @param <T> key
     * @return
     */
    <T> T get(Class<T> key);
    /**
     * Convienience method for storing/retrieving typed objects from the map.
     * equivilent to:  put(key.getName(), value);
     * @param <T> key
     * @return
     */
    <T> void put(Class<T> key, T value);
}
