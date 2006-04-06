package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.CreateSequenceResponseType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.OfferType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

public class RMProxy {

    private static final Logger LOG = LogUtils.getL7dLogger(RMProxy.class);
    private RMHandler handler;

    public RMProxy(RMHandler h) {
        handler = h;
    }

    public CreateSequenceResponseType createSequence(RMSource source, 
                                                     EndpointReferenceType acksTo, 
                                                     Identifier i) 
        throws IOException, SequenceFault {
        CreateSequenceRequest request = new CreateSequenceRequest(handler.getBinding(), source, acksTo);
        OfferType o = request.getIncludedOffer();

        ObjectMessageContext responseCtx = invoke(request.getObjectMessageContext(), 
                                                  CreateSequenceRequest.createDataBindingCallback());
        Object result = responseCtx.getReturn();
        if (result instanceof JAXBElement) {
            result = ((JAXBElement)result).getValue();
        }
        CreateSequenceResponseType csr = (CreateSequenceResponseType)result;
        
        Sequence seq = new Sequence(csr.getIdentifier(), source, csr.getExpires());
        source.addSequence(seq);
        source.setCurrent(i, seq);
        
        if (null != o) {
            assert null != csr.getAccept();
            RMDestination dest = source.getHandler().getDestination();
            String address = csr.getAccept().getAcksTo().getAddress().getValue();
            if (!RMUtils.getAddressingConstants().getNoneURI().equals(address)) {
                Sequence ds = new Sequence(o.getIdentifier(), dest, csr.getAccept().getAcksTo());
                dest.addSequence(ds);
            }
        }
        
        return csr;
    }
    
    public void terminateSequence(Sequence seq) throws IOException {
        TerminateSequenceRequest request = new TerminateSequenceRequest(handler.getBinding(), seq);
        
        invokeOneWay(request.getObjectMessageContext(), TerminateSequenceRequest.createDataBindingCallback());
    }
    
    /** 
     * Send a standalone message requesting acknowledgments for the
     * given sequences.
     * 
     * @param seqs the sequences for which acknowledgments are requested.
     * @throws IOException
     */
    public void requestAcknowledgment(Collection<Sequence> seqs) throws IOException {
        SequenceInfoRequest request = new SequenceInfoRequest(handler.getBinding()); 
        request.requestAcknowledgement(seqs);
        invokeOneWay(request.getObjectMessageContext(), null);
    }
    
    /** 
     * Send a standalone LastMessage message for the given sequence.
     * 
     * @param seq the sequence for which the last message is to be sent.
     * @throws IOException
     */
    
    public void lastMessage(Sequence seq) throws IOException {
        LOG.fine("sending standalone last message");
        SequenceInfoRequest request = new SequenceInfoRequest(handler.getBinding()); 
        request.lastMessage(seq);
        invokeOneWay(request.getObjectMessageContext(), null);
    }
    
    /** 
     * Send a standalone SequenceAcknowledgement message for the given sequence.
     * 
     * @param seq the sequence for which an acknowledgment is to be sent.
     * @throws IOException
     */
    public void acknowledge(Sequence seq) throws IOException {
        LOG.fine("sending standalone sequence acknowledgment");
        SequenceInfoRequest request = new SequenceInfoRequest(handler.getBinding()); 
        request.acknowledge(seq);
        invokeOneWay(request.getObjectMessageContext(), null);
    }
    
    public void sequenceInfo() throws IOException {
        SequenceInfoRequest request = new SequenceInfoRequest(handler.getBinding());
        invokeOneWay(request.getObjectMessageContext(), null);
    }
    
    private ObjectMessageContext invoke(ObjectMessageContext requestCtx, DataBindingCallback callback) 
        throws IOException, SequenceFault {
        ObjectMessageContext responseCtx = null;
        if (handler.getClientBinding() != null) {
            responseCtx = handler.getClientBinding().invoke(requestCtx, callback); 
            throwIfNecessary(responseCtx);
        } else {
            // wait for changes on the transport decoupling -
            // server transport should allow to send this out of band request
            Message msg = new Message("SERVER_SIDE_OUT_OF_BAND_NOT_IMPLEMENTED_MSG", LOG,
                ContextUtils.retrieveMAPs(requestCtx, true, true).getAction().getValue());
            LOG.severe(msg.toString());
        } 
        return responseCtx;
    }
    
    private void invokeOneWay(ObjectMessageContext requestCtx, DataBindingCallback callback)
        throws IOException {        
        
        if (handler.getClientBinding() != null) {            
            handler.getClientBinding().invokeOneWay(requestCtx, callback);
        } else {
            // wait for changes on the transport decoupling -
            // server transport should allow to send this out of band request
            Message msg = new Message("SERVER_SIDE_OUT_OF_BAND_NOT_IMPLEMENTED_MSG", LOG,
                ContextUtils.retrieveMAPs(requestCtx, true, true).getAction().getValue());
            LOG.severe(msg.toString());
        }
    }
    
    private void throwIfNecessary(ObjectMessageContext objextCtx) throws SequenceFault
    {
        Throwable t = objextCtx.getException();
        if (null != t) {
            LOG.log(Level.INFO, "RM_INVOCATION_FAILED", t);
            if (ProtocolException.class.isAssignableFrom(t.getClass())) {
                throw (ProtocolException)t;
            } else if (WebServiceException.class.isAssignableFrom(t.getClass())) {
                throw (WebServiceException)t;
            } else if (SequenceFault.class.isAssignableFrom(t.getClass())) {
                throw (SequenceFault)t;
            } else {                
                throw new ProtocolException(objextCtx.getException());
            }
        }        
    }

}
