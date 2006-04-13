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
     * Create a sequence in the persistent store, with the sequence attributes as specified in the
     * <code>RMSequence</code> object.
     * @param seq the sequence
     */
    void createSequence(RMSourceSequence seq);
    
    /**
     * Remove the sequence identified by the <code>RMSequence</code> object from the persistent 
     * store. 
     * @param seq the sequence
     */
    void removeSequence(Identifier seq);
    
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
     * Retrieves the outbound messages stored for the source sequence with the given identifier.
     * @param sid the source sequence identifier
     * @return the collection of messages
     */
    Collection<RMMessage> getOutboundMessages(Identifier sid);
    
    /**
     * Retrieves the inbound messages stored for the destination sequence with the given identifier.
     * @param sid the destination sequence identifier
     * @return the collection of messages
     */
    Collection<RMMessage> getInboundMessages(Identifier sid);
    
    /**
     * Called by an RM source upon processing an outbound message. The <code>RMMessage</code>
     * parameter is null for non application (RM protocol) messages.
     * 
     * @param srcSequenceId the identifier of the source sequence
     * @param lastMessage true if the message is the last message in the sequence
     * @param msg the outgoing message
     */
    void persistOutgoing(Identifier srcSequenceId, boolean lastMessage, RMMessage msg);
    
   /**
    * Called by an RM source upon processing an outbound message. The <code>RMMessage</code>
     * parameter is null for non application (RM protocol) messages.
     * 
    * @param destSequenceId the identifier of the destination sequence
    * @param lastMessage true if the message is the last message in the sequence
    * @param msg the incoming message
    */
    void persistIncoming(Identifier destSequenceId, boolean lastMessage, RMMessage msg);
  
    /**
     * Removes the messages with the given message numbers from the specified source
     * sequence.
     * 
     * @param sid the identifier of the source sequence
     * @param messageNr the collection of message numbers
     */
    void removeMessage(Identifier sid, Collection<BigInteger> messageNr);
}
