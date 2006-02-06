package org.objectweb.celtix;

import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import static org.easymock.EasyMock.isA;

// test bus event handler
public class BusEventTest extends TestCase {
    Bus bus;
    
    BusEventListener bel;
    BusEventFilter bef;
    BusEvent event;
    
    public void setUp() throws Exception {
        bel = EasyMock.createMock(BusEventListener.class);
        bef = EasyMock.createMock(BusEventFilter.class);
        bus = Bus.getCurrent();
        event = new BusEvent("Test for EventListener", BusEvent.BUS_EVENT); 
        
    }
    
    public void tearDown() throws Exception {
        
    }
    
    public void testBusSendEvent() throws BusException {
        EasyMock.reset(bel);
        EasyMock.reset(bef);
        bef.isEventEnabled(isA(BusEvent.class));
        EasyMock.expectLastCall().andReturn(true);
        bel.processEvent(isA(BusEvent.class));
        EasyMock.expectLastCall();
        EasyMock.replay(bel);
        EasyMock.replay(bef);
        
        bus.addListener(bel, bef);
        bus.sendEvent(event);        
        bus.removeListener(bel);
        // this event should not be called
        bus.sendEvent(event);
        
        EasyMock.verify(bel);
        EasyMock.verify(bef);
    }
    
    public void testBusRemoveListener() throws BusException {
        bus.addListener(bel, bef);
        
        bus.removeListener(bel);
        
        bus.sendEvent(new BusEvent("Test for EventListener", BusEvent.BUS_EVENT));
        
    }
    
    // test for the get event cache
    public void testBusEventCache() throws BusException {
        
        BusEventCache cache = bus.getEventCache();
        
        BusEvent event1 = new BusEvent("Test for EventCache", BusEvent.BUS_EVENT);
        BusEvent event2 = new BusEvent("Test for EventCache", "TEST");
        
        cache.flushEvents();
        
        bus.sendEvent(event);
        bus.sendEvent(event1);
        bus.sendEvent(event2);
        
        assertTrue(cache.getEvents().size() == 3);
        
        assertTrue(cache.getEvents("TEST").size() == 1);
                
        List<BusEvent> events = cache.flushEvents("TEST");
        
        assertTrue(events.size() == 1);
        assertTrue(events.get(0).getID().compareTo("TEST") == 0);
        
        assertTrue(cache.getEvents().size() == 2);
        
        cache.flushEvents(BusEvent.class);
        assertTrue(cache.getEvents().size() == 0);        
        
    }
    
}
