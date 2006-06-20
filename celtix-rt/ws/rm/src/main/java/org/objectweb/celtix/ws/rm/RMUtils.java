package org.objectweb.celtix.ws.rm;

import org.objectweb.celtix.ws.addressing.AddressingConstants;
import org.objectweb.celtix.ws.addressing.AddressingConstantsImpl;
import org.objectweb.celtix.ws.addressing.VersionTransformer;
import org.objectweb.celtix.ws.addressing.v200408.AttributedURI;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;

import org.objectweb.celtix.ws.policy.PolicyConstants;
import org.objectweb.celtix.ws.policy.PolicyConstantsImpl;
import org.objectweb.celtix.ws.rm.persistence.PersistenceUtils;

public final class RMUtils {
   
    private static final org.objectweb.celtix.ws.addressing.v200408.ObjectFactory WSA_FACTORY;
    private static final org.objectweb.celtix.ws.rm.ObjectFactory WSRM_FACTORY;
    private static final org.objectweb.celtix.bus.configuration.wsrm.ObjectFactory WSRM_CONF_FACTORY;
    private static final org.objectweb.celtix.ws.rm.policy.ObjectFactory WSRM_POLICY_FACTORY;
    private static final RMConstants WSRM_CONSTANTS;
    private static final AddressingConstants WSA_CONSTANTS; 
    private static final PolicyConstants WSP_CONSTANTS;
    private static final PersistenceUtils WSRM_PERSISTENCE_UTILS;
    
    static {
        WSA_FACTORY = new org.objectweb.celtix.ws.addressing.v200408.ObjectFactory();
        WSRM_FACTORY = new org.objectweb.celtix.ws.rm.ObjectFactory();
        WSRM_CONF_FACTORY = new org.objectweb.celtix.bus.configuration.wsrm.ObjectFactory();
        WSRM_POLICY_FACTORY = new org.objectweb.celtix.ws.rm.policy.ObjectFactory();
        WSRM_CONSTANTS = new RMConstantsImpl();
        WSA_CONSTANTS = new AddressingConstantsImpl();
        WSP_CONSTANTS = new PolicyConstantsImpl();
        WSRM_PERSISTENCE_UTILS = new PersistenceUtils();       
    }
    
    protected RMUtils() {        
    }
    
    public static org.objectweb.celtix.ws.addressing.v200408.ObjectFactory getWSAFactory() {
        return WSA_FACTORY;
    }
    
    public static org.objectweb.celtix.ws.rm.ObjectFactory getWSRMFactory() {
        return WSRM_FACTORY;
    }

    public static org.objectweb.celtix.bus.configuration.wsrm.ObjectFactory getWSRMConfFactory() {
        return WSRM_CONF_FACTORY;
    }
    
    public static org.objectweb.celtix.ws.rm.policy.ObjectFactory getWSRMPolicyFactory() {
        return WSRM_POLICY_FACTORY;
    }
    
    public static RMConstants getRMConstants() {
        return WSRM_CONSTANTS;
    }
    
    public static AddressingConstants getAddressingConstants() {
        return WSA_CONSTANTS;
    }
    
    public static PolicyConstants getPolicyConstants() {
        return WSP_CONSTANTS;
    }
    
    public static PersistenceUtils getPersistenceUtils() {
        return WSRM_PERSISTENCE_UTILS;
    }
    
    public static EndpointReferenceType createReference(String address) {
        EndpointReferenceType ref = 
            VersionTransformer.Names200408.WSA_OBJECT_FACTORY.createEndpointReferenceType();
        AttributedURI value =
            VersionTransformer.Names200408.WSA_OBJECT_FACTORY.createAttributedURI();
        value.setValue(address);
        ref.setAddress(value);
        return ref;
    }
}
