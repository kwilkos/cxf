package org.objectweb.celtix.bus.ws.rm;

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
            System.out.println(offer);
            AcceptType accept = RMUtils.getWSRMFactory().createAcceptType();
            if (dp.isAcceptOffers()) {                
                accept.setAcksTo(RMUtils.createReference(to.getValue()));
            } else {
                accept.setAcksTo(RMUtils.createReference(RMUtils.getAddressingConstants().getNoneURI()));
            }
            csr.setAccept(accept);
        }
        
        destination.addSequence(csr.getIdentifier(), cs.getAcksTo());
        
        return csr;
    }
    
    public void terminateSequence(RMDestination destination, Identifier sid) 
        throws SequenceFault {
        
    }
}
