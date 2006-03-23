package org.objectweb.celtix.bus.ws.rm;

import java.util.ArrayList;
import java.util.Collection;

import org.objectweb.celtix.ws.rm.AckRequestedType;
import org.objectweb.celtix.ws.rm.RMProperties;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceType;

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
    
    protected void setSequence(Sequence seq) {
        SequenceType s = RMUtils.getWSRMFactory().createSequenceType();
        s.setIdentifier(seq.getIdentifier());
        s.setMessageNumber(seq.getCurrentMessageNumber());   
        if (seq.getCurrentMessageNumber().equals(seq.getLastMessageNumber())) {
            s.setLastMessage(new SequenceType.LastMessage());
        }
        setSequence(s);
    }
    
    protected void addAck(Sequence seq) {
        if (null == acks) {
            acks = new ArrayList<SequenceAcknowledgement>();
        }
        SequenceAcknowledgement ack = seq.getAcknowledged();
        acks.add(ack);
        seq.acknowledgmentSent();
    }
    
    protected boolean matchSequence(Sequence seq) {
        return null != sequence 
            && sequence.getIdentifier().getValue().equals(seq.getIdentifier().getValue());
    }
    
    
    
    
    
}
