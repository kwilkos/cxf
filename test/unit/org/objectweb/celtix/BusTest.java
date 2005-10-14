package org.objectweb.celtix;

import java.util.HashMap;
import java.util.Map;

import junit.framework.*;

import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.bus.busimpl.CeltixBus;

public class BusTest extends TestCase {

    public void testBusInit() throws Exception {
        Bus bus = Bus.init(null, new HashMap<String, Object>());
        assertNotNull(bus);
        assertTrue("Bus not a Celtix bus", bus instanceof CeltixBus);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Bus.BUS_CLASS_PROPERTY, "com.foo.bar.Bus");
        try {
            bus = Bus.init(null, map);            
            fail("Should have thrown an exception");
        } catch (BusException ex) {
            //ignore -expected
        } finally {
            bus.shutdown(true);
        }
    }
    
    /*
     * Test method for 'org.objectweb.celtix.Bus.getCurrent()'
     */    
    public void testBusGetCurrent() throws Exception {
        Bus bus1 = Bus.init(null, new HashMap<String, Object>());
        assertNotNull(bus1);
        assertTrue("Bus not a Celtix bus", bus1 instanceof CeltixBus);

        //Last Created bus should always be returned.
        
        assertSame("getCurrent should have returned the same bus handle.", bus1, Bus.getCurrent());

        //Create another bus
        Bus bus2 = Bus.init(null, new HashMap<String, Object>());
        assertNotSame("getCurrent should have returned a different bus handle.", bus1, Bus.getCurrent());
        assertSame(bus2, Bus.getCurrent());
        
        bus1.shutdown(true);
        bus2.shutdown(true);
    }    
    
    /*
     * Test method for 'org.objectweb.celtix.Bus.getCurrent()'
     */    
    public void testBusGetBindingManager() throws Exception {
        Bus bus = Bus.init(null, new HashMap<String, Object>());
        assertNotNull(bus);

        BindingManager bindingManager = bus.getBindingManager();
        assertNotNull(bindingManager);
        
        BindingFactory factory = bindingManager.getBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/");
        assertNotNull(factory);
        //Last Created bus should always be returned.
        bus.shutdown(true);
    }    
    
}


