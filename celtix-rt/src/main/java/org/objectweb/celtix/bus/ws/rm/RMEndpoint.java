package org.objectweb.celtix.bus.ws.rm;

import java.math.BigInteger;
import java.util.Map;

import javax.xml.namespace.QName;

import org.objectweb.celtix.bus.configuration.wsrm.EndpointPolicyType;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType.BaseRetransmissionInterval;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType.ExponentialBackoff;

public class RMEndpoint {

    private static final String POLICIES_PROPERTY_NAME = "policies";
    private static final String RMASSERTION_PROPERTY_NAME = "rmAssertion";
    
    private RMHandler handler;
    

    protected RMEndpoint(RMHandler h) {
        handler = h;
    }
    
    
    public RMHandler getHandler() {
        return handler;
    }
    
    public RMAssertionType getRMAssertion() {
        RMAssertionType a = getHandler().getConfiguration()
            .getObject(RMAssertionType.class, RMASSERTION_PROPERTY_NAME);        

        // the following should not be necessary as the rm handler configuration metadata 
        // supplies a default value for the RMAssertion
        
        if (null == a) {
            a = RMUtils.getWSRMPolicyFactory().createRMAssertionType();
            RMUtils.getWSRMPolicyFactory().createRMAssertionType();
        }
        
        if (null == a.getBaseRetransmissionInterval()) {
            BaseRetransmissionInterval bri = 
                RMUtils.getWSRMPolicyFactory().createRMAssertionTypeBaseRetransmissionInterval();
            bri.setMilliseconds(new BigInteger(RetransmissionQueue.DEFAULT_BASE_RETRANSMISSION_INTERVAL));
            a.setBaseRetransmissionInterval(bri);
        }
        
        if (null == a.getExponentialBackoff()) {
            ExponentialBackoff eb  = 
                RMUtils.getWSRMPolicyFactory().createRMAssertionTypeExponentialBackoff();
            a.setExponentialBackoff(eb);
        }
        Map<QName, String> otherAttributes = a.getExponentialBackoff().getOtherAttributes();
        String val = otherAttributes.get(RetransmissionQueue.EXPONENTIAL_BACKOFF_BASE_ATTR);
        if (null == val) {
            otherAttributes.put(RetransmissionQueue.EXPONENTIAL_BACKOFF_BASE_ATTR, 
                                RetransmissionQueue.DEFAULT_EXPONENTIAL_BACKOFF);
        }  

        return a;
    }
    

    /**
     * Generates and returns a sequence identifier.
     * 
     * @return the sequence identifier.
     */
    public Identifier generateSequenceIdentifier() {
        String sequenceID = ContextUtils.generateUUID();
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue(sequenceID);        
        return sid;
    }

    public EndpointPolicyType getPolicies() {
        return (EndpointPolicyType)handler.getConfiguration().getObject(POLICIES_PROPERTY_NAME);
    }
    
    public String getEndpointId() {
        return handler.getConfiguration().getParent().getId().toString(); 
    }
    
}
