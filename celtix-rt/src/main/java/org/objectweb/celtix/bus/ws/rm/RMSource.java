package org.objectweb.celtix.bus.ws.rm;

import java.util.HashSet;
import java.util.Set;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.wsrm.SequenceTerminationPolicyType;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.buslifecycle.BusLifeCycleListener;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.workqueue.WorkQueue;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;

public class RMSource extends RMEndpoint {

    private static final String SOURCE_POLICIES_PROPERTY_NAME = "sourcePolicies";
    private Sequence current;
    private final RetransmissionQueue retransmissionQueue;
    private Set<Identifier> offeredIdentifiers;

    RMSource(RMHandler h) {
        super(h);
        Bus bus = h.getBinding().getBus();
        WorkQueue workQueue =
            bus.getWorkQueueManager().getAutomaticWorkQueue();
        bus.getLifeCycleManager().registerLifeCycleListener(new BusLifeCycleListener() {
            public void initComplete() {      
            }
            public void postShutdown() {      
            }
            public void preShutdown() {
                shutdown();
            }
        });
        retransmissionQueue = new RetransmissionQueue();
        retransmissionQueue.start(workQueue);
        offeredIdentifiers = new HashSet<Identifier>();
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
     * Returns the current sequence
     * 
     * @return the current sequence.
     */
    Sequence getCurrent() {
        return current;
    }

    /**
     * Stores the sequence under its sequence identifier. and makes this the 
     * current sequence.
     * 
     * @param id the sequence identifier.
     * @param seq the sequence.
     */
    public void addSequence(Sequence seq) {
        
        super.addSequence(seq);
        current = seq;
    }

    /**
     * Create a copy of the message, store it in the retransmission queue and
     * schedule the next transmission
     * 
     * @param context
     */
    public void addUnacknowledged(MessageContext context) {

        ObjectMessageContext clone = getHandler().getBinding().createObjectContext();
        clone.putAll(context);
        getRetransmissionQueue().cacheUnacknowledged(clone);
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
        Sequence seq = getSequence(sid);        
        if (null != seq) {
            seq.setAcknowledged(acknowledgment);
            retransmissionQueue.purgeAcknowledged(seq);
        }
    }

    public Identifier offer() {
        Identifier sid = generateSequenceIdentifier();
        offeredIdentifiers.add(sid);
        return sid;
    }
    
    public void shutdown() {
        retransmissionQueue.shutdown();
    }
}
