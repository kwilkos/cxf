package org.objectweb.celtix.management.jmx;

import javax.management.MBeanServer;

import org.objectweb.celtix.BusEvent;

/**
 * An event indicating that an external MBean server must be
 * registered with the <code>Bus</code>.   
 * This event identifies the external MBean server that must be registered.
 */
public class MBeanServerEvent extends BusEvent {
   
    /**
     * Constant representing the MBeanServer Bus Event ID.
     */
    public static final String MBEAN_SERVER_EVENT = "org.objectweb.celtix.mbean.server.event";
    
    
    /**
     * Constructs an <code>MBeanServerEvent</code> object.
     * @param server The external <code>MBeanServer</code> to be registered.
     */
    public MBeanServerEvent(MBeanServer server) {
        super(server, MBEAN_SERVER_EVENT);
    }
}
