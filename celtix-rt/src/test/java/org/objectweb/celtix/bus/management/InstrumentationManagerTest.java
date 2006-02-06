package org.objectweb.celtix.bus.management;

import java.util.List;
import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.busimpl.ComponentCreatedEvent;
import org.objectweb.celtix.bus.busimpl.ComponentRemovedEvent;

import org.objectweb.celtix.bus.workqueue.WorkQueueInstrumentation;
import org.objectweb.celtix.bus.workqueue.WorkQueueManagerImpl;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.management.InstrumentationManager;


public class InstrumentationManagerTest extends TestCase {
    Bus bus;
    InstrumentationManager im;    
    
    public void setUp() throws Exception {      
        bus = Bus.getCurrent();       
        im = bus.getInstrumentationManager();        
    }
    
    public void tearDown() throws Exception {
        
    }
    
    // try to get WorkQueue information
    public void testWorkQueueInstrumentation() throws BusException {
        //im.getAllInstrumentation();
        WorkQueueManagerImpl wqm = new WorkQueueManagerImpl(bus);
        bus.sendEvent(new ComponentCreatedEvent(wqm));        
        bus.sendEvent(new ComponentCreatedEvent(wqm));
        // TODO should test for the im getInstrumentation 
        List<Instrumentation> list = im.getAllInstrumentation();        
        assertTrue(list.size() == 2);
        Instrumentation it1 = list.get(0);
        Instrumentation it2 = list.get(1);
        assertTrue(WorkQueueInstrumentation.class.isAssignableFrom(it1.getClass()));
        assertTrue(WorkQueueInstrumentation.class.isAssignableFrom(it2.getClass()));
        
        assertTrue(it1.getUniqueInstrumentationName().compareTo(it1.getInstrumentationName() + 0) == 0);
        assertTrue(it2.getUniqueInstrumentationName().compareTo(it2.getInstrumentationName() + 1) == 0);
        
        bus.sendEvent(new ComponentRemovedEvent(wqm));
        assertTrue(list.size() == 0);
    }
      

}
