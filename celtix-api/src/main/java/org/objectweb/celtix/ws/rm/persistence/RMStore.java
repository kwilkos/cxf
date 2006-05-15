package org.objectweb.celtix.ws.rm.persistence;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import org.objectweb.celtix.ws.rm.Identifier;


public interface RMStore {
   
    /**
     * Initialises the store.
     * @param params the store initialisation parameters
     */
    void init(Map<String, String> params);
    
    /**
     * Create a source sequence in the persistent store, with the sequence attributes as specified in the
     * <code>RMSourceSequence</code> object.
     * @param seq the sequence
     */
    void createSourceSequence(RMSourceSequence seq);
    
    /**
     * Create a destination sequence in the persistent store, with the sequence attributes as specified in the
     * <code>RMSDestinationSequence</code> object.
     * @param seq the sequence
     */
    void createDestinationSequence(RMDestinationSequence seq);
    
    /**
     * Remove the source sequence with the specified identifier from persistent store. 
     * @param seq the sequence
     */
    void removeSourceSequence(Identifier seq);
    
    /**
     * Remove the destination sequence with the specified identifier from persistent store. 
     * @param seq the sequence
     */
    void removeDestinationSequence(Identifier seq);
    
    /**
     * Retrieves all sequences managed by the identified RM source endpoint 
     * from persistent store.
     * 
     * @param endpointIdentifier the identifier for the source
     * @return the collection of sequences
     */    
    Collection<RMSourceSequence> getSourceSequences(String endpointIdentifier);
    
    /**
     * Retrieves all sequences managed by the identified RM destination endpoint 
     * from persistent store.
     * 
     * @param endpointIdentifier the identifier for the destination
     * @return the collection of sequences
     */    
    Collection<RMDestinationSequence> getDestinationSequences(String endpointIdentifier);
    
    /**
     * Retrieves the outbound/inbound messages stored for the source/destination sequence with 
     * the given identifier.
     * @param sid the source sequence identifier
     * @param outbound true if the message is outbound
     * @return the collection of messages
     * * 
     */
    Collection<RMMessage> getMessages(Identifier sid, boolean outbound);
    
    /**
     * Called by an RM source upon processing an outbound message. The <code>RMMessage</code>
     * parameter is null for non application (RM protocol) messages.
     * 
     * @param seq the source sequence 
     * @param msg the outgoing message
     */
    void persistOutgoing(RMSourceSequence seq, RMMessage msg);
    
   /**
    * Called by an RM source upon processing an outbound message. The <code>RMMessage</code>
     * parameter is null for non application (RM protocol) messages.
     * 
    * @param seq the destination sequence
    * @param msg the incoming message
    */
    void persistIncoming(RMDestinationSequence seq, RMMessage msg);
  
    /**
     * Removes the messages with the given message numbers and identifiers from the store of
     * outbound/inbound messages.
     * 
     * @param sid the identifier of the source sequence
     * @param messageNrs the collection of message numbers
     * @param outbound true if the message is outbound
     */
    void removeMessages(Identifier sid, Collection<BigInteger> messageNrs, boolean outbound);
}
