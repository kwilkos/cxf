package org.objectweb.celtix.bus.ws.rm;

import org.objectweb.celtix.ws.rm.RMConstants;

public final class RMUtils {
   
    private static final org.objectweb.celtix.ws.rm.ObjectFactory WSRM_FACTORY;
    private static final org.objectweb.celtix.ws.addressing.ObjectFactory WSA_FACTORY;
    private static final org.objectweb.celtix.bus.configuration.wsrm.ObjectFactory WSRM_CONF_FACTORY;
    private static final RMConstants WSRM_CONSTANTS;
    
    static {
        WSRM_FACTORY = new org.objectweb.celtix.ws.rm.ObjectFactory();
        WSA_FACTORY = new org.objectweb.celtix.ws.addressing.ObjectFactory();
        WSRM_CONF_FACTORY = new org.objectweb.celtix.bus.configuration.wsrm.ObjectFactory();
        WSRM_CONSTANTS = new RMConstantsImpl();
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

    public static org.objectweb.celtix.ws.addressing.ObjectFactory getWSAFactory() {
        return WSA_FACTORY;
    }

    public static org.objectweb.celtix.bus.configuration.wsrm.ObjectFactory getWSRMConfFactory() {
        return WSRM_CONF_FACTORY;
    }
    
    public static RMConstants getRMConstants() {
        return WSRM_CONSTANTS;
    }
    
    
}
