package org.objectweb.celtix.bus.ws.rm;

import java.lang.reflect.Method;

import javax.xml.datatype.Duration;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.Request;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.CreateSequenceType;
import org.objectweb.celtix.ws.rm.Expires;
import org.objectweb.celtix.ws.rm.OfferType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceAbstractPortType;

public class CreateSequenceRequest extends Request {
    
    private static final String METHOD_NAME = "createSequence";    
    
    public CreateSequenceRequest(AbstractBindingBase b, RMSource source, 
                                 EndpointReferenceType defaultAcksTo) {
        
        super(b, b.createObjectContext());
        getObjectMessageContext().setRequestorRole(true);
        
        getObjectMessageContext().setMethod(getMethod());
        
        AddressingProperties maps = new AddressingPropertiesImpl();
        AttributedURIType actionURI = ContextUtils.WSA_OBJECT_FACTORY.createAttributedURIType();
        actionURI.setValue(RMUtils.getRMConstants().getCreateSequenceAction());
        maps.setAction(actionURI);
        ContextUtils.storeMAPs(maps, getObjectMessageContext(), true, true, true, true);
            
        setMessageParameters(source, defaultAcksTo);
    }
    
    public static Method getMethod() {
        Method method  = null;
        try {
            method = SequenceAbstractPortType.class.getMethod(
                METHOD_NAME, 
                new Class[] {CreateSequenceType.class});
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
        return method;
    }
    
    public static DataBindingCallback createDataBindingCallback() {
        Method method = getMethod();
        return new JAXBDataBindingCallback(method, DataBindingCallback.Mode.PARTS, null);
    }
    
    protected OfferType getIncludedOffer() {
        Object[] params = getObjectMessageContext().getMessageObjects();
        if (null == params || params.length < 1) {
            return null;
        }
        return ((CreateSequenceType)params[0]).getOffer();
    }
    
    private void setMessageParameters(RMSource source, EndpointReferenceType defaultAcksTo) {
        SourcePolicyType sourcePolicies = source.getSourcePolicies();
        assert null != sourcePolicies;
        
        CreateSequenceType cs = RMUtils.getWSRMFactory().createCreateSequenceType();
        
        String address = sourcePolicies.getAcksTo();
        EndpointReferenceType acksTo = null;
        if (null != address) {
            acksTo = RMUtils.createReference(address);
        } else {
            acksTo = defaultAcksTo; 
        }
        cs.setAcksTo(acksTo);

        Duration d = sourcePolicies.getSequenceExpiration();
        if (null != d) {
            Expires expires = RMUtils.getWSRMFactory().createExpires();
            expires.setValue(d);  
            cs.setExpires(expires);
        }
        
        if (sourcePolicies.isIncludeOffer()) {
            OfferType offer = RMUtils.getWSRMFactory().createOfferType();
            d = sourcePolicies.getOfferedSequenceExpiration();
            if (null != d) {
                Expires expires = RMUtils.getWSRMFactory().createExpires();
                expires.setValue(d);  
                offer.setExpires(expires);
            }
            offer.setIdentifier(source.generateSequenceIdentifier());
            cs.setOffer(offer);
        }
        
        getObjectMessageContext().setMessageObjects(new Object[] {cs});
    }
}
