package org.objectweb.celtix.bus.management.jmx;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


public class JMXManagedComponent { 
    private static final Logger LOG = Logger.getLogger(JMXManagedComponent.class.getName());
    protected MBeanServer mbs;    
    protected ObjectName objectName;
    
    
    public JMXManagedComponent() {
        // TODO setup the gernal MBeanServer for it
        mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            objectName = new ObjectName("org.objectweb.celtix:Type=ManagedComponent");
        } catch (MalformedObjectNameException e) {
            LOG.log(Level.SEVERE, "MalformedObjectNameException", e);
        } catch (NullPointerException e) {
            LOG.log(Level.SEVERE, "NullPoniterException", e);
        } 
    }
    
    public ObjectName getObjectName() {
        return objectName;
    }
      
    public static ObjectName getObjectName(String name) {
        ObjectName o = null;
        try {
            o = new ObjectName("org.objectweb.celtix:Type=" + name);
        } catch (MalformedObjectNameException e) {
            LOG.log(Level.SEVERE, "MalformedObjectNameException", e);
        } catch (NullPointerException e) {
            LOG.log(Level.SEVERE, "NullPoniterException", e);
        }
        return o; 
    }
    
}
