package org.objectweb.celtix.ws.rm;

import java.util.ArrayList;
import java.util.Collection;

public class RMPropertiesImpl implements RMProperties {
    private SequenceType sequence;
    private Collection<SequenceAcknowledgement> acks;
    private Collection<AckRequestedType> acksRequested;
    
    public Collection<SequenceAcknowledgement> getAcks() {
        return acks;
    }
    
    public Collection<AckRequestedType> getAcksRequested() {
        return acksRequested;
    }
    
    public SequenceType getSequence() {
        return sequence;
    }
    
    public void setAcks(Collection<SequenceAcknowledgement> a) {
        acks = a;
    }
    
    public void setAcksRequested(Collection<AckRequestedType> ar) {
        acksRequested = ar;       
    }
    
    public void setSequence(SequenceType s) {
        sequence = s;
    }
    
    protected void setSequence(SourceSequence seq) {
        SequenceType s = RMUtils.getWSRMFactory().createSequenceType();
        s.setIdentifier(seq.getIdentifier());
        s.setMessageNumber(seq.getCurrentMessageNr());   
        if (seq.isLastMessage()) {
            s.setLastMessage(new SequenceType.LastMessage());
        }
        setSequence(s);
    }
    
    protected void addAck(DestinationSequence seq) {
        if (null == acks) {
            acks = new ArrayList<SequenceAcknowledgement>();
        }
        SequenceAcknowledgement ack = seq.getAcknowledgment();
        acks.add(ack);
        seq.acknowledgmentSent();
    }
  
}
