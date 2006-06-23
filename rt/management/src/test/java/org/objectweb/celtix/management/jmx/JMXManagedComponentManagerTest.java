package org.objectweb.celtix.management.jmx;

import javax.management.ObjectName;


import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.instrumentation.JMXConnectorPolicyType;
import org.objectweb.celtix.bus.instrumentation.MBServerPolicyType;
import org.objectweb.celtix.management.InstrumentationCreatedEvent;
import org.objectweb.celtix.management.InstrumentationRemovedEvent;
import org.objectweb.celtix.management.jmx.export.AnnotationTestInstrumentation;


public class JMXManagedComponentManagerTest extends TestCase {
    private static final String NAME_ATTRIBUTE = "Name";
    private static final String BUS_ID = "celtix";
    private Bus bus;    
    private JMXManagedComponentManager manager;    
    
    public void setUp() throws BusException {
        bus = EasyMock.createMock(Bus.class);
        EasyMock.reset(bus);
        bus.getBusID();
        EasyMock.expectLastCall().andReturn(BUS_ID);
        EasyMock.replay(bus);
        manager = new JMXManagedComponentManager(bus); 
    }
    
    public void tearDown() throws Exception {
        //?
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
    
    public void testJMXManagerProcessEvent() throws BusException {
        MBServerPolicyType policy = new MBServerPolicyType();
        JMXConnectorPolicyType connector = new JMXConnectorPolicyType();        
        policy.setJMXConnector(connector);        
        connector.setDaemon(false);
        connector.setThreaded(false);
        connector.setJMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9913/jmxrmi");
        manager.init(policy);
        // setup the fack instrumentation
        AnnotationTestInstrumentation im = new AnnotationTestInstrumentation();
        ObjectName name = JMXUtils.getObjectName(im.getInstrumentationName(), 
                                                 im.getUniqueInstrumentationName(), 
                                                 BUS_ID);
       
        im.setName("John Smith");          
        manager.processEvent(new InstrumentationCreatedEvent(im));
        
        try {            
            Object val = manager.getMBeanServer().getAttribute(name, NAME_ATTRIBUTE);
            assertEquals("Incorrect result", "John Smith", val);
            Thread.sleep(300);
        } catch (Exception ex) {            
            ex.printStackTrace();
            assertTrue("get instrumentation attribute error", false);
        }
        manager.processEvent(new InstrumentationRemovedEvent(im));
        manager.shutdown();
    }

}
