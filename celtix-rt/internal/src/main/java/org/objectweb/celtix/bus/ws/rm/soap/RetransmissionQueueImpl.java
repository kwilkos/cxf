package org.objectweb.celtix.bus.ws.rm.soap;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.Request;
import org.objectweb.celtix.bindings.Response;
import org.objectweb.celtix.bindings.ServerRequest;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.bus.ws.rm.ConfigurationHelper;
import org.objectweb.celtix.bus.ws.rm.RMContextUtils;
import org.objectweb.celtix.bus.ws.rm.RMUtils;
import org.objectweb.celtix.bus.ws.rm.RetransmissionQueue;
import org.objectweb.celtix.bus.ws.rm.SourceSequence;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.workqueue.WorkQueue;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.rm.AckRequestedType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.RMProperties;
import org.objectweb.celtix.ws.rm.SequenceType;
import org.objectweb.celtix.ws.rm.persistence.RMMessage;
import org.objectweb.celtix.ws.rm.persistence.RMStore;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;

public class RetransmissionQueueImpl implements RetransmissionQueue {

    private static final String SOAP_MSG_KEY = "org.objectweb.celtix.bindings.soap.message";
    private static final Logger LOG = LogUtils.getL7dLogger(RetransmissionQueueImpl.class);

    private PersistenceHandler handler;
    private WorkQueue workQueue;
    private long baseRetransmissionInterval;
    private int exponentialBackoff;
    private Map<String, List<ResendCandidate>> candidates;
    private Runnable resendInitiator;
    private boolean shutdown;
    private Resender resender;
    private Timer timer;

    /**
     * Constructor.
     */
    public RetransmissionQueueImpl(PersistenceHandler h) {
        this(h, Long.parseLong(DEFAULT_BASE_RETRANSMISSION_INTERVAL), Integer
            .parseInt(DEFAULT_EXPONENTIAL_BACKOFF));
    }

    /**
     * Constructor.
     */
    public RetransmissionQueueImpl(PersistenceHandler h, RMAssertionType rma) {
        this(h, rma.getBaseRetransmissionInterval().getMilliseconds().longValue(), Integer.parseInt(rma
            .getExponentialBackoff().getOtherAttributes().get(
                ConfigurationHelper.EXPONENTIAL_BACKOFF_BASE_ATTR)));
    }

