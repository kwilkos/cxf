package org.objectweb.celtix.handlers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerChainType;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerInitParamType;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerType;
import org.objectweb.celtix.bus.jaxws.configuration.types.ObjectFactory;

public class HandlerChainBuilderTest extends TestCase {

    Handler[] allHandlers = {EasyMock.createMock(LogicalHandler.class), EasyMock.createMock(Handler.class),
                             EasyMock.createMock(Handler.class), EasyMock.createMock(LogicalHandler.class)};
    Handler[] logicalHandlers = {allHandlers[0], allHandlers[3]};
    Handler[] protocolHandlers = {allHandlers[1], allHandlers[2]};

    HandlerChainBuilder builder = new HandlerChainBuilder(); 

    public void testChainSorting() {

        List<Handler> sortedHandlerChain = builder.sortHandlers(Arrays.asList(allHandlers));
        assertSame(logicalHandlers[0], sortedHandlerChain.get(0));
        assertSame(logicalHandlers[1], sortedHandlerChain.get(1));
        assertSame(protocolHandlers[0], sortedHandlerChain.get(2));
        assertSame(protocolHandlers[1], sortedHandlerChain.get(3));
    }

    public void testBuildHandlerChainFromConfiguration() {

        HandlerChainType hc = createHandlerChainType();
        List<Handler> chain = builder.buildHandlerChainFromConfiguration(hc);

        assertNotNull(chain);
        assertEquals(4, chain.size());
        assertEquals(TestLogicalHandler.class, chain.get(0).getClass());
        assertEquals(TestLogicalHandler.class, chain.get(1).getClass()); 
        assertEquals(TestProtocolHandler.class, chain.get(2).getClass()); 
        assertEquals(TestProtocolHandler.class, chain.get(3).getClass()); 
        
        TestLogicalHandler tlh = (TestLogicalHandler)chain.get(0);
        assertTrue(!tlh.initCalled);
        assertNull(tlh.config);
    }

    public void testBuilderCallsInit() {

        HandlerChainType hc = createHandlerChainType();
        hc.getHandler().remove(3);
        hc.getHandler().remove(2);
        hc.getHandler().remove(1);
        HandlerType h = hc.getHandler().get(0);
        List<HandlerInitParamType> params = h.getInitParam();
        HandlerInitParamType p = new ObjectFactory().createHandlerInitParamType();
        p.setParamName("foo");
        p.setParamValue("1");
        params.add(p);
        p = new ObjectFactory().createHandlerInitParamType();
        p.setParamName("bar");
        p.setParamValue("2");
        params.add(p);
        
        List<Handler> chain = builder.buildHandlerChainFromConfiguration(hc);
        assertEquals(1, chain.size());
        TestLogicalHandler tlh = (TestLogicalHandler)chain.get(0);

        assertTrue(tlh.initCalled);
        Map cfg = tlh.config;
        assertNotNull(tlh.config);

        assertEquals(2, cfg.keySet().size());
        Iterator iter = cfg.keySet().iterator();
        assertEquals("foo", iter.next());
        assertEquals("1", cfg.get("foo"));
        assertEquals("bar", iter.next());
        assertEquals("2", cfg.get("bar"));
    }

    public void testBuilderCallsInitWithNoInitParamValues() {

        HandlerChainType hc = createHandlerChainType();
        hc.getHandler().remove(3);
        hc.getHandler().remove(2);
        hc.getHandler().remove(1);
        HandlerType h = hc.getHandler().get(0);
        List<HandlerInitParamType> params = h.getInitParam();
        HandlerInitParamType p = new ObjectFactory().createHandlerInitParamType();
        p.setParamName("foo");
        params.add(p);
        p = new ObjectFactory().createHandlerInitParamType();
        p.setParamName("bar");
        params.add(p);
        
        List<Handler> chain = builder.buildHandlerChainFromConfiguration(hc);
        assertEquals(1, chain.size());
        TestLogicalHandler tlh = (TestLogicalHandler)chain.get(0);

        assertTrue(tlh.initCalled);
        Map cfg = tlh.config;
        assertNotNull(tlh.config);
        assertEquals(2, cfg.keySet().size());
    }

    public void testBuilderCannotLoadHandlerClass() {
        HandlerChainType hc = createHandlerChainType();
        hc.getHandler().remove(3);
        hc.getHandler().remove(2);
        hc.getHandler().remove(1);
        hc.getHandler().get(0).setHandlerClass("no.such.class");
        
        try {
            builder.buildHandlerChainFromConfiguration(hc);
            fail("did not get expected exception");
        } catch (WebServiceException ex) {
            // ex.printStackTrace();
            assertNotNull(ex.getCause());
            assertEquals(ClassNotFoundException.class, ex.getCause().getClass());
            // happy
        }
    }
    
    private HandlerChainType createHandlerChainType() {
        HandlerChainType hc = new ObjectFactory().createHandlerChainType();
        List<HandlerType> handlers = hc.getHandler();
        HandlerType h = new ObjectFactory().createHandlerType();
        h.setHandlerName("lh1");
        h.setHandlerClass(TestLogicalHandler.class.getName());
        handlers.add(h);
        h = new ObjectFactory().createHandlerType();
        h.setHandlerName("ph1");
        h.setHandlerClass(TestProtocolHandler.class.getName());
        handlers.add(h);
        h = new ObjectFactory().createHandlerType();
        h.setHandlerName("ph2");
        h.setHandlerClass(TestProtocolHandler.class.getName());
        handlers.add(h);
        h = new ObjectFactory().createHandlerType();
        h.setHandlerName("lh2");
        h.setHandlerClass(TestLogicalHandler.class.getName());
        handlers.add(h);
        return hc;
    }
    
    public static class TestLogicalHandler implements LogicalHandler {

        Map config; 
        boolean initCalled; 
        
        public void close(MessageContext arg0) {
        }

        public boolean handleFault(MessageContext arg0) {
            return false;
        }

        public boolean handleMessage(MessageContext arg0) {
            return false;
        }
        
        public final void init(final Map map) {
            config = map; 
            initCalled = true; 
        }
    }
    
    public static class TestProtocolHandler implements Handler {

        public void close(MessageContext arg0) {
        }

        public boolean handleFault(MessageContext arg0) {
            return false;
        }

        public boolean handleMessage(MessageContext arg0) {
            return false;
        }
    }

}
