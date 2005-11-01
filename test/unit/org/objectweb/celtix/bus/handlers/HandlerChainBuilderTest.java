package org.objectweb.celtix.bus.handlers;


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jws.HandlerChain;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.objectweb.hello_world_soap_http.AnnotatedGreeterImpl;

public class HandlerChainBuilderTest extends TestCase {

    Handler[] allHandlers = {EasyMock.createMock(LogicalHandler.class),
                             EasyMock.createMock(Handler.class),
                             EasyMock.createMock(Handler.class),
                             EasyMock.createMock(LogicalHandler.class)};
    Handler[] logicalHandlers = {allHandlers[0], allHandlers[3]}; 
    Handler[] protocolHandlers = {allHandlers[1], allHandlers[2]}; 
    AnnotatedGreeterImpl greeterImpl = new AnnotatedGreeterImpl(); 

    HandlerChainBuilder builder = new HandlerChainBuilder();

    public void testChainSorting() {

        List<Handler> sortedHandlerChain = builder.sortHandlers(Arrays.asList(allHandlers));
        assertSame(logicalHandlers[0], sortedHandlerChain.get(0));
        assertSame(logicalHandlers[1], sortedHandlerChain.get(1));
        assertSame(protocolHandlers[0], sortedHandlerChain.get(2));
        assertSame(protocolHandlers[1], sortedHandlerChain.get(3));
    }


    public void testBuildHandlerChain() { 

        List<Handler> chain = builder.buildHandlerChainFor(greeterImpl); 
        
        assertNotNull(chain); 
        assertEquals(1, chain.size()); 
        assertTrue(chain.get(0) instanceof Handler);
    } 

    public void testBuilderCallsInit() { 
        
        List<Handler> chain = builder.buildHandlerChainFor(greeterImpl); 
        
        assertEquals(1, chain.size()); 
        assertEquals(DummyHandler.class, chain.get(0).getClass()); 
        DummyHandler dh = (DummyHandler)chain.get(0);

        assertNotNull(dh.getConfig()); 
        Map cfg = dh.getConfig(); 
        
        assertEquals(2, cfg.keySet().size());
        Iterator iter = cfg.keySet().iterator();
        assertEquals("foo", iter.next()); 
        assertEquals("1", cfg.get("foo")); 
        assertEquals("bar", iter.next()); 
        assertEquals("2", cfg.get("bar")); 
    } 

    public void testBuilderCallsInitWithNoInitParams() { 

        List<Handler> chain = builder.buildHandlerChainFor(new HandlerChainNoInit()); 
        assertEquals(1, chain.size()); 
        Handler h = chain.get(0); 
        assertNotNull(h); 
        assertEquals(h.getClass(), DummyHandler.class); 
        assertTrue(((DummyHandler)h).initCalled()); 
        assertEquals(0, ((DummyHandler)h).getConfig().keySet().size()); 
    } 


    public void testBuildHandlerChainInvalidFile() { 

        try { 
            builder.buildHandlerChainFor(new InvalidAnnotation()); 
            fail("did not get expected exception"); 
        } catch (WebServiceException ex) { 
            // happy
        } 
    } 

    public void testBuildHandlerChainInvalidHandlerList() {

        try { 
            builder.buildHandlerChainFor(new InvalidName()); 
            fail("did not get expected exception"); 
        } catch (WebServiceException ex) { 
            // happy
        } 
    }

    public void testBuilderCannotLoadHandlerClass() { 

        try { 
            builder.buildHandlerChainFor(new NoSuchClassName()); 
            fail("did not get expected exception"); 
        } catch (WebServiceException ex) { 
            assertNotNull(ex.getCause()); 
            assertEquals(ClassNotFoundException.class, ex.getCause().getClass());
            // happy
        } 
    } 
    
}


@HandlerChain(file = "invalid_annotation.xml", name = "yoohoo")
class InvalidAnnotation extends AnnotatedGreeterImpl {
}


@HandlerChain(file = "handlers.xml", name = "yoohoo")
class InvalidName extends AnnotatedGreeterImpl {
}

@HandlerChain(file = "./broken_handlers.xml", name = "BrokenHandlerChain")
class NoSuchClassName extends AnnotatedGreeterImpl {
}

@HandlerChain(file = "../../../hello_world_soap_http/handlers.xml", name = "HandlerChainNoInitParam")
class HandlerChainNoInit extends AnnotatedGreeterImpl {
}

