package org.objectweb.celtix.ws.rm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.rm.persistence.RMStore;

public class RMSource extends RMEndpoint {

    private static final Logger LOG = LogUtils.getL7dLogger(RMSource.class);
    private static final String REQUESTOR_SEQUENCE_ID = "";
    
    private Map<String, SourceSequence> map;
    private Map<String, SourceSequence> current;     
    private Lock sequenceCreationLock;
    private Condition sequenceCreationCondition;
    private boolean sequenceCreationNotified;


    RMSource(RMHandler h) {
        super(h);
        map = new HashMap<String, SourceSequence>();
        current = new HashMap<String, SourceSequence>();
             
        sequenceCreationLock = new ReentrantLock();
        sequenceCreationCondition = sequenceCreationLock.newCondition();
    }
    
    public SourceSequence getSequence(Identifier id) {        
        return map.get(id.getValue());
    }
    
    public void addSequence(SourceSequence seq) { 
        addSequence(seq, true);
    }
    
    public void addSequence(SourceSequence seq, boolean persist) {
        seq.setSource(this);
        map.put(seq.getIdentifier().getValue(), seq);
        if (persist) {
            RMStore store = getHandler().getStore();
            if (null != store) {
                store.createSourceSequence(seq);
            }
        }
    }
    
    public void removeSequence(SourceSequence seq) {        
        map.remove(seq.getIdentifier().getValue());
        RMStore store = getHandler().getStore();
        if (null != store) {
            store.removeSourceSequence(seq.getIdentifier());
        }
    }
    
    public Collection<SourceSequence> getAllSequences() {         
        return map.values();
    }
    

    

    /**
     * Returns the current sequence used by a client side source.
     * 
     * @return the current sequence.
     */
    SourceSequence getCurrent() {
        return getCurrent(null);
    }
    
    /**
     * Sets the current sequence used by a client side source.
     * @param s the current sequence.
     */
    void setCurrent(SourceSequence s) {
        setCurrent(null, s);
    }
    
    /**
     * Returns the current sequence used by a server side source for responses to a message
     * sent as part of the inbound sequence with the specified identifier.
     * 
     * @return the current sequence.
     */
    SourceSequence getCurrent(Identifier i) {        
        sequenceCreationLock.lock();
        try {
            return getAssociatedSequence(i);
        } finally {
            sequenceCreationLock.unlock();
        }
    }

    /**
     * Returns the sequence associated with the given identifier.
     * 
     * @param i the corresponding sequence identifier
     * @return the associated sequence
     * @pre the sequenceCreationLock is already held
     */
    SourceSequence getAssociatedSequence(Identifier i) {        
        return current.get(i == null ? REQUESTOR_SEQUENCE_ID : i.getValue());
    }
    
    /**
     * Await the availability of a sequence corresponding to the given identifier.
     * 
     * @param i the sequence identifier
     * @return
     */
    SourceSequence awaitCurrent(Identifier i) {
        sequenceCreationLock.lock();
        try {
            SourceSequence seq = getAssociatedSequence(i);
            while (seq == null) {
                while (!sequenceCreationNotified) {
                    try {
                        sequenceCreationCondition.await();
                    } catch (InterruptedException ie) {
                        // ignore
                    }
                }
                seq = getAssociatedSequence(i);
            }
            return seq;
        } finally {
            sequenceCreationLock.unlock();
        }
    }
    
    /**
     * Sets the current sequence used by a server side source for responses to a message
     * sent as part of the inbound sequence with the specified identifier.
     * @param s the current sequence.
     */
    void setCurrent(Identifier i, SourceSequence s) {        
        sequenceCreationLock.lock();
        try {
            current.put(i == null ? REQUESTOR_SEQUENCE_ID : i.getValue(), s);
            sequenceCreationNotified = true;
            sequenceCreationCondition.signal();
        } finally {
            sequenceCreationLock.unlock();
        }
    }

    /**
     * Stores the received acknowledgment in the Sequence object identified in
     * the <code>SequenceAcknowldgement</code> parameter. Then purges any
     * acknowledged messages from the retransmission queue and requests sequence
     * termination if necessary.
     * 
     * @param acknowledgment
     */
    public void setAcknowledged(SequenceAcknowledgement acknowledgment) {
        Identifier sid = acknowledgment.getIdentifier();
        SourceSequence seq = getSequence(sid);        
        if (null != seq) {
            seq.setAcknowledged(acknowledgment);
            getHandler().getPersistenceManager().getQueue().purgeAcknowledged(seq);
            if (seq.allAcknowledged()) {
                try {
                    getHandler().getProxy().terminateSequence(seq); 
                } catch (IOException ex) {
                    Message msg = new Message("SEQ_TERMINATION_FAILURE", LOG, seq.getIdentifier());
                    LOG.log(Level.SEVERE, msg.toString(), ex);
                }
            }
        }
    }
    
    /**
     * Returns a collection of all sequences for which have not yet been
     * completely acknowledged.
     * 
     * @return the collection of unacknowledged sequences.
     */
    public Collection<SourceSequence> getAllUnacknowledgedSequences() {
        Collection<SourceSequence> seqs = new ArrayList<SourceSequence>();
        for (SourceSequence seq : map.values()) {
            if (!seq.allAcknowledged()) {
                seqs.add(seq);
            }
        }
        return seqs;        
    }
}
