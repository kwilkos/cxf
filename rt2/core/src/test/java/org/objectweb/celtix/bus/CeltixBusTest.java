package org.objectweb.celtix.bus;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactoryManager;
import org.objectweb.celtix.event.EventProcessor;
import org.objectweb.celtix.management.InstrumentationManager;
import org.objectweb.celtix.messaging.ConduitInitiatorManager;
import org.objectweb.celtix.messaging.DestinationFactoryManager;
import org.objectweb.celtix.phase.PhaseManager;
import org.objectweb.celtix.wsdl.WSDLManager;

public class CeltixBusTest extends TestCase {

    
    public void testConstructionWithoutExtensions() throws BusException {
        
        CeltixBus bus = new CeltixBus();
        assertNotNull(bus.getExtension(BindingFactoryManager.class));
        assertNotNull(bus.getExtension(ConduitInitiatorManager.class));   
        assertNotNull(bus.getExtension(DestinationFactoryManager.class));
        assertNotNull(bus.getExtension(WSDLManager.class));
        assertNotNull(bus.getExtension(PhaseManager.class));
    }
    
    public void testConstructionWithExtensions() throws BusException {
        
        IMocksControl control;
        BindingFactoryManager bindingFactoryManager;
        WSDLManager wsdlManager;
        EventProcessor eventProcessor;
        InstrumentationManager instrumentationManager;
        PhaseManager phaseManager;
        
        control = EasyMock.createNiceControl();
        
        Map<Class, Object> properties = new HashMap<Class, Object>();
        bindingFactoryManager = control.createMock(BindingFactoryManager.class);
        wsdlManager = control.createMock(WSDLManager.class);
        eventProcessor = control.createMock(EventProcessor.class);
        instrumentationManager = control.createMock(InstrumentationManager.class);
        phaseManager = control.createMock(PhaseManager.class);
        
        properties.put(BindingFactoryManager.class, bindingFactoryManager);
        properties.put(WSDLManager.class, wsdlManager);
        properties.put(EventProcessor.class, eventProcessor);
        properties.put(InstrumentationManager.class, instrumentationManager);
        properties.put(PhaseManager.class, phaseManager);
        
        CeltixBus bus = new CeltixBus(properties);
        
        assertSame(bindingFactoryManager, bus.getExtension(BindingFactoryManager.class));
        assertSame(wsdlManager, bus.getExtension(WSDLManager.class));
        assertSame(eventProcessor, bus.getExtension(EventProcessor.class));
        assertSame(instrumentationManager, bus.getExtension(InstrumentationManager.class));
        assertSame(phaseManager, bus.getExtension(PhaseManager.class));
  
    }

    public void testExtensions() {
        CeltixBus bus = new CeltixBus();
        String extension = "CXF";
        bus.setExtension(extension, String.class);
        assertSame(extension, bus.getExtension(String.class));
    }
    

}
