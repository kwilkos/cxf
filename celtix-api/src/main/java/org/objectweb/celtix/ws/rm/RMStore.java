package org.objectweb.celtix.ws.rm;

import java.math.BigInteger;
import java.util.Collection;

public interface RMStore {
    
    // transaction demarcation
    
    void beginTransaction();
    
    void commit();
    
    void abort();
    
    // sequences
   
    /**
     * Create a sequence in the persistent store, with the sequence attributes as specified in the
     * <code>RMSequence</code> object.
     * @param seq the sequence
     */
    void createSequence(RMSequence seq);
    
    /**
     * Remove the sequence identified by the <code>RMSequence</code> object from the persistent 
     * store. 
     * @param seq the sequence
     */
    void removeSequence(Identifier seq);
   
    /**
     * Update the sequence identified by the <code>RMSequence</code> object in the persistent store.  
     * @param seq the sequence
     */
    void updateSequence(RMSequence seq);
    
    /**
     * Retrieves all sequences managed by the identified endpoint (source or
     * destination) from persistent store.
     * 
     * @param destination true if the endpoint is an RM destination
     * @param endpointIdentifier the identifier for the endpoint
     * @return the collection of sequences
     */    
    Collection<RMSequence> getSequences(boolean destination, String endpointIdentifier);
    
    // messages 
    
    /**
     * Stores the message presented by the <code>RMMessage</code> object in the persistent store.
     * 
     * @param sid the sequence identifier
     * @param msg the message
     */
    void storeMessage(Identifier sid, RMMessage msg);
    
    /**
     * Removes the message with the given message in the given sequence from the persistent store.
     * 
     * @param sid the sequence identifier
     * @param messageNr the message number
     */
    void removeMessage(Identifier sid, BigInteger messageNr);
    
    /**
     * Retrieves the messages stored for the given sequence.
     * @param sid the sequence identifier
     * @return the collection of messages
     */
    Collection<RMMessage> getMessages(Identifier sid);
}
