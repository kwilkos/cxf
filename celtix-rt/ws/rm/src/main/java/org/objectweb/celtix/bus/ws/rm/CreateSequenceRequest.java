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
import org.objectweb.celtix.bus.ws.addressing.VersionTransformer;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.RelatesToType;
import org.objectweb.celtix.ws.addressing.WSAContextUtils;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;

import org.objectweb.celtix.ws.rm.CreateSequenceType;
import org.objectweb.celtix.ws.rm.Expires;
import org.objectweb.celtix.ws.rm.OfferType;

public class CreateSequenceRequest extends Request {
    
    private static final String METHOD_NAME = "createSequence";    
    private static final String OPERATION_NAME = "CreateSequence";
    
    public CreateSequenceRequest(AbstractBindingBase b,
                                 Transport t,
                                 RMSource source, 
                                 org.objectweb.celtix.ws.addressing.EndpointReferenceType to,
                                 EndpointReferenceType acksTo,
                                 RelatesToType relatesTo) {
        
        super(b, t, b.createObjectContext());
        
        if (to != null) {
            WSAContextUtils.storeTo(to, getObjectMessageContext());
            WSAContextUtils.storeReplyTo(VersionTransformer.convert(acksTo),
                                      getObjectMessageContext());
        }

        WSAContextUtils.storeUsingAddressing(true, getObjectMessageContext());

        getObjectMessageContext().setRequestorRole(true);
        
        AddressingProperties maps = new AddressingPropertiesImpl();
        AttributedURIType actionURI = ContextUtils.WSA_OBJECT_FACTORY.createAttributedURIType();
        actionURI.setValue(RMUtils.getRMConstants().getCreateSequenceAction());
        maps.setAction(actionURI);
        maps.setRelatesTo(relatesTo);
        ContextUtils.storeMAPs(maps, getObjectMessageContext(), true, true, true, true);
            
        setMessageParameters(source, acksTo);
        
        // this request is sent as the initial request in a pair of
        // correlated oneways - setting the oneway flag to false is
        // simply to ensure that the WS-Addressing RelpyTo property
        // is included
        setOneway(false);
        expectRelatedRequest();
    }
    
    public static Method getMethod() {
        Method method  = null;
        try {
            method = OutOfBandProtocolMessages.class.getMethod(
                METHOD_NAME, 
                new Class[] {CreateSequenceType.class});
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
        return method;
    }
    
    public static String getOperationName() {
        return OPERATION_NAME;
    }
    
    public static DataBindingCallback createDataBindingCallback() {
        return new JAXBDataBindingCallback(getMethod(), DataBindingCallback.Mode.PARTS, null);
    }
    
    protected OfferType getIncludedOffer() {
        Object[] params = getObjectMessageContext().getMessageObjects();
        if (null == params || params.length < 1) {
            return null;
        }
        return ((CreateSequenceType)params[0]).getOffer();
    }
    
    private void setMessageParameters(RMSource source, EndpointReferenceType defaultAcksTo) {
        SourcePolicyType sourcePolicies = source.getHandler().getConfigurationHelper().getSourcePolicies();
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