    /**
     * Constructor.
     * 
     * @param base the base retransmission interval
     * @param backoff the exponential backoff
     */
    public RetransmissionQueueImpl(PersistenceHandler h, long base, int backoff) {
        handler = h;
        baseRetransmissionInterval = base;
        exponentialBackoff = backoff;
        candidates = new HashMap<String, List<ResendCandidate>>();
        resender = getDefaultResender();
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
    
    /**
     * Accepts a new context for posible future retransmission.
     * 
     * @param ctx the message context.
     */
    public void addUnacknowledged(ObjectMessageContext context) {
        cacheUnacknowledged(context);       
    }
    
    /**
     * Purge all candidates for the given sequence that have been acknowledged.
     * 
     * @param seq the sequence object.
     */
    public void purgeAcknowledged(SourceSequence seq) {
        Collection<BigInteger> purged = new ArrayList<BigInteger>();
        synchronized (this) {
            List<ResendCandidate> sequenceCandidates = getSequenceCandidates(seq);
            if (null != sequenceCandidates) {
                for (int i = sequenceCandidates.size() - 1; i >= 0; i--) {
                    ResendCandidate candidate = sequenceCandidates.get(i);
                    RMProperties properties = RMContextUtils.retrieveRMProperties(candidate.getContext(),
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
        }
        if (purged.size() > 0) {
            RMStore store = handler.getStore();
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
    public void start(WorkQueue queue) {
        if (null == workQueue) {
            LOG.fine("Starting retransmission queue");
            workQueue = queue;

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
    }
    /**
     * Stops retransmission queue.
     */ 
    public void stop() {
        if (null != timer) {
            LOG.fine("Stopping retransmission queue");
            timer.cancel();
        }
    }

    /**
     * Populates the retransmission queue with messages recovered from
     * persistent store.
     */
    public void populate(Collection<SourceSequence> seqs) {
        RMStore store = handler.getStore();
        if (null != store) {
            for (SourceSequence seq : seqs) {
                Collection<RMMessage> msgs = store.getMessages(seq.getIdentifier(), true);
                for (RMMessage msg : msgs) {
                    ObjectMessageContext objCtx = new ObjectMessageContextImpl();
                    objCtx.putAll(msg.getContext());                    
                    cacheUnacknowledged(objCtx);
                }
            }
        }
    }

    /**
     * Create default Resender logic.
     * 
     * @return default Resender
     */
    protected final Resender getDefaultResender() {
        return new Resender() {
            public void resend(ObjectMessageContext context, boolean requestAcknowledge) {
                RMProperties properties = RMContextUtils.retrieveRMProperties(context, true);
                SequenceType st = properties.getSequence();
                if (st != null) {
                    LOG.log(Level.INFO, "RESEND_MSG", st.getMessageNumber());
                }
                try {
                    refreshMAPs(context);
                    refreshRMProperties(context, requestAcknowledge);
                    if (ContextUtils.isRequestor(context)) {
                        clientResend(context);
                    } else {
                        serverResend(context);
                    }
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "RESEND_FAILED_MSG", e);
                }
            }
        };
    };

    /**
     * Refresh the MAPs with a new message ID (to avoid the resend being
     * rejected by the receiver-side WS-Addressing layer as a duplicate).
     * 
     * @param context the message context
     */
    private void refreshMAPs(MessageContext context) {
        AddressingProperties maps = ContextUtils.retrieveMAPs(context, false, true);
        String uuid = ContextUtils.generateUUID();
        maps.setMessageID(ContextUtils.getAttributedURI(uuid));
    }

    /**
     * Refresh the RM Properties with an AckRequested if necessary. Currently
     * the first resend for each sequence on each initiator iteration includes
     * an AckRequested. The idea is that a timely ACK may cause some of of the
     * resend to be avoided.
     * 
     * @param context the message context
     * @param requestAcknowledge true if an AckRequested header should be
     *            included
     */
    private void refreshRMProperties(MessageContext context, boolean requestAcknowledge) {
        RMProperties properties = RMContextUtils.retrieveRMProperties(context, true);
        List<AckRequestedType> requests = null;
        if (requestAcknowledge) {
            requests = new ArrayList<AckRequestedType>();
            requests.add(RMUtils.getWSRMFactory().createAckRequestedType());
            Identifier id = properties.getSequence().getIdentifier();
            requests.get(0).setIdentifier(id);
        }
        properties.setAcksRequested(requests);
    }

    /**
     * Create a client request for retransmission.
     * 
     * @param context the message context
     * @return an appropriate Request for the context
     */
    private Request createClientRequest(ObjectMessageContext context) {
        AbstractBindingBase binding = handler.getBinding();
        Transport transport = handler.getClientTransport();
        Request request = new Request(binding, transport, context);
        request.setOneway(ContextUtils.isOneway(context));
        return request;
    }

    /**
     * Client-side resend.
     * 
     * @param context the message context
     */
    private void clientResend(ObjectMessageContext context) throws IOException {
        Request request = createClientRequest(context);
        OutputStreamMessageContext outputStreamContext = request.process(null, true, true);
        ClientTransport transport = handler.getClientTransport();
        if (transport != null) {
            // decoupled response channel always being used with RM,
            // hence a partial response must be processed
            invokePartial(request, transport, outputStreamContext);
        } else {
            LOG.log(Level.WARNING, "NO_TRANSPORT_FOR_RESEND_MSG");
        }
    }

    /**
     * Create a server request for retransmission.
     * 
     * @param context the message context
     * @return an appropriate ServerRequest for the context
     */
    private ServerRequest createServerRequest(ObjectMessageContext context) {
        AbstractBindingBase binding = handler.getBinding();
        ServerRequest request = new ServerRequest(binding, context);
        // a server-originated resend implies a response, hence non-oneway
        request.setOneway(false);
        return request;
    }

    /**
     * Server-side resend.
     * 
     * @param context the message context
     */
    private void serverResend(ObjectMessageContext context) throws IOException {
        ServerTransport transport = handler.getServerTransport();
        if (transport != null) {
            ServerRequest serverRequest = createServerRequest(context);
            serverRequest.processOutbound(transport, null, true);
        } else {
            LOG.log(Level.WARNING, "NO_TRANSPORT_FOR_RESEND_MSG");
        }
    }

    /**
     * Invoke a oneway operation, allowing for a partial response.
     * 
     * @param request the request
     * @param transport the client transport
     * @param outputStreamContext the output stream message context
     */
    private void invokePartial(Request request, ClientTransport transport,
                               OutputStreamMessageContext outputStreamContext) throws IOException {
        InputStreamMessageContext inputStreamContext = transport.invoke(outputStreamContext);
        // create new binding and object message contexts for response so as to not
        // corrupt the stored context for the message about to be retransmitted
        // (necessary in case there will be more than one retransmission of
        // this message)
        // Response response = new Response(request);
        Response response = new Response(request.getBinding(), request.getHandlerInvoker());
        response.processProtocol(inputStreamContext);        
        response.processLogical(null);
    }

    
    /**
     * Plug in replacement resend logic (facilitates unit testing).
     * 
     * @param replacement resend logic
     */
    protected void replaceResender(Resender replacement) {
        resender = replacement;
    }

    /**
     * Accepts a new resend candidate.
     * 
     * @param ctx the message context.
     * @return ResendCandidate
     */
    protected ResendCandidate cacheUnacknowledged(ObjectMessageContext ctx) {
        ResendCandidate candidate = null;
        RMProperties rmps = RMContextUtils.retrieveRMProperties(ctx, true);
        if (null == rmps) {
            SOAPMessage message = (SOAPMessage)ctx.get(SOAP_MSG_KEY);
            rmps = handler.getRMSoapHandler().unmarshalRMProperties(message);
            RMContextUtils.storeRMProperties(ctx, rmps, true);
        }
        AddressingProperties maps = ContextUtils.retrieveMAPs(ctx, false, true);
        if (null == maps) {
            SOAPMessage message = (SOAPMessage)ctx.get(SOAP_MSG_KEY);
            try {
                maps = handler.getWsaSOAPHandler().unmarshalMAPs(message);
                ContextUtils.storeMAPs(maps, ctx, true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        SequenceType st = rmps.getSequence();
        Identifier sid = st.getIdentifier();
        synchronized (this) {
            String key = sid.getValue();
            List<ResendCandidate> sequenceCandidates = getSequenceCandidates(key);
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
    public synchronized void shutdown() {
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
     * Manages scheduling of resend attempts. A single task runs every base
     * transmission interval, determining which resend candidates are due a
     * resend attempt.
     */
    protected class ResendInitiator implements Runnable {
        public void run() {
            // iterate over resend candidates, resending any that are due
            synchronized (RetransmissionQueueImpl.this) {
                Iterator<Map.Entry<String, List<ResendCandidate>>> sequences = candidates.entrySet()
                    .iterator();
                while (sequences.hasNext()) {
                    Iterator<ResendCandidate> sequenceCandidates = sequences.next().getValue().iterator();
                    boolean requestAck = true;
                    while (sequenceCandidates.hasNext()) {
                        ResendCandidate candidate = sequenceCandidates.next();
                        if (candidate.isDue()) {
                            candidate.initiate(requestAck);
                            requestAck = false;
                        }
                    }
                }
            }
        }
    }

    /**
     * Represents a candidate for resend, i.e. an unacked outgoing message. When
     * this is determined as due another resend attempt, an asynchronous task is
     * scheduled for this purpose.
     */
    protected class ResendCandidate implements Runnable {
        private ObjectMessageContext context;
        private int skips;
        private int skipped;
        private boolean pending;
        private boolean includeAckRequested;

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
                    resender.resend(context, includeAckRequested);
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
            try {
                workQueue.execute(this);
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
         * @param if a AckRequest should be included
         */
        void resend(ObjectMessageContext context, boolean requestAcknowledge);
    }

  
}
