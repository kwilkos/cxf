package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.Duration;

import org.objectweb.celtix.bus.configuration.wsrm.DestinationPolicyType;
import org.objectweb.celtix.common.logging.LogUtils;
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
                                                     AttributedURI to)
        throws SequenceFault {
        
        CreateSequenceResponseType csr = RMUtils.getWSRMFactory().createCreateSequenceResponseType();
        csr.setIdentifier(destination.generateSequenceIdentifier());

        DestinationPolicyType dp = destination.getDestinationPolicies();
        Duration supportedDuration = dp.getSequenceExpiration();
        if (null == supportedDuration) {
            supportedDuration = Sequence.PT0S;
        }
        Expires ex = cs.getExpires();
        LOG.info("supported duration: "  + supportedDuration);

        if (null != ex || supportedDuration.isShorterThan(Sequence.PT0S)) {
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
                accept.setAcksTo(RMUtils.createReference(to.getValue()));
                Sequence seq = new Sequence(offer.getIdentifier(), source, offer.getExpires(), csr
                    .getIdentifier());
                source.addSequence(seq);
                source.setCurrent(csr.getIdentifier(), seq);      
                LOG.fine("Making offered sequence the current sequence for responses to "
                         + csr.getIdentifier().getValue());
            } else {
                LOG.fine("Refusing inbound sequence offer"); 
                accept.setAcksTo(RMUtils.createReference(Names.WSA_NONE_ADDRESS));
            }
            csr.setAccept(accept);
        }
        
        destination.addSequence(csr.getIdentifier(), cs.getAcksTo());
        
        return csr;
    }
    

    /**
     * Checks if the terminated sequence was created in response to a createSequence
     * request that included an offer for an inbound sequence which was accepted.
     * In other words, check if there this handlers RM source manages a sequence for which
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
        
        Sequence terminatedSeq = destination.getSequence(sid);
        if (null == terminatedSeq) {
            LOG.severe("No such sequence.");
            return;
        } 

        destination.removeSequence(terminatedSeq);
        
        // the following may be necessary if the last message for this sequence was a oneway
        // request and hence there was no response to which a last message could have been added
        
        for (Sequence outboundSeq : destination.getHandler().getSource().getAllSequences()) {
            if (outboundSeq.offeredBy(sid) && null == outboundSeq.getLastMessageNumber()) {
                
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
}
