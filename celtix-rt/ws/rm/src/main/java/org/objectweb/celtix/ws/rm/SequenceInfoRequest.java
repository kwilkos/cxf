package org.objectweb.celtix.ws.rm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.Request;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.addressing.VersionTransformer;
import org.objectweb.celtix.ws.addressing.WSAContextUtils;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;

public class SequenceInfoRequest extends Request {
    
    public SequenceInfoRequest(AbstractBindingBase b,
                               Transport t,
                               EndpointReferenceType to) {
        this(b, t, VersionTransformer.convert(to));
    }
    
    public SequenceInfoRequest(AbstractBindingBase b,
                               Transport t,
                               org.objectweb.celtix.ws.addressing.EndpointReferenceType to) {

        super(b, t, b.createObjectContext());
        
        if (to != null) {
            WSAContextUtils.storeTo(to, getObjectMessageContext());
        }

        WSAContextUtils.storeUsingAddressing(true, getObjectMessageContext());
            
        getObjectMessageContext().setRequestorRole(true);
        AddressingProperties maps = new AddressingPropertiesImpl();
        AttributedURIType actionURI =
            ContextUtils.WSA_OBJECT_FACTORY.createAttributedURIType();
        actionURI.setValue(RMUtils.getRMConstants().getSequenceInfoAction());
        maps.setAction(actionURI);
        ContextUtils.storeMAPs(maps, getObjectMessageContext(), true, true, true, true);  
        
        setOneway(true);
        
        // NOTE: Not storing a method in the context causes BindingContextUtils.isOnewayMethod
        // to always return false (although effectively all standalone requests based on the
        // the SequenceInfo request are oneway requests). 
        // An important implication of this is that we don't expect partial
        // responses sent in response to such messages, which is fine as we normally only piggyback
        // sequence acknowledgements onto application messages.
    }
    
    public void requestAcknowledgement(Collection<SourceSequence> seqs) {
        List<AckRequestedType> requested = new ArrayList<AckRequestedType>();
        for (AbstractSequenceImpl seq : seqs) {
            AckRequestedType ar = RMUtils.getWSRMFactory().createAckRequestedType();
            ar.setIdentifier(seq.getIdentifier());
            requested.add(ar);
        }
        RMPropertiesImpl rmps = new RMPropertiesImpl();        
        rmps.setAcksRequested(requested);
        RMContextUtils.storeRMProperties(getObjectMessageContext(), rmps, true);
    }
    
    public void acknowledge(RMDestinationSequence seq) {
        AddressingProperties maps = ContextUtils.retrieveMAPs(getObjectMessageContext(), true, true);
        maps.getAction().setValue(RMUtils.getRMConstants().getSequenceAcknowledgmentAction());
        AttributedURIType toAddress = ContextUtils.WSA_OBJECT_FACTORY.createAttributedURIType();
        toAddress.setValue(seq.getAcksTo().getAddress().getValue());
        maps.setTo(toAddress);
        // rm properties will be created (and actual acknowledgments added)
        // by rm handler upon outbound processing of this message
    }
    
    public void lastMessage(SourceSequence seq) {
        AddressingProperties maps = ContextUtils.retrieveMAPs(getObjectMessageContext(), true, true);
        maps.getAction().setValue(RMUtils.getRMConstants().getLastMessageAction());
        RMPropertiesImpl rmps = new RMPropertiesImpl(); 
        seq.nextAndLastMessageNumber();
        rmps.setSequence(seq);
        assert seq.isLastMessage();
        RMContextUtils.storeRMProperties(getObjectMessageContext(), rmps, true);
    }
}
