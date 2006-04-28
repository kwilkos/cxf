package org.objectweb.celtix.bus.ws.rm;

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


import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.wsrm.SequenceTerminationPolicyType;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.buslifecycle.BusLifeCycleListener;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.persistence.RMMessage;
import org.objectweb.celtix.ws.rm.persistence.RMSourceSequence;
import org.objectweb.celtix.ws.rm.persistence.RMStore;

public class RMSource extends RMEndpoint {

    private static final Logger LOG = LogUtils.getL7dLogger(RMSource.class);
    private static final String SOURCE_POLICIES_PROPERTY_NAME = "sourcePolicies";
    private static final String REQUESTOR_SEQUENCE_ID = "";
    
    private Map<String, SourceSequence> map;
    private Map<String, SourceSequence> current;     
    private final RetransmissionQueue retransmissionQueue;
    private Lock sequenceCreationLock;
    private Condition sequenceCreationCondition;
    private boolean sequenceCreationNotified;


    RMSource(RMHandler h) {
        super(h);
        map = new HashMap<String, SourceSequence>();
        Bus bus = h.getBus();
        bus.getLifeCycleManager().registerLifeCycleListener(new BusLifeCycleListener() {
            public void initComplete() {      
            }
            public void postShutdown() {      
            }
            public void preShutdown() {
                shutdown();
            }
        });
        current = new HashMap<String, SourceSequence>();
        
        retransmissionQueue = new RetransmissionQueue(h, getRMAssertion());         
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
        LOG.fine("Adding source sequence: " + seq);
        seq.setSource(this);
        map.put(seq.getIdentifier().getValue(), seq);
        if (persist) {
            getHandler().getStore().createSourceSequence(seq);
        }
    }
    
    public void removeSequence(SourceSequence seq) {        
        map.remove(seq.getIdentifier().getValue());
        getHandler().getStore().removeSourceSequence(seq.getIdentifier());
    }
    
    public final Collection<SourceSequence> getAllSequences() {        
        return map.values();
    }
    

    public SourcePolicyType getSourcePolicies() {
        SourcePolicyType sp = (SourcePolicyType)getHandler().getConfiguration()
            .getObject(SourcePolicyType.class, SOURCE_POLICIES_PROPERTY_NAME);
        if (null == sp) {
            sp = RMUtils.getWSRMConfFactory().createSourcePolicyType();
        }
        return sp;
    }

    public SequenceTerminationPolicyType getSequenceTerminationPolicy() {
        SourcePolicyType sp = getSourcePolicies();
        assert null != sp;
        SequenceTerminationPolicyType stp = sp.getSequenceTerminationPolicy();
        if (null == stp) {
            stp = RMUtils.getWSRMConfFactory().createSequenceTerminationPolicyType();
        }
        return stp;
    }

    public RetransmissionQueue getRetransmissionQueue() {
        return retransmissionQueue;
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
     * Await the avilability of a sequence corresponding to the given identifier.
     * 
     * @param i the sequnce identifier
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
     * Create a copy of the message, store it in the retransmission queue and
     * schedule the next transmission
     * 
     * @param context
     */
    public void addUnacknowledged(SourceSequence seq, RMMessage msg) {
        ObjectMessageContext clone = getHandler().getBinding().createObjectContext();
        clone.putAll(msg.getContext());
        getRetransmissionQueue().cacheUnacknowledged(clone); 
        getHandler().getStore().persistOutgoing(seq, msg);
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
            retransmissionQueue.purgeAcknowledged(seq);
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
    
    public void shutdown() {
        retransmissionQueue.shutdown();
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
    
    void restore() {
        RMStore store = getHandler().getStore();
        
        Collection<RMSourceSequence> dss = store.getSourceSequences(getEndpointId());
        // Don't make any of these sequences the current sequence, thus forcing
        // termination of the recovered sequences as soon as possible
        for (RMSourceSequence ds : dss) {
            addSequence((SourceSequence)ds, false);
        }
        
        retransmissionQueue.populate(getAllSequences());
        int n = retransmissionQueue.getUnacknowledged().size();
        if (n > 0) {
            LOG.fine("Recovered " + n + " messages, start retransmission queue now");
            retransmissionQueue.start(getHandler().getBus().getWorkQueueManager().getAutomaticWorkQueue());
        } else {
            LOG.fine("No outgoing messages recovered");
        }
        
        
    }
}
