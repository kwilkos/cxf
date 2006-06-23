package org.objectweb.celtix.ws.rm;

import java.math.BigInteger;

import org.objectweb.celtix.ws.rm.SequenceAcknowledgement.AcknowledgementRange;

public abstract class AbstractSequenceImpl {
    
    protected final Identifier id;
    protected SequenceAcknowledgement acked;
    
    protected AbstractSequenceImpl(Identifier i) {
        id = i;
    }
    
    /**
     * @return the sequence identifier
     */
    public Identifier getIdentifier() {
        return id;
    }
    
    public String toString() {
        return id.getValue();
    }
    
    public boolean equals(Object other) {
        if (other == this) {
            return true;            
        }
        if (other instanceof AbstractSequenceImpl) {
            AbstractSequenceImpl otherSeq = (AbstractSequenceImpl)other;
            return otherSeq.getIdentifier().getValue().equals(getIdentifier().getValue());
        }        
        return false;
    }
    
    public int hashCode() {
        return getIdentifier().getValue().hashCode();
    }
    
    static boolean identifierEquals(Identifier id1, Identifier id2) {
        if (null == id1) {
            return null == id2;
        } else {
            return null != id2 && id1.getValue().equals(id2.getValue());
        }
    }
    
    public synchronized boolean isAcknowledged(BigInteger m) {
        for (AcknowledgementRange r : acked.getAcknowledgementRange()) {
            if (m.subtract(r.getLower()).signum() >= 0 && r.getUpper().subtract(m).signum() >= 0) {
                return true;
            }
        }
        return false;
    }
   
}
