/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.ws.rm.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.ws.rm.Identifier;
import org.apache.cxf.ws.rm.RMContextUtils;
import org.apache.cxf.ws.rm.RMManager;
import org.apache.cxf.ws.rm.RMMessageConstants;
import org.apache.cxf.ws.rm.RMProperties;
import org.apache.cxf.ws.rm.RetransmissionCallback;
import org.apache.cxf.ws.rm.RetransmissionQueue;
import org.apache.cxf.ws.rm.SequenceType;
import org.apache.cxf.ws.rm.SourceSequence;
import org.apache.cxf.ws.rm.persistence.RMStore;
import org.apache.cxf.ws.rm.policy.RMAssertion;

/**
 * 
 */
public class RetransmissionQueueImpl implements RetransmissionQueue {

    private static final Logger LOG = LogUtils.getL7dLogger(RetransmissionQueueImpl.class);
    
    private Map<String, List<ResendCandidate>> candidates = new HashMap<String, List<ResendCandidate>>();
    private Resender resender;
    private Runnable resendInitiator;
    private Timer timer;
    private RMManager manager;
    
    public RetransmissionQueueImpl(RMManager m) {
        manager = m;
    }
      
    public RMManager getManager() {
        return manager;
    }

    public void setManager(RMManager m) {
        manager = m;
    }

    public long getBaseRetransmissionInterval() {
        RMAssertion rma = null == manager ? null : manager.getRMAssertion();
        if (null != rma && null != rma.getBaseRetransmissionInterval()
            && null != rma.getBaseRetransmissionInterval().getMilliseconds()) {
            return rma.getBaseRetransmissionInterval().getMilliseconds().longValue();
        }
        return new BigInteger(DEFAULT_BASE_RETRANSMISSION_INTERVAL).longValue();
    }

    public void addUnacknowledged(Message message) {
        cacheUnacknowledged(message);        
    }

    /**
     * @param seq the sequence under consideration
     * @return the number of unacknowledged messages for that sequence
     */
    public synchronized int countUnacknowledged(SourceSequence seq) {
        List<ResendCandidate> sequenceCandidates = getSequenceCandidates(seq);
        return sequenceCandidates == null ? 0 : sequenceCandidates.size();
    }

    /**
     * @return true if there are no unacknowledged messages in the queue
     */
    public boolean isEmpty() {  
        return 0 == getUnacknowledged().size();
    }

    public void populate(Collection<SourceSequence> sss) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Purge all candidates for the given sequence that have been acknowledged.
     * 
     * @param seq the sequence object.
     */
    public void purgeAcknowledged(SourceSequence seq) {
        Collection<BigInteger> purged = new ArrayList<BigInteger>();
        synchronized (this) {
            LOG.fine("Start purging resend candidates.");
            List<ResendCandidate> sequenceCandidates = getSequenceCandidates(seq);
            if (null != sequenceCandidates) {
                for (int i = sequenceCandidates.size() - 1; i >= 0; i--) {
                    ResendCandidate candidate = sequenceCandidates.get(i);
                    RMProperties properties = RMContextUtils.retrieveRMProperties(candidate.getMessage(),
                                                                                  true);
                    SequenceType st = properties.getSequence();
                    BigInteger m = st.getMessageNumber();
                    if (seq.isAcknowledged(m)) {
                        sequenceCandidates.remove(i);
                        candidate.resolved();
                        purged.add(m);
                    }
                }
            }
            LOG.fine("Completed purging resend candidates.");
        }
        if (purged.size() > 0) {
            RMStore store = manager.getStore();
            if (null != store) {
                store.removeMessages(seq.getIdentifier(), purged, true);
            }
        }
    }

    /**
     * Initiate resends.
     * 
     * @param queue the work queue providing async execution
     */
    public void start() {
        if (null != timer) {
            return;
        }
        LOG.fine("Starting retransmission queue");
        // setup resender
        if (null == resender) {
            resender = getDefaultResender();
        }
        
        // start resend initiator
        TimerTask task = new TimerTask() {
            public void run() {
                getResendInitiator().run();
            }
        };
        timer = new Timer();
        // TODO
        // delay starting the queue to give the first request a chance to be sent before 
        // waiting for another period.
        timer.schedule(task, getBaseRetransmissionInterval() / 2, getBaseRetransmissionInterval());  
    }

    /**
     * Stops retransmission queue.
     */ 
    public void stop() {
        if (null != timer) {
            LOG.fine("Stopping retransmission queue");
            timer.cancel();
            timer = null;
        }  
    }
    
