package org.objectweb.celtix.bus.ws.rm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.BindingContextUtils;
import org.objectweb.celtix.bindings.Request;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.workqueue.WorkQueue;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceType;

public class RetransmissionQueue {
    private static final Logger LOG = LogUtils.getL7dLogger(RetransmissionQueue.class);
    // temporary value to suppress spurious resends caused by 15 second delay
    // on the dispatch by Jetty of the first incoming HTTP request after a 
    // partial response
    private static final long DEFAULT_BASE_RETRANSMISSION_INTERVAL = 6 * 3000L;
    private static final int DEFAULT_EXPONENTIAL_BACKOFF = 2;

    private WorkQueue workQueue;
    private long baseRetransmissionInterval;
    private int exponentialBackoff;
    private Map<String, List<ResendCandidate>> candidates;
    private Runnable resendInitiator;
    private boolean shutdown;
    private Resender resender;
    
    /**
     * Constructor.
     */
    public RetransmissionQueue() {
        this(DEFAULT_BASE_RETRANSMISSION_INTERVAL, 
             DEFAULT_EXPONENTIAL_BACKOFF);
    }

    /**
     * Constructor.
     * 
     * @param base the base retransmission interval
     * @param backoff the exponential backoff
     */
    public RetransmissionQueue(long base,
                               int backoff) {
        baseRetransmissionInterval = base;
        exponentialBackoff = backoff;
        candidates = new HashMap<String, List<ResendCandidate>>();
        resender = getDefaultResender();
    }
    
    /**
     * Create default Resender logic.
     * 
     * @return default Resender
     */
    private Resender getDefaultResender() {   
        return new Resender() {
            public void resend(ObjectMessageContext context) {
                SequenceType st = RMContextUtils.retrieveSequence(context);
                if (st != null) {
                    LOG.log(Level.INFO, "RESEND_MSG", st.getMessageNumber());
                }
                try {
                    AddressingProperties maps = 
                        ContextUtils.retrieveMAPs(context, false, true);
                    String uuid = ContextUtils.generateUUID();
                    maps.setMessageID(ContextUtils.getAttributedURI(uuid));
                    AbstractBindingBase binding = (AbstractBindingBase)
                        BindingContextUtils.retrieveBinding(context);
                    Request request = new Request(binding, context);
                    request.setOneway(ContextUtils.isOneway(context));
                    OutputStreamMessageContext outputStreamContext =
                        request.process(null, true);
                    ClientTransport transport = 
                        BindingContextUtils.retrieveClientTransport(context);
                    if (request.isOneway()) {
                        transport.invokeOneway(outputStreamContext);
                    } else {
                        InputStreamMessageContext inputStreamContext =
                            transport.invoke(outputStreamContext);
                        // input stream context should be null due to
                        // decoupled response channel alway being used
                        // with RM
                        assert inputStreamContext == null;
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "RESEND_FAILED_MSG", e);
                }  
            }
        };
    };
    
    /**
     * Plug in replacement resend logic (facilitates unit testing).
     *  
     * @param replacement resend logic
     */
    protected void replaceResender(Resender replacement) {
        resender = replacement;    
    }
    
    /**
     * Initiate resends.
     * 
     * @param queue the work queue providing async execution
     */
    protected void start(WorkQueue queue) {
        workQueue = queue;
        workQueue.schedule(getResendInitiator(),
                           baseRetransmissionInterval);        
    }
    
    /**
     * Accepts a new resend candidate.
     * 
     * @param ctx the message context.
     * @return ResendCandidate
     */
    protected ResendCandidate cacheUnacknowledged(ObjectMessageContext ctx) {
        ResendCandidate candidate = null;
        SequenceType st = RMContextUtils.retrieveSequence(ctx);
        Identifier sid = st.getIdentifier();
        synchronized (this) {
            String key = sid.getValue();
            List<ResendCandidate> sequenceCandidates =
                getSequenceCandidates(key);
            if (null == sequenceCandidates) {
                sequenceCandidates = new ArrayList<ResendCandidate>();
                candidates.put(key, sequenceCandidates);
            }
            candidate = new ResendCandidate(ctx);
            sequenceCandidates.add(candidate);
        }
        return candidate;
    }

    /**
     * Purge all candidates for the given sequence that
     * have been acknowledged.
     * 
     * @param seq the sequence object.
     */
    protected synchronized void purgeAcknowledged(Sequence seq) {
        List<ResendCandidate> sequenceCandidates = getSequenceCandidates(seq);
        if (null != sequenceCandidates) {
            for (int i = sequenceCandidates.size() - 1; i >= 0; i--) {
                ResendCandidate candidate = sequenceCandidates.get(i);
                SequenceType st = 
                    RMContextUtils.retrieveSequence(candidate.getContext());
                BigInteger m = st.getMessageNumber();
                if (seq.isAcknowledged(m)) {
                    sequenceCandidates.remove(i);
                    candidate.resolved();
                }
            }
        }
    }

