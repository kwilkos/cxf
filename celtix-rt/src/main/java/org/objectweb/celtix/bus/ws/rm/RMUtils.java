package org.objectweb.celtix.bus.ws.rm;

import org.objectweb.celtix.bus.ws.addressing.AddressingConstantsImpl;
import org.objectweb.celtix.ws.addressing.AddressingConstants;
import org.objectweb.celtix.ws.addressing.addressing200408.AttributedURI;
import org.objectweb.celtix.ws.addressing.addressing200408.EndpointReferenceType;

import org.objectweb.celtix.ws.rm.RMConstants;

public final class RMUtils {
   
    private static final org.objectweb.celtix.ws.rm.ObjectFactory WSRM_FACTORY;
    private static final org.objectweb.celtix.ws.addressing.addressing200408.ObjectFactory WSA_2004_FACTORY;
    private static final org.objectweb.celtix.ws.addressing.ObjectFactory WSA_2005_FACTORY;
    private static final org.objectweb.celtix.bus.configuration.wsrm.ObjectFactory WSRM_CONF_FACTORY;
    private static final RMConstants WSRM_CONSTANTS;
    private static final AddressingConstants WSA_CONSTANTS; 
    
    static {
        WSRM_FACTORY = new org.objectweb.celtix.ws.rm.ObjectFactory();
        WSA_2004_FACTORY = new org.objectweb.celtix.ws.addressing.addressing200408.ObjectFactory();
        WSA_2005_FACTORY = new org.objectweb.celtix.ws.addressing.ObjectFactory();
        WSRM_CONF_FACTORY = new org.objectweb.celtix.bus.configuration.wsrm.ObjectFactory();
        WSRM_CONSTANTS = new RMConstantsImpl();
        WSA_CONSTANTS = new AddressingConstantsImpl();
    }
    
    /**
     * prevent instantiation
     *
     */
    protected RMUtils() {        
    }
    
    public static org.objectweb.celtix.ws.rm.ObjectFactory getWSRMFactory() {
        return WSRM_FACTORY;
    }

    public static org.objectweb.celtix.ws.addressing.addressing200408.ObjectFactory getWSAFactory() {
        return WSA_2004_FACTORY;
    }
    
    public static org.objectweb.celtix.ws.addressing.ObjectFactory getWSA2005Factory() {
        return WSA_2005_FACTORY;
    }

    public static org.objectweb.celtix.bus.configuration.wsrm.ObjectFactory getWSRMConfFactory() {
        return WSRM_CONF_FACTORY;
    }
    
    public static RMConstants getRMConstants() {
        return WSRM_CONSTANTS;
    }
    
    public static AddressingConstants getAddressingConstants() {
        return WSA_CONSTANTS;
    }
    
    public static EndpointReferenceType createReference(String address) {
        EndpointReferenceType ref = WSA_2004_FACTORY.createEndpointReferenceType();
        AttributedURI value = WSA_2004_FACTORY.createAttributedURI();
        value.setValue(address);
        ref.setAddress(value);
        return ref;
    }
    
    public static EndpointReferenceType cast(EndpointReferenceType ref) {
        return ref;
    }
    
    public static EndpointReferenceType cast(org.objectweb.celtix.ws.addressing.EndpointReferenceType ref) {
        if (null == ref) {
            return null;
        }
        EndpointReferenceType ref2004 = WSA_2004_FACTORY.createEndpointReferenceType();
        AttributedURI address2004 = WSA_2004_FACTORY.createAttributedURI();
        address2004.setValue(ref.getAddress().getValue());
        address2004.getOtherAttributes().putAll(ref.getOtherAttributes());
        return ref2004;
    }
    
}
