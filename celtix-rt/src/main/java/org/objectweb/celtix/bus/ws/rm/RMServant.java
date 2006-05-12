package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.Duration;

import org.objectweb.celtix.bus.configuration.wsrm.DestinationPolicyType;
import org.objectweb.celtix.bus.ws.addressing.VersionTransformer;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.v200408.AttributedURI;
import org.objectweb.celtix.ws.rm.AcceptType;
import org.objectweb.celtix.ws.rm.CreateSequenceResponseType;
import org.objectweb.celtix.ws.rm.CreateSequenceType;
import org.objectweb.celtix.ws.rm.Expires;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.OfferType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

public class RMServant {

    private static final Logger LOG = LogUtils.getL7dLogger(RMServant.class);

    // REVISIT assumption there is only a single outstanding unattached Identifier
    private Identifier unattachedIdentifier;
    
    public RMServant() {
    }
    
    /** 
     * Called on the RM handler upon inbound processing a CreateSequence request.
     * 
     * @param 
     * @return the CreateSequenceResponse.
     */
    public CreateSequenceResponseType createSequence(RMDestination destination,
                                                     CreateSequenceType cs, 
                                                     AddressingProperties maps)
        throws SequenceFault {
        
        CreateSequenceResponseType csr = RMUtils.getWSRMFactory().createCreateSequenceResponseType();
        csr.setIdentifier(destination.generateSequenceIdentifier());

        DestinationPolicyType dp = destination.getDestinationPolicies();
        Duration supportedDuration = dp.getSequenceExpiration();
        if (null == supportedDuration) {
            supportedDuration = SourceSequence.PT0S;
        }
        Expires ex = cs.getExpires();

        if (null != ex || supportedDuration.isShorterThan(SourceSequence.PT0S)) {
            Duration effectiveDuration = supportedDuration;
            if (null != ex && supportedDuration.isLongerThan(ex.getValue()))  {
                effectiveDuration = supportedDuration;
            }
            ex = RMUtils.getWSRMFactory().createExpires();
            ex.setValue(effectiveDuration);
            csr.setExpires(ex);
        }
        
        OfferType offer = cs.getOffer();
        if (null != offer) {
            AcceptType accept = RMUtils.getWSRMFactory().createAcceptType();
            if (dp.isAcceptOffers()) {
                RMSource source = destination.getHandler().getSource();
                LOG.fine("Accepting inbound sequence offer");
                AttributedURI to = VersionTransformer.convert(maps.getTo());
                accept.setAcksTo(RMUtils.createReference(to.getValue()));
                SourceSequence seq = new SourceSequence(offer.getIdentifier(), 
                                                                    null, 
                                                                    csr.getIdentifier());
                seq.setExpires(offer.getExpires());
                seq.setTarget(VersionTransformer.convert(cs.getAcksTo()));
                source.addSequence(seq);
                source.setCurrent(csr.getIdentifier(), seq);  
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Making offered sequence the current sequence for responses to "
                             + csr.getIdentifier().getValue());
                }
            } else {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Refusing inbound sequence offer"); 
                }
                accept.setAcksTo(RMUtils.createReference(Names.WSA_NONE_ADDRESS));
            }
            csr.setAccept(accept);
        }

        DestinationSequence seq = new DestinationSequence(csr.getIdentifier(), cs.getAcksTo(), destination);
        seq.setCorrelationID(maps.getMessageID().getValue());
        destination.addSequence(seq);
        
        return csr;
    }
    
    public void createSequenceResponse(RMSource source,
                                       CreateSequenceResponseType csr, 
                                       Identifier offeredId) {
        // moved from RMProxy.createSequence
        // csr.getIdentifier() is the Identifier for the newly created sequence
        SourceSequence seq = new SourceSequence(csr.getIdentifier());
        seq.setExpires(csr.getExpires());
        source.addSequence(seq);
        
        // the incoming sequence ID is either used as the requestor sequence
        // (signalled by null) or associated with a corresponding sequence 
        // identifier
        source.setCurrent(clearUnattachedIdentifier(), seq);

        // if a sequence was offered and accepted, then we can add this to
        // to the local destination sequence list, otherwise we have to wait for
        // and incoming CreateSequence request
        if (null != offeredId) {
            assert null != csr.getAccept();
            RMDestination dest = source.getHandler().getDestination();
            String address = csr.getAccept().getAcksTo().getAddress().getValue();
            if (!RMUtils.getAddressingConstants().getNoneURI().equals(address)) {
                //System.out.println("\n***offered: " + offeredId
                //                   + " csr: " + csr + " accept: "
                //                   + csr.getAccept() + "\n");
                DestinationSequence ds = 
                    new DestinationSequence(offeredId, csr.getAccept().getAcksTo(), dest);
                dest.addSequence(ds);
            }
        }
    }

    /**
     * Checks if the terminated sequence was created in response to a createSequence
     * request that included an offer for an inbound sequence which was accepted.
     * the offering identifier is equal to the identifier of the sequence now terminated,
     * and request termination of that sequence in turn.
     * 
     * @param destination
     * @param sid
     * @throws SequenceFault
     */
    public void terminateSequence(RMDestination destination, Identifier sid) 
        throws SequenceFault {
        // check if the terminated sequence was created in response to a a createSequence
        // request
        
        DestinationSequence terminatedSeq = destination.getSequence(sid);
        if (null == terminatedSeq) {
            LOG.severe("No such sequence.");
            return;
        } 

        destination.removeSequence(terminatedSeq);
        
        // the following may be necessary if the last message for this sequence was a oneway
        // request and hence there was no response to which a last message could have been added
        
        for (SourceSequence outboundSeq : destination.getHandler().getSource().getAllSequences()) {
            if (outboundSeq.offeredBy(sid) && !outboundSeq.isLastMessage()) {
                
                // send an out of band message with an empty body and a 
                // sequence header containing a lastMessage element.
               
                RMProxy proxy = destination.getHandler().getProxy();
                try {
                    proxy.lastMessage(outboundSeq);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Could not terminate correlated sequence.", ex);
                }
                
                
                break;
            }
        }
        
        
    }
    
    protected Identifier clearUnattachedIdentifier() {
        Identifier ret = unattachedIdentifier;
        unattachedIdentifier = null;
        return ret;
    }
    
    protected void setUnattachedIdentifier(Identifier i) { 
        unattachedIdentifier = i;
    }
}
