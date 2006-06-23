package org.objectweb.celtix.bus.busimpl;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;

public class CeltixBusInstrumentationTest extends TestCase {
    CeltixBus bus;
    CeltixBusInstrumentation cbi;
    
    public void setUp() throws Exception {
        bus = (CeltixBus)Bus.init(); 
        cbi = new CeltixBusInstrumentation(bus);
    } 
    
    public void tearDown() throws Exception {
        bus.shutdown(true);        
    }
    
    public void testGetFactories() throws Exception {
        String[] bindingFactories = cbi.getBindingFactories();
        assertEquals("bindingFactories number is wrong ", 5, bindingFactories.length);
        
        String[] transportFactories = cbi.getTransportFactories();
        assertEquals("transportFactories number is wrong ", 6, transportFactories.length);
    }
   

}
