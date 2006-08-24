package org.apache.cxf.management.jmx;

import junit.framework.TestCase;

import org.apache.cxf.BusException;
import org.apache.cxf.configuration.instrumentation.types.JMXConnectorPolicyType;
import org.apache.cxf.configuration.instrumentation.types.MBServerPolicyType;


public class JMXManagedComponentManagerTest extends TestCase {
   
    private JMXManagedComponentManager manager;    
    
    public void setUp() throws BusException {
        manager = new JMXManagedComponentManager(); 
    }
        
    public void testJMXManagerInit() {
        MBServerPolicyType policy = new MBServerPolicyType();
        JMXConnectorPolicyType connector = new JMXConnectorPolicyType();        
        policy.setJMXConnector(connector);        
        connector.setDaemon(false);
        connector.setThreaded(true);
        connector.setJMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9913/jmxrmi");
        try {
            manager.init(policy); 
            Thread.sleep(300);
            manager.shutdown();
        } catch (Exception ex) {
            assertTrue("JMX Manager init with NewMBeanServer error", false);
            ex.printStackTrace();
        }
    }

}
