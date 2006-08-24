package org.apache.cxf.management.jmx;



import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import junit.framework.TestCase;



public class MBServerConnectorTest extends  TestCase {
    public void testMBServerConnector() {
        MBServerConnectorFactory mcf;    
        MBeanServer mbs;        
        mbs = MBeanServerFactory.createMBeanServer("test");            
        mcf = MBServerConnectorFactory.getInstance();
        mcf.setMBeanServer(mbs);
        mcf.setThreaded(true);
        mcf.setDaemon(true);
        mcf.setServiceUrl("service:jmx:rmi:///jndi/rmi://localhost:9913/jmxrmi");
        try {
            mcf.createConnector(); 
            Thread.sleep(1000);           
            mcf.destroy();
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse("Some Exception happen to MBServerConnectorTest", true);
        }
    }

}
