package org.objectweb.celtix.bus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactoryManager;
import org.objectweb.celtix.buslifecycle.BusLifeCycleManager;
import org.objectweb.celtix.event.EventProcessor;
import org.objectweb.celtix.management.InstrumentationManager;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.wsdl.WSDLManager;

public class CeltixBusTest extends TestCase {

    private CeltixBus bus;
    private IMocksControl control;
    private BindingFactoryManager bindingFactoryManager;
    private WSDLManager wsdlManager;
    private EventProcessor eventProcessor;
    private InstrumentationManager instrumentationManager;
    private BusLifeCycleManager lifecycleManager;
    

    public void setUp() {
        bus = new CeltixBus();
        control = EasyMock.createNiceControl();
    }
    
    public void testInitWithoutProperties() throws BusException {
        // don't test as this construction of a whole bunch of objects ...
    }
    
    public void testInitWithProperties() throws BusException {
        Map<String, Object> properties = new HashMap<String, Object>();
        bindingFactoryManager = control.createMock(BindingFactoryManager.class);
        wsdlManager = control.createMock(WSDLManager.class);
        eventProcessor = control.createMock(EventProcessor.class);
        instrumentationManager = control.createMock(InstrumentationManager.class);
        lifecycleManager = control.createMock(BusLifeCycleManager.class);
        properties.put("bindingFactoryManager", bindingFactoryManager);
        properties.put("wsdl11Manager", wsdlManager);
        properties.put("eventProcessor", eventProcessor);
        properties.put("instrumentationManager", instrumentationManager);
        properties.put("lifeCycleManager", lifecycleManager);
        
        bus.initialize(properties);
        
        assertSame(bindingFactoryManager, bus.getBindingManager());
        assertSame(wsdlManager, bus.getWSDL11Manager());
        assertSame(eventProcessor, bus.getEventProcessor());
        assertSame(instrumentationManager, bus.getInstrumentationManager());
        assertSame(lifecycleManager, bus.getLifeCycleManager());
    }

    public void testCreatePhases() {
        assertNull(bus.getInPhases());
        bus.createPhases();
        List<Phase> phases = bus.getInPhases();
        assertTrue(phases.size() > 0);
        phases = bus.getOutPhases();
        assertTrue(phases.size() > 0);     
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
