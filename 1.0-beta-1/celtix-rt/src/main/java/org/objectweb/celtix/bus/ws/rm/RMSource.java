package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.wsrm.SequenceTerminationPolicyType;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.buslifecycle.BusLifeCycleListener;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.workqueue.WorkQueue;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;

public class RMSource extends RMEndpoint {

    private static final Logger LOG = LogUtils.getL7dLogger(RMSource.class);
    private static final String SOURCE_POLICIES_PROPERTY_NAME = "sourcePolicies";
    private static final String REQUESTOR_SEQUENCE_ID = "";
    private Map<String, Sequence> current; 
    private final RetransmissionQueue retransmissionQueue;

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
        current = new HashMap<String, Sequence>();
        
        retransmissionQueue = new RetransmissionQueue(getRMAssertion());
        
        retransmissionQueue.start(workQueue);
       
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
    Sequence getCurrent() {
        return getCurrent(null);
    }
    
    /**
     * Sets the current sequence used by a client side source.
     * @param s the current sequence.
     */
    void setCurrent(Sequence s) {
        setCurrent(null, s);
    }
    
    /**
     * Returns the current sequence used by a server side source for responses to a message
     * sent as part of the inbound sequence with the specified identifier.
     * 
     * @return the current sequence.
     */
    Sequence getCurrent(Identifier i) {        
        return current.get(i == null ? REQUESTOR_SEQUENCE_ID : i.getValue());
    }
    
    /**
     * Sets the current sequence used by a server side source for responses to a message
     * sent as part of the inbound sequence with the specified identifier.
     * @param s the current sequence.
     */
    void setCurrent(Identifier i, Sequence s) {        
        current.put(i == null ? REQUESTOR_SEQUENCE_ID : i.getValue(), s);
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
}