    /**
     * @param seq the sequence under consideration
     * @return the number of unacknowledged messages for that sequence
     */
    protected synchronized int countUnacknowledged(Sequence seq) {
        List<ResendCandidate> sequenceCandidates = getSequenceCandidates(seq);
        return sequenceCandidates == null ? 0 : sequenceCandidates.size();
    }
    
    /**
     * @return a map relating sequence ID to a lists of un-acknowledged 
     * messages for that sequence
     */
    protected Map<String, List<ResendCandidate>> getUnacknowledged() {
        return candidates;
    }
    
    /**
     * @param seq the sequence under consideration
     * @return the list of resend candidates for that sequence
     * @pre called with mutex held
     */
    protected List<ResendCandidate> getSequenceCandidates(Sequence seq) {
        return getSequenceCandidates(seq.getIdentifier().getValue());
    }
    
    /**
     * @param key the sequence identifier under consideration
     * @return the list of resend candidates for that sequence
     * @pre called with mutex held
     */
    protected List<ResendCandidate> getSequenceCandidates(String key) {
        return candidates.get(key);
    }
   
    /**
     * @return the base retransmission interval
     */
    protected long getBaseRetransmissionInterval() {
        return baseRetransmissionInterval;
    }
    
    /**
     * @return the exponential backoff
     */
    protected int getExponentialBackoff() {
        return exponentialBackoff;
    }
    
    /**
     * Shutdown.
     */
    protected synchronized void shutdown() {
        shutdown = true;
    }

    /**
     * @return true if shutdown
     */
    protected synchronized boolean isShutdown() {
        return shutdown;
    }
    
    /**
     * @return the ResendInitiator
     */
    protected Runnable getResendInitiator() {
        if (resendInitiator == null) {
            resendInitiator = new ResendInitiator();
        }
        return resendInitiator;
    }

    /**
     * @param context the message context 
     * @return a ResendCandidate
     */
    protected ResendCandidate createResendCandidate(ObjectMessageContext context) {
        return new ResendCandidate(context);
    }
    
    /**
     * Manages scheduling of resend attempts.
     * A single task runs every base transmission interval,
     * determining which resend candidates are due a resend attempt.
     */
    protected class ResendInitiator implements Runnable {
        public void run() {
            // iterate over resend candidates, resending any that are due
            synchronized (RetransmissionQueue.this) {
                Iterator<Map.Entry<String, List<ResendCandidate>>> sequences = 
                    candidates.entrySet().iterator();
                while (sequences.hasNext()) {
                    Iterator<ResendCandidate> sequenceCandidates =
                        sequences.next().getValue().iterator();
                    while (sequenceCandidates.hasNext()) {
                        ResendCandidate candidate = sequenceCandidates.next();
                        if (candidate.isDue()) {
                            candidate.initiate();
                        }
                    }
                }
            }
            
            if (!isShutdown()) {
                // schedule next resend initiation task (rescheduling each time,
                // as opposed to scheduling a periodic task, eliminates the
                // potential for simultaneous execution)
                workQueue.schedule(this, getBaseRetransmissionInterval());
            }
        }
    }
    
    /**
     * Represents a candidate for resend, i.e. an unacked outgoing message.
     * When this is determined as due another resend attempt, an asynchronous
     * task is scheduled for this purpose.
     */
    protected class ResendCandidate implements Runnable {
        private ObjectMessageContext context;
        private int skips;
        private int skipped;
        private boolean pending;

        /**
         * @param ctx message context for the unacked message
         */
        protected ResendCandidate(ObjectMessageContext ctx) {
            context = ctx;
            skipped = -1;
            skips = 1;
        }
        
        /**
         * Async resend logic.
         */
        public void run() {
            try {
                // ensure ACK wasn't received while this task was enqueued
                // on executor
                if (isPending()) {
                    resender.resend(context);
                }
            } finally {
                attempted();
            }
        }
        
        /**
         * @return true if candidate is due a resend
         * REVISIT should bound the max number of resend attampts
         */
        protected synchronized boolean isDue() {
            boolean due = false;
            // skip count is used to model exponential backoff
            // to avoid gratuitous time evaluation
            if (!pending && ++skipped == skips) {
                skips *= getExponentialBackoff();
                skipped = 0;
                due = true;
            }                    
            return due;
        }
        
        /**
         * @return if resend attempt is pending
         */
        protected synchronized boolean isPending() {
            return pending;
        }
       
        /**
         * Initiate resend asynchronsly.
         */
        protected synchronized void initiate() {
            pending = true;
            workQueue.execute(this);
        }
        
        /**
         * ACK has been received for this candidate.
         */
        protected synchronized void resolved() {
            pending = false;
            skips = Integer.MAX_VALUE;
        }
        
        /**
         * @return associated message context
         */
        protected MessageContext getContext() {
            return context;
        }
                        
        /**
         * A resend has been attempted.
         */
        private synchronized void attempted() {
            pending = false;
        }
    }
    
    /**
     * Encapsulates actual resend logic (pluggable to facilitate unit testing)
     */ 
    public interface Resender {
        /**
         * Resend mechanics.
         * 
         * @param context the cloned message context.
         */
        void resend(ObjectMessageContext context);
    }
}
