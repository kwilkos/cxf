package org.objectweb.celtix.bus.ws.rm;

import org.objectweb.celtix.bus.ws.addressing.AddressingConstantsImpl;
import org.objectweb.celtix.bus.ws.addressing.VersionTransformer;
import org.objectweb.celtix.ws.addressing.AddressingConstants;
import org.objectweb.celtix.ws.addressing.v200408.AttributedURI;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;

import org.objectweb.celtix.ws.rm.RMConstants;

public final class RMUtils {
   
    private static final org.objectweb.celtix.ws.addressing.v200408.ObjectFactory WSA_FACTORY;
    private static final org.objectweb.celtix.ws.rm.ObjectFactory WSRM_FACTORY;
    private static final org.objectweb.celtix.bus.configuration.wsrm.ObjectFactory WSRM_CONF_FACTORY;
    private static final org.objectweb.celtix.ws.rm.policy.ObjectFactory WSRM_POLICY_FACTORY;
    private static final RMConstants WSRM_CONSTANTS;
    private static final AddressingConstants WSA_CONSTANTS; 
    
    static {
        WSA_FACTORY = new org.objectweb.celtix.ws.addressing.v200408.ObjectFactory();
        WSRM_FACTORY = new org.objectweb.celtix.ws.rm.ObjectFactory();
        WSRM_CONF_FACTORY = new org.objectweb.celtix.bus.configuration.wsrm.ObjectFactory();
        WSRM_POLICY_FACTORY = new org.objectweb.celtix.ws.rm.policy.ObjectFactory();
        WSRM_CONSTANTS = new RMConstantsImpl();
        WSA_CONSTANTS = new AddressingConstantsImpl();
    }
    
    /**
     * prevent instantiation
     *
     */
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
    
    public static EndpointReferenceType createReference(String address) {
        EndpointReferenceType ref = 
            VersionTransformer.Names200408.WSA_OBJECT_FACTORY.createEndpointReferenceType();
        AttributedURI value =
            VersionTransformer.Names200408.WSA_OBJECT_FACTORY.createAttributedURI();
        value.setValue(address);
        ref.setAddress(value);
        return ref;
    }
    
    public static EndpointReferenceType cast(EndpointReferenceType ref) {
        return ref;
    }    
}
