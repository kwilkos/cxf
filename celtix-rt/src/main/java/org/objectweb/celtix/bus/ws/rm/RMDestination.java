package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.bus.configuration.wsrm.AcksPolicyType;
import org.objectweb.celtix.bus.configuration.wsrm.DestinationPolicyType;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

public class RMDestination extends RMEndpoint {

    private static final Logger LOG = LogUtils.getL7dLogger(RMDestination.class);
    private static final String DESTINATION_POLICIES_PROPERTY_NAME = "destinationPolicies";
    
    RMDestination(RMHandler h) {
        super(h);
    }
    
    /**
     * Called by the RM destination when no sequence with the given identifier
     * exists.
     */
    public void addSequence(Identifier id, EndpointReferenceType a) {
        Sequence seq = new Sequence(id, this, a);
        addSequence(seq);
    }
    
    
    public DestinationPolicyType getDestinationPolicies() {
        DestinationPolicyType dp = getHandler().getConfiguration()
            .getObject(DestinationPolicyType.class, DESTINATION_POLICIES_PROPERTY_NAME);
        if (null == dp) {
            dp = RMUtils.getWSRMConfFactory().createDestinationPolicyType();
        }
        return dp;
    }
    
    public AcksPolicyType getAcksPolicy() {
        DestinationPolicyType dp = getDestinationPolicies();
        assert null != dp;
        AcksPolicyType ap = dp.getAcksPolicy();
        if (null == ap) {
            ap = RMUtils.getWSRMConfFactory().createAcksPolicyType();
        }
        return ap;
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
        Sequence seq = getSequence(sequenceType.getIdentifier());
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
                        Message msg = new Message("SEQ_ACKNOWLEDGMENT_FAILURE", LOG, seq);
                        LOG.log(Level.SEVERE, msg.toString(), ex);
                    }
                }
            }
        } else {
            throw Sequence.createUnknownSequenceFault(sequenceType.getIdentifier());
        }
    }
}
