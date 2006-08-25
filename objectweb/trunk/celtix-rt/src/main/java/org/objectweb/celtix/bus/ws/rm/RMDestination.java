package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceType;
import org.objectweb.celtix.ws.rm.persistence.RMStore;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

public class RMDestination extends RMEndpoint {

    private static final Logger LOG = LogUtils.getL7dLogger(RMDestination.class);
    
  
    private Map<String, DestinationSequence> map;
    
    RMDestination(RMHandler h) {
        super(h);
        map = new HashMap<String, DestinationSequence>();    
    }
    
    public DestinationSequence getSequence(Identifier id) {        
        return map.get(id.getValue());
    }
    
    public void addSequence(DestinationSequence seq) {
        addSequence(seq, true);
    }
    
    public void addSequence(DestinationSequence seq, boolean persist) {  
        seq.setDestination(this);
        map.put(seq.getIdentifier().getValue(), seq);
        if (persist) {
            RMStore store = getHandler().getStore();
            if (null != store) {
                store.createDestinationSequence(seq);
            }
        }
    }
    
    public void removeSequence(DestinationSequence seq) {        
        map.remove(seq.getIdentifier().getValue());
        RMStore store = getHandler().getStore();
        if (null != store) {
            store.removeDestinationSequence(seq.getIdentifier());
        }
    }
    
    public Collection<DestinationSequence> getAllSequences() {        
        return map.values();
    }

  
    
   /**
    * Acknowledges receipt of a message. If the message is the last in the sequence, 
    * sends an out-of-band SequenceAcknowledgement unless there a response will be sent
    * to the acksTo address onto which the acknowldegment can be piggybacked. 
    *  
    * @param sequenceType the sequenceType object that includes identifier and message number
    * (and possibly a lastMessage element) for the message to be acknowledged)
    * @param replyToAddress the replyTo address of the message that carried this sequence information
    * @throws SequenceFault if the sequence specified in <code>sequenceType</code> does not exist
    */
    public void acknowledge(SequenceType sequenceType, String replyToAddress) 
        throws SequenceFault {
        DestinationSequence seq = getSequence(sequenceType.getIdentifier());
        if (null != seq) {
            seq.acknowledge(sequenceType.getMessageNumber());
            
            if (null != sequenceType.getLastMessage()) {
                
                seq.setLastMessageNumber(sequenceType.getMessageNumber());
                
                seq.scheduleImmediateAcknowledgement();
                
                // if we cannot expect an outgoing message to which the acknowledgement
                // can be added we need to send an out-of-band SequenceAcknowledgement message
           
                if (!(seq.getAcksTo().getAddress().getValue().equals(replyToAddress)
                    || seq.canPiggybackAckOnPartialResponse())) {
                    try {
                        getHandler().getProxy().acknowledge(seq);
                    } catch (IOException ex) {
                        Message msg = new Message("SEQ_ACK_SEND_EXC", LOG, seq);
                        LOG.log(Level.SEVERE, msg.toString(), ex);
                    }
                }
            }
        } else {
            throw DestinationSequence.createUnknownSequenceFault(sequenceType.getIdentifier());
        }
    }
}
