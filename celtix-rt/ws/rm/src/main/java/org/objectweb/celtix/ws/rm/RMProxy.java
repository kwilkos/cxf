package org.objectweb.celtix.ws.rm;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bindings.AbstractClientBinding;
import org.objectweb.celtix.bindings.BindingContextUtils;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.Request;
import org.objectweb.celtix.bindings.Response;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.addressing.RelatesToType;
import org.objectweb.celtix.ws.addressing.VersionTransformer;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

public class RMProxy {

    private static final Logger LOG = LogUtils.getL7dLogger(RMProxy.class);
    private RMHandler handler;
    // REVISIT assumption there is only a single outstanding offer
    private Identifier offeredIdentifier;

    public RMProxy(RMHandler h) {
        handler = h;
    }

    public void createSequence(RMSource source, 
                               org.objectweb.celtix.ws.addressing.EndpointReferenceType to,
                               EndpointReferenceType acksTo,
                               RelatesToType relatesTo) 
        throws IOException, SequenceFault {
        CreateSequenceRequest request =
            new CreateSequenceRequest(handler.getBinding(),
                                      getTransport(),
                                      source,
                                      to,
                                      acksTo,
                                      relatesTo);
        setOfferedIdentifier(request.getIncludedOffer());

        send(request, CreateSequenceRequest.createDataBindingCallback());
    }
    
    public void createSequenceResponse(AddressingProperties inMAPs,
                                       CreateSequenceResponseType csr) 
        throws IOException, SequenceFault {
        CreateSequenceResponse request =
            new CreateSequenceResponse(handler.getBinding(),
                                       getTransport(),
                                       inMAPs,
                                       csr);

        send(request, CreateSequenceResponse.createDataBindingCallback());
    }

    public void terminateSequence(SourceSequence seq) throws IOException {
        if (canSend(seq.getTarget())) {
            TerminateSequenceRequest request =
                new TerminateSequenceRequest(handler.getBinding(),
                                             getTransport(),
                                             seq);
            // required?
            handler.getSource().removeSequence(seq);

            send(request, TerminateSequenceRequest.createDataBindingCallback());
        }
    }
    
    /** 
     * Send a standalone message requesting acknowledgments for the
     * given sequences.
     * 
     * @param seqs the sequences for which acknowledgments are requested.
     * @throws IOException
     */
    public void requestAcknowledgment(Collection<SourceSequence> seqs) throws IOException {
        // it only makes sense to relate a group of sequnces in the same
        // AckRequest if they all have the same AcksTo, hence we can safely 
        // take the AckTo from the first sequence in the collection
        SourceSequence first = getFirstSequence(seqs);
        if (canSend(first.getTarget())) {
            SequenceInfoRequest request =
                new SequenceInfoRequest(handler.getBinding(),
                                        getTransport(),
                                        first.getTarget()); 
            request.requestAcknowledgement(seqs);
            send(request, null);
        }
    }
    
    /** 
     * Send a standalone LastMessage message for the given sequence.
     * 
     * @param seq the sequence for which the last message is to be sent.
     * @throws IOException
     */
    
    public void lastMessage(SourceSequence seq) throws IOException {
        LOG.fine("sending standalone last message");
        if (canSend(seq.getTarget())) {            
            SequenceInfoRequest request =
                new SequenceInfoRequest(handler.getBinding(),
                                        getTransport(),
                                        seq.getTarget()); 
            request.lastMessage(seq);
            send(request, null);
        }
    }
    
    /** 
     * Send a standalone SequenceAcknowledgement message for the given sequence.
     * 
     * @param seq the sequence for which an acknowledgment is to be sent.
     * @throws IOException
     */
    public void acknowledge(RMDestinationSequence seq) throws IOException {
        // required?
        if (Names.WSA_ANONYMOUS_ADDRESS.equals(seq.getAcksTo().getAddress().getValue())) {
            LOG.log(Level.WARNING, "STANDALONE_ANON_ACKS_NOT_SUPPORTED");
            return;
        }
        LOG.fine("sending standalone sequence acknowledgment");
        if (canSend(seq.getAcksTo())) {
            SequenceInfoRequest request =
                new SequenceInfoRequest(handler.getBinding(),
                                        handler.getTransport(),
                                        seq.getAcksTo()); 
            request.acknowledge(seq);
            send(request, null);
        }
    }
        
    protected Identifier getOfferedIdentifier() {
        return offeredIdentifier;    
    }
    
    protected void setOfferedIdentifier(OfferType offer) { 
        if (offer != null) {
            offeredIdentifier = offer.getIdentifier();
        }
    }
    
    protected void send(Request request, DataBindingCallback callback)
        throws IOException {        
        
        boolean isOneway = request.isOneway();
        if (handler.getBinding() != null) {            
            handler.getBinding().send(request, callback);
            if (!(handler.getClientBinding() == null || isOneway)) {
                Response response = 
                    ((AbstractClientBinding)handler.getClientBinding())
                        .getResponseCorrelator().getResponse(request);
                response.setHandlerInvoker(request.getHandlerInvoker());
                MessageContext responseContext = response.getBindingMessageContext();
                DataBindingCallback responseCallback =
                    BindingContextUtils.retrieveDataBindingCallback(responseContext);
                response.processLogical(responseCallback);
            }
        } else {
            AddressingProperties maps = 
                ContextUtils.retrieveMAPs(request.getObjectMessageContext(), true, true);
            String action = maps.getAction() != null
                            ? maps.getAction().getValue()
                            : "empty";
            Message msg = new Message("NO_BINDING_FOR_OUT_OF_BAND_MSG", LOG, action);
            LOG.severe(msg.toString());
        }
    }
    
    /**
     * A outgoing out-of-band protocol message cannot be sent if from the server
     * side if the target (e.g. the AcksTo address) is anonymous.
     * 
     * @param to the target EPR
     * @return true if the message may be sent
     */
    protected boolean canSend(EndpointReferenceType to) {
        return !(handler.getClientBinding() == null
                 && ContextUtils.isGenericAddress(VersionTransformer.convert(to)));
    }
    
    /**
     * A outgoing out-of-band protocol message cannot be sent if from the server
     * side if the target (e.g. the AcksTo address) is anonymous.
     * 
     * @param to the target EPR
     * @return true if the message may be sent
     */
    protected boolean canSend(org.objectweb.celtix.ws.addressing.EndpointReferenceType to) {
        boolean ret = false;
        if (handler.getClientBinding() == null) {
            ret = !ContextUtils.isGenericAddress(to);
        } else {
            try {
                ret = ((AbstractClientBinding)handler.getClientBinding()).getTransport() != null;
            } catch (IOException ioe) {
                // ignore
            }
        }
        return ret;
    }
    
    /**
     * This is required as the resource injected transport may be shutdown already
     * (e.g. for LastMessage or TerminateSequence messages originating from 
     * BusLifeCycleListener.preShutdown()).
     * 
     * @return
     */
    protected Transport getTransport() {
        Transport ret = null;
        if (handler.getClientBinding() == null) {
            ret = handler.getTransport();
        } else {
            try {
                ret = ((AbstractClientBinding)handler.getClientBinding()).getTransport();
            } catch (IOException ioe) {
                // ignore
            }
        }
        return ret;
    }
    
    
    private SourceSequence getFirstSequence(Collection<SourceSequence> seqs) {
        Iterator<SourceSequence> i = seqs.iterator();
        return i.hasNext() ? i.next() : null;
    }
    
    protected void setHandler(RMHandler h) {
        handler = h;
    }
}
