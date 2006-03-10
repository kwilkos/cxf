package org.objectweb.celtix.bus.ws.rm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.Request;
import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.rm.AckRequestedType;

public class SequenceInfoRequest extends Request {
    
    public SequenceInfoRequest(AbstractBindingBase b) {
        
        super(b, b.createObjectContext());
        getObjectMessageContext().setRequestorRole(true);
        AddressingProperties maps = new AddressingPropertiesImpl();
        AttributedURIType actionURI =
            ContextUtils.WSA_OBJECT_FACTORY.createAttributedURIType();
        actionURI.setValue(RMUtils.getRMConstants().getSequenceInfoAction());
        maps.setAction(actionURI);
        ContextUtils.storeMAPs(maps, getObjectMessageContext(), true);
    }
    
    public void requestAcknowledgement(Collection<Sequence> seqs) {
        List<AckRequestedType> requested = new ArrayList<AckRequestedType>();
        for (Sequence seq : seqs) {
            AckRequestedType ar = RMUtils.getWSRMFactory().createAckRequestedType();
            ar.setIdentifier(seq.getIdentifier());
            requested.add(ar);
        }
        RMContextUtils.storeAcksRequested(getObjectMessageContext(), requested);
    }
}
