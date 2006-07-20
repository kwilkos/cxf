package org.objectweb.celtix.bus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactoryManager;
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
    
    public void testResourceInjection() {
        Collection<Object> objs = new ArrayList<Object>();
        Map<String, Object> props = bus.getProperties();
 
        BindingFactoryManager bfm = EasyMock.createMock(BindingFactoryManager.class);
        props.put(CeltixBus.BINDINGFACTORYMANAGER_PROPERTY_NAME, bfm);
        BusService bs = new BusService();
        objs.add(bs);

        bus.injectResources(objs, true);
        
        assertSame("Bus was not injected", bus, bs.bus);
        assertSame("Bus was not injected", bus, bs.otherBus);
        assertSame("BindingFactoryManager was not injected", bfm, bs.bindingFactoryManager);
        assertNull("Unexpected injection of BindingFactoryManager", bs.bindingManager); 
        
        bs = new BusService(); 
        objs.clear();
        objs.add(bs);
        
        bus.injectResources(objs, false);
        
        assertSame("Bus was not injected", bus, bs.bus);
        assertSame("Bus was not injected", bus, bs.otherBus);
        assertNull("Unexpected injection of BindingFactoryManager", bs.bindingFactoryManager);
        assertNull("Unexpected injection of BindingFactoryManager", bs.bindingManager);
        
    }
    
    class BusService {
        
        @Resource()
        Bus bus;
        
        @Resource(name = "bus")
        Bus otherBus;
        
        @Resource 
        BindingFactoryManager bindingFactoryManager;
        
        @Resource 
        BindingFactoryManager bindingManager;
        
        
        
        
    }
}
