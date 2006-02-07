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
        bus = Bus.init();        
        event = new BusEvent("Test for EventListener", BusEvent.BUS_EVENT);         
    }
    
    public void tearDown() throws Exception {
        bus.shutdown(true);
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
        
        assertEquals("The event cache getEvents size is not correct",
                     3, cache.getEvents().size());
        
        assertEquals("The event cache getEvents() size is not correct",
                     1, cache.getEvents("TEST").size());
                
        List<BusEvent> events = cache.flushEvents("TEST");
        
        assertEquals("The event cache flushEvent TEST events size is not correct", 
                   1, events.size());
        
        assertEquals("The event cache getID is not correct", 
                     0, events.get(0).getID().compareTo("TEST"));
        
        assertEquals("The event cache getEvents size is not correct", 
                     2, cache.getEvents().size());
        
        cache.flushEvents(BusEvent.class);
        assertEquals("After flush the event cache getEvents size is not correct", 
                     0, cache.getEvents().size());        
        
    }
    
}
