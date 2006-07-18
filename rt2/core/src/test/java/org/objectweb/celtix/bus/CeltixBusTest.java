package org.objectweb.celtix.bus;

import java.util.List;

import junit.framework.TestCase;

import org.objectweb.celtix.phase.Phase;

public class CeltixBusTest extends TestCase {

    private CeltixBus bus;

    public void setUp() {
        bus = new CeltixBus();
    }

    public void testCreatePhases() {
        assertNull(bus.getInPhases());
        bus.createPhases();
        List<Phase> phases = bus.getInPhases();
        assertTrue(phases.size() > 0);
        phases = bus.getOutPhases();
        assertTrue(phases.size() > 0);     
    }
    
    public void testInitWithoutProperties() {
        
    }
    
    public void testInitWithProperties() {
        
    }
}
