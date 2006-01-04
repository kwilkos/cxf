package org.objectweb.celtix;



import java.util.HashMap;
import java.util.Map;
import javax.xml.ws.Holder;
import junit.framework.*;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.bus.busimpl.CeltixBus;

public class BusTest extends TestCase {

    public void tearDown() { 
        Bus.clearCurrent();
        Bus.clearDefault();
    } 

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

        assertSame("getCurrent should have returned the same bus handle.", bus1, Bus.getCurrent());

        //Create another bus
        Bus bus2 = Bus.init(null, new HashMap<String, Object>());
        assertNotSame("getCurrent should have returned a different bus handle.", bus1, Bus.getCurrent());
        assertSame("last bus initilialised should be the current bus ", bus2, Bus.getCurrent());
        
        bus1.shutdown(true);
        bus2.shutdown(true);
    }    
    
    public void testBusGetCurrentDefaultMulitpleThreads() throws Exception { 

        final Bus bus1 = Bus.getCurrent();

        Thread t = new Thread() { 
                public void run() { 
                    Bus bus2 = Bus.getCurrent(); 
                    assertSame("default bus not visible on all threads", bus1, bus2); 
                }
            };

        t.start(); 
        t.join();
    } 

    public void testBusGetCurrentPreInitMulitpleThreads() throws Exception { 

        final Bus bus1 = Bus.init(null, new HashMap<String, Object>());
        assertNotNull(bus1);
        assertTrue("Bus not a Celtix bus", bus1 instanceof CeltixBus);

        //Last Created bus should always be returned.
        assertSame("getCurrent should have returned the same bus handle.", bus1, Bus.getCurrent());

        final Holder<Bus> busHolder = new Holder<Bus>(); 
        Thread t = new Thread() { 
                public void run() { 
                    busHolder.value = Bus.getCurrent(); 
                }
            };

        t.start(); 
        t.join();
        
        assertSame("initialised bus not visible on all threads", bus1, busHolder.value); 
    } 


    public void testBusGetCurrentDefault() throws Exception { 
        
        Bus bus1 = Bus.getCurrent(); 
        assertNotNull("getCurrent did not return default bus", bus1); 
        Bus bus2 = Bus.getCurrent(); 
        assertNotNull("getCurrent did not return default bus", bus2); 
        assertSame("calls to get default bus returned different buses", bus1, bus2);

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