    /**
     * @return the exponential backoff
     */
    protected int getExponentialBackoff() {
        return DEFAULT_EXPONENTIAL_BACKOFF;
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
     * @param message the message context
     * @return a ResendCandidate
     */
    protected ResendCandidate createResendCandidate(Message message) {
        return new ResendCandidate(message);
    }
    
    /**
     * Accepts a new resend candidate.
     * 
     * @param ctx the message context.
     * @return ResendCandidate
     */
    protected ResendCandidate cacheUnacknowledged(Message message) {
        ResendCandidate candidate = null;
        RMProperties rmps = RMContextUtils.retrieveRMProperties(message, true);        
        SequenceType st = rmps.getSequence();
        Identifier sid = st.getIdentifier();
        synchronized (this) {
            String key = sid.getValue();
            List<ResendCandidate> sequenceCandidates = getSequenceCandidates(key);
            if (null == sequenceCandidates) {
                sequenceCandidates = new ArrayList<ResendCandidate>();
                candidates.put(key, sequenceCandidates);
            }
            candidate = new ResendCandidate(message);
            sequenceCandidates.add(candidate);
        }
        LOG.fine("Cached unacknowledged message.");
        return candidate;
    }
    
    /**
     * @return a map relating sequence ID to a lists of un-acknowledged messages
     *         for that sequence
     */
    protected Map<String, List<ResendCandidate>> getUnacknowledged() {
        return candidates;
    }

    /**
     * @param seq the sequence under consideration
     * @return the list of resend candidates for that sequence
     * @pre called with mutex held
     */
    protected List<ResendCandidate> getSequenceCandidates(SourceSequence seq) {
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
    
    private void clientResend(Message message) {        
        Conduit c = message.getExchange().getConduit();
        try {
            
            // get registered callbacks, create new output stream and re-register
            // all callbacks except the retransmission callback
            
            OutputStream os = message.getContent(OutputStream.class);
            List<CachedOutputStreamCallback> callbacks = null;            
            if (os instanceof AbstractCachedOutputStream) {
                callbacks = ((AbstractCachedOutputStream)os).getCallbacks();
            }
            
            c.send(message);
            
            os = message.getContent(OutputStream.class);
            if (os instanceof AbstractCachedOutputStream && callbacks.size() > 1) {
                for (CachedOutputStreamCallback cb : callbacks) {
                    if (!(cb instanceof RetransmissionCallback)) {
                        ((AbstractCachedOutputStream)os).registerCallback(cb);
                    }
                }
            }
            ByteArrayOutputStream savedOutputStream = 
                (ByteArrayOutputStream)message.get(RMMessageConstants.SAVED_OUTPUT_STREAM);
            ByteArrayInputStream bis = new ByteArrayInputStream(savedOutputStream.toByteArray());
            
            // copy saved output stream to new output stream in chunks of 1024
            AbstractCachedOutputStream.copyStream(bis, os, 1024);
            os.flush();
            os.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "RESEND_FAILED_MSG", ex); 
        }
    }
    
    private void serverResend(Message message) {
        // TODO
    }
    
    /**
     * Manages scheduling of resend attempts. A single task runs every base
     * transmission interval, determining which resend candidates are due a
     * resend attempt.
     */
    protected class ResendInitiator implements Runnable {
        public void run() {            
            // iterate over resend candidates, resending any that are due
            synchronized (RetransmissionQueueImpl.this) {
                LOG.fine("Starting ResendInitiator on thread " + Thread.currentThread());
                Iterator<Map.Entry<String, List<ResendCandidate>>> sequences = candidates.entrySet()
                    .iterator();
                while (sequences.hasNext()) {
                    Iterator<ResendCandidate> sequenceCandidates = sequences.next().getValue().iterator();
                    boolean requestAck = true;
                    try {
                        while (sequenceCandidates.hasNext()) {
                            ResendCandidate candidate = sequenceCandidates.next();
                            if (candidate.isDue()) {
                                candidate.initiate(requestAck);
                                requestAck = false;
                            }
                        }
                    } catch (ConcurrentModificationException ex) {
                        // TODO: 
                        // can happen if resend occurs on same thread as resend initiation
                        // i.e. when endpoint's executor executes on current thread
                        LOG.log(Level.WARNING, "RESEND_CANDIDATES_CONCURRENT_MODIFICATION_MSG");
                    }
                }
                LOG.fine("Completed ResendInitiator");
            }
            
        }
    }
    
    /**
     * Represents a candidate for resend, i.e. an unacked outgoing message. When
     * this is determined as due another resend attempt, an asynchronous task is
     * scheduled for this purpose.
     */
    protected class ResendCandidate implements Runnable {
        private Message message;
        private int skips;
        private int skipped;
        private boolean pending;
        private boolean includeAckRequested;

        /**
         * @param ctx message context for the unacked message
         */
        protected ResendCandidate(Message m) {
            message = m;
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
                    resender.resend(message, includeAckRequested);
                    includeAckRequested = false;
                }
            } finally {
                attempted();
            }
        }

        /**
         * @return true if candidate is due a resend REVISIT should bound the
         *         max number of resend attampts
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
         * 
         * @param requestAcknowledge true if a AckRequest header is to be sent
         *            with resend
         */
        protected synchronized void initiate(boolean requestAcknowledge) {
            includeAckRequested = requestAcknowledge;
            pending = true;
            Endpoint ep = message.getExchange().get(Endpoint.class);
            Executor executor = ep.getExecutor();
            if (null == executor) {
                executor = ep.getService().getExecutor();
            }
            try {
                executor.execute(this);
            } catch (RejectedExecutionException ex) {
                LOG.log(Level.SEVERE, "RESEND_INITIATION_FAILED_MSG", ex);
            }
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
        protected Message getMessage() {
            return message;
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
         * @param if a AckRequest should be included
         */
        void resend(Message message, boolean requestAcknowledge);
    }
    
    /**
     * Create default Resender logic.
     * 
     * @return default Resender
     */
    protected final Resender getDefaultResender() {
        return new Resender() {
            public void resend(Message message, boolean requestAcknowledge) {                
                RMProperties properties = RMContextUtils.retrieveRMProperties(message, true);
                SequenceType st = properties.getSequence();
                if (st != null) {
                    LOG.log(Level.INFO, "RESEND_MSG", st.getMessageNumber());
                }
                try {
                    // TODO: remove previously added acknowledgments and update
                    // message id (to avoid duplicates)
                    
                    if (RMContextUtils.isRequestor(message)) {
                        clientResend(message);
                    } else {
                        serverResend(message);
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

}
