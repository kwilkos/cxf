package org.apache.cxf.bus;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.cxf.BusException;
import org.apache.cxf.bindings.BindingFactoryManager;
import org.apache.cxf.bus.CXFBus.State;
import org.apache.cxf.configuration.Configuration;
import org.apache.cxf.event.EventProcessor;
import org.apache.cxf.management.InstrumentationManager;
import org.apache.cxf.messaging.ConduitInitiatorManager;
import org.apache.cxf.messaging.DestinationFactoryManager;
import org.apache.cxf.phase.PhaseManager;
import org.apache.cxf.wsdl.WSDLManager;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

public class CXFBusTest extends TestCase {

    
    public void testConstructionWithoutExtensions() throws BusException {
        
        CXFBus bus = new CXFBus();
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
        
        CXFBus bus = new CXFBus(properties);
        
        assertSame(bindingFactoryManager, bus.getExtension(BindingFactoryManager.class));
        assertSame(wsdlManager, bus.getExtension(WSDLManager.class));
        assertSame(eventProcessor, bus.getExtension(EventProcessor.class));
        assertSame(instrumentationManager, bus.getExtension(InstrumentationManager.class));
        assertSame(phaseManager, bus.getExtension(PhaseManager.class));
  
    }

    public void testExtensions() {
        CXFBus bus = new CXFBus();
        String extension = "CXF";
        bus.setExtension(extension, String.class);
        assertSame(extension, bus.getExtension(String.class));
    }
    
    public void testConfiguration() {
        CXFBus bus = new CXFBus();
        Configuration c = bus.getConfiguration();
        assertTrue("Unexpected value for servicesMonitoring property.",
                   !c.getBoolean("servicesMonitoring"));
    }
    
    public void testRun() {
        final CXFBus bus = new CXFBus();
        Thread t = new Thread() {
            public void run() {
                bus.run();
            }
        };
        t.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            // ignore;
        }
        try {
            t.join(400);
        } catch (InterruptedException ex) {
            // ignore
        }
        assertEquals(State.RUNNING, bus.getState());
    }
    
    public void testShutdown() {
        final CXFBus bus = new CXFBus();
        Thread t = new Thread() {
            public void run() {
                bus.run();
            }
        };
        t.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            // ignore;
        }
        bus.shutdown(true);
        try {
            t.join();
        } catch (InterruptedException ex) {
            // ignore
        }
        assertEquals(State.SHUTDOWN, bus.getState());
        
    }
    
    

}
