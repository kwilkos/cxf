package org.objectweb.celtix.bus.ws.rm;

import java.math.BigInteger;
import java.util.Map;

import javax.xml.namespace.QName;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bus.configuration.wsrm.AcksPolicyType;
import org.objectweb.celtix.bus.configuration.wsrm.DestinationPolicyType;
import org.objectweb.celtix.bus.configuration.wsrm.SequenceTerminationPolicyType;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.bus.jaxws.EndpointImpl;
import org.objectweb.celtix.bus.jaxws.ServiceImpl;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.configuration.ConfigurationProvider;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType.BaseRetransmissionInterval;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType.ExponentialBackoff;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

/**
 * Utility class used to access (and create if necessary) configuration for RM
 */
public class ConfigurationHelper {

    public static final String RM_CONFIGURATION_URI = "http://celtix.objectweb.org/bus/ws/rm/rm-config";
    public static final String RM_CONFIGURATION_ID = "rm-handler";
    
    public static final QName EXPONENTIAL_BACKOFF_BASE_ATTR = new QName(RM_CONFIGURATION_URI,
        "exponentialBackoffBase");

    // static final String POLICIES_PROPERTY_NAME = "policies";
    static final String RMASSERTION_PROPERTY_NAME = "rmAssertion";
    static final String SOURCE_POLICIES_PROPERTY_NAME = "sourcePolicies";
    static final String DESTINATION_POLICIES_PROPERTY_NAME = "destinationPolicies";
    
    private Configuration configuration;

    public ConfigurationHelper(AbstractBindingBase binding, boolean server) {
        Configuration busCfg = binding.getBus().getConfiguration();
        ConfigurationBuilder builder = ConfigurationBuilderFactory.getBuilder();
        Configuration parent;
        org.objectweb.celtix.ws.addressing.EndpointReferenceType ref = binding.getEndpointReference();

        if (server) {
            parent = builder.getConfiguration(EndpointImpl.ENDPOINT_CONFIGURATION_URI, EndpointReferenceUtils
                .getServiceName(ref).toString(), busCfg);
        } else {
            String id = EndpointReferenceUtils.getServiceName(ref).toString() + "/"
                        + EndpointReferenceUtils.getPortName(ref);
            parent = builder.getConfiguration(ServiceImpl.PORT_CONFIGURATION_URI, id, busCfg);            
        }

        configuration = builder.getConfiguration(RM_CONFIGURATION_URI, RM_CONFIGURATION_ID, parent);
        if (null == configuration) {
            configuration = builder.buildConfiguration(RM_CONFIGURATION_URI, RM_CONFIGURATION_ID, parent);

        }
        boolean policyProviderRegistered = false;
        for (ConfigurationProvider p : configuration.getProviders()) {
            if (p instanceof RMPolicyProvider) {
                policyProviderRegistered = true;
                break;
            }
        }
        if (!policyProviderRegistered) {
            configuration.getProviders().add(new RMPolicyProvider(binding.getBus(), 
                                                                  binding.getEndpointReference()));
        }
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }

    public RMAssertionType getRMAssertion() {
        RMAssertionType a = configuration.getObject(RMAssertionType.class, RMASSERTION_PROPERTY_NAME);

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
        String val = otherAttributes.get(EXPONENTIAL_BACKOFF_BASE_ATTR);
        if (null == val) {
            otherAttributes.put(EXPONENTIAL_BACKOFF_BASE_ATTR,
                                RetransmissionQueue.DEFAULT_EXPONENTIAL_BACKOFF);
        }

        return a;
    }

    public String getEndpointId() {
        StringBuffer buf = new StringBuffer();
        Configuration cfg = configuration.getParent();
        
        while (null != cfg) {
            if (buf.length() > 0) {
                buf.insert(0, ".");
            }
            buf.insert(0, cfg.getId().toString());
            cfg = cfg.getParent();
        }
        return buf.toString();
    }
    
    public SourcePolicyType getSourcePolicies() {
        SourcePolicyType sp = (SourcePolicyType)configuration
            .getObject(SourcePolicyType.class, SOURCE_POLICIES_PROPERTY_NAME);
        if (null == sp) {
            sp = RMUtils.getWSRMConfFactory().createSourcePolicyType();
        }
        return sp;
    }

    public SequenceTerminationPolicyType getSequenceTerminationPolicy() {
        SourcePolicyType sp = getSourcePolicies();
        assert null != sp;
        SequenceTerminationPolicyType stp = sp.getSequenceTerminationPolicy();
        if (null == stp) {
            stp = RMUtils.getWSRMConfFactory().createSequenceTerminationPolicyType();
        }
        return stp;
    }
    
    public DestinationPolicyType getDestinationPolicies() {
        DestinationPolicyType dp = configuration
            .getObject(DestinationPolicyType.class, DESTINATION_POLICIES_PROPERTY_NAME);
        if (null == dp) {
            dp = RMUtils.getWSRMConfFactory().createDestinationPolicyType();
        }
        return dp;
    }
    
    public AcksPolicyType getAcksPolicy() {
        DestinationPolicyType dp = getDestinationPolicies();
        assert null != dp;
        AcksPolicyType ap = dp.getAcksPolicy();
        if (null == ap) {
            ap = RMUtils.getWSRMConfFactory().createAcksPolicyType();
        }
        return ap;
    }
    
}
