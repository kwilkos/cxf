package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerChainType;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerType;
import org.objectweb.celtix.bus.jaxws.configuration.types.ObjectFactory;
import org.objectweb.celtix.bus.jaxws.configuration.types.SystemHandlerChainType;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.context.StreamMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;
import org.objectweb.celtix.handlers.StreamHandler;
import org.objectweb.celtix.handlers.SystemHandler;


import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

public class AbstractBindingImplTest extends TestCase {
    private Handler lhs1;
    private Handler lhs2;
    private Handler lhs3;
    private Handler phs1;
    private Handler phs2;
    private Handler phs3;
    
    private Handler lh2;
    private Handler lh1;
    private Handler ph2;
    private Handler ph1;
    private Handler sh1; 
    
    private List<Handler> userHandlers;
        
    public void setUp() {
        
        lhs1 = new TestLogicalSystemHandler();
        lhs2 = new TestLogicalSystemHandler();
        lhs3 = new TestLogicalSystemHandler();
        phs1 = new TestProtocolSystemHandler();
        phs2 = new TestProtocolSystemHandler();
        phs3 = new TestProtocolSystemHandler();
        
        lh1 = new TestLogicalSystemHandler();
        lh2 = new TestLogicalSystemHandler();
        ph1 = new TestProtocolSystemHandler();
        ph2 = new TestProtocolSystemHandler();
        sh1 = new TestStreamHandler();
        
        userHandlers = new ArrayList<Handler>();
        userHandlers.add(sh1);
        userHandlers.add(ph1);
        userHandlers.add(lh1);
        userHandlers.add(lh2); 
        userHandlers.add(ph2);          
    }
    
    public void testSystemHandlerAccessors() {
        TestBinding b = new TestBinding();
        
        List<Handler> preLogical = b.getPreLogicalSystemHandlers();         
        assertEquals(0, preLogical.size());
        List<Handler> postLogical = b.getPostLogicalSystemHandlers();         
        assertEquals(0, postLogical.size());
        List<Handler> preProtocol = b.getPreProtocolSystemHandlers();         
        assertEquals(0, preProtocol.size());
        List<Handler> postProtocol = b.getPostProtocolSystemHandlers();         
        assertEquals(0, postProtocol.size());
        
        setSystemHandlers(b);

        preLogical = b.getPreLogicalSystemHandlers(); 
        assertEquals(2, preLogical.size());
        postLogical = b.getPostLogicalSystemHandlers(); 
        assertEquals(1, postLogical.size());
        preProtocol = b.getPreProtocolSystemHandlers(); 
        assertEquals(1, preProtocol.size());
        postProtocol = b.getPostProtocolSystemHandlers(); 
        assertEquals(2, postProtocol.size());
    }
    
    public void testGetHandlerChain() {    
        TestBinding b = new TestBinding();
        List<Handler> handlers;
        
        assertNull(b.getHandlerChain(false));
        assertEquals(0, b.getHandlerChain(true).size()); 
               
        setSystemHandlers(b);
        
        assertNull(b.getHandlerChain(false));
        handlers = b.getHandlerChain(true);
        assertEquals(6, handlers.size());
        assertTrue(lhs1 == handlers.get(0));
        assertTrue(lhs2 == handlers.get(1));
        assertTrue(lhs3 == handlers.get(2));
        assertTrue(phs1 == handlers.get(3));
        assertTrue(phs2 == handlers.get(4));
        assertTrue(phs3 == handlers.get(5));
        
        b.setHandlerChain(userHandlers);
        
        assertEquals(5, b.getHandlerChain(false).size());
        handlers = b.getHandlerChain(true);
        assertEquals(11, handlers.size());
        assertTrue(lhs1 == handlers.get(0));
        assertTrue(lhs2 == handlers.get(1));
        assertTrue(lh1 == handlers.get(2));
        assertTrue(lh2 == handlers.get(3));
        assertTrue(lhs3 == handlers.get(4));
        assertTrue(phs1 == handlers.get(5));
        assertTrue(ph1 == handlers.get(6)); 
        assertTrue(ph2 == handlers.get(7)); 
        assertTrue(phs2 == handlers.get(8)); 
        assertTrue(phs3 == handlers.get(9)); 
        assertTrue(sh1 == handlers.get(10));     
    }
    
    public void testConfigureSystemHandlers() {
        
        TestBinding b = new TestBinding();
        SystemHandlerChainType shc = null;
        Configuration c = createMock(Configuration.class);
        c.getObject("systemHandlerChain");
        expectLastCall().andReturn(shc);
        replay(c);
        
        b.configureSystemHandlers(c);
        assertEquals(0, b.getPreLogicalSystemHandlers().size());
        assertEquals(0, b.getPostLogicalSystemHandlers().size());
        assertEquals(0, b.getPreProtocolSystemHandlers().size());
        assertEquals(0, b.getPreProtocolSystemHandlers().size());
        
        verify(c);
        reset(c);
        
        shc = new ObjectFactory().createSystemHandlerChainType();
        c = createMock(Configuration.class);
        c.getObject("systemHandlerChain");
        expectLastCall().andReturn(shc);
        replay(c);
        
        b.configureSystemHandlers(c);
        assertEquals(0, b.getPreLogicalSystemHandlers().size());
        assertEquals(0, b.getPostLogicalSystemHandlers().size());
        assertEquals(0, b.getPreProtocolSystemHandlers().size());
        assertEquals(0, b.getPreProtocolSystemHandlers().size());
        
        verify(c);
        reset(c);
        
        shc = createSystemHandlerChain();
        c = createMock(Configuration.class);
        c.getObject("systemHandlerChain");
        expectLastCall().andReturn(shc);
        replay(c);
        
        b.configureSystemHandlers(c);
        assertEquals(2, b.getPreLogicalSystemHandlers().size());
        assertEquals(1, b.getPostLogicalSystemHandlers().size());
        assertEquals(1, b.getPreProtocolSystemHandlers().size());
        assertEquals(2, b.getPostProtocolSystemHandlers().size());
        
        verify(c);       
    }
    
    private SystemHandlerChainType createSystemHandlerChain() {
        SystemHandlerChainType shc = new ObjectFactory().createSystemHandlerChainType();
        HandlerChainType hc = new ObjectFactory().createHandlerChainType();
        List<HandlerType> handlers = hc.getHandler();
        HandlerType h = new ObjectFactory().createHandlerType();
        h.setHandlerName("lhs1");
        h.setHandlerClass(TestLogicalSystemHandler.class.getName());
        handlers.add(h);
        h = new ObjectFactory().createHandlerType();
        h.setHandlerName("lhs2");
        h.setHandlerClass(TestLogicalSystemHandler.class.getName());
        handlers.add(h);
        shc.setPreLogical(hc);
        
        hc = new ObjectFactory().createHandlerChainType();
        handlers = hc.getHandler();
        h = new ObjectFactory().createHandlerType();
        h.setHandlerName("lhs3");
        h.setHandlerClass(TestLogicalSystemHandler.class.getName());
        handlers.add(h);
        shc.setPostLogical(hc);
        
        hc = new ObjectFactory().createHandlerChainType();
        handlers = hc.getHandler();
        h = new ObjectFactory().createHandlerType();
        h.setHandlerName("phs1");
        h.setHandlerClass(TestProtocolSystemHandler.class.getName());
        handlers.add(h);
        shc.setPreProtocol(hc);
        
        hc = new ObjectFactory().createHandlerChainType();
        handlers = hc.getHandler();
        h = new ObjectFactory().createHandlerType();
        h.setHandlerName("phs2");
        h.setHandlerClass(TestProtocolSystemHandler.class.getName());
        handlers.add(h);
        h = new ObjectFactory().createHandlerType();
        h.setHandlerName("phs3");
        h.setHandlerClass(TestProtocolSystemHandler.class.getName());
        handlers.add(h);
        shc.setPostProtocol(hc);
        
        return shc; 
    }
    
    private void setSystemHandlers(TestBinding b) {
        b.getPreLogicalSystemHandlers().add(lhs1);
        b.getPreLogicalSystemHandlers().add(lhs2);
        b.getPostLogicalSystemHandlers().add(lhs3);
        b.getPreProtocolSystemHandlers().add(phs1);
        b.getPostProtocolSystemHandlers().add(phs2);
        b.getPostProtocolSystemHandlers().add(phs3);
    }
    
    static class TestBinding extends AbstractBindingImpl {

        public MessageContext createBindingMessageContext(MessageContext orig) {
            return null;
        }

        public HandlerInvoker createHandlerInvoker() {
            return null;
        }

        public void marshal(ObjectMessageContext objContext, MessageContext context, 
                               DataBindingCallback callback) {        
        }
        
        public void marshalFault(ObjectMessageContext objContext, MessageContext context, 
                               DataBindingCallback callback) {        
        }

        public void unmarshal(MessageContext context, ObjectMessageContext objContext, 
                                 DataBindingCallback callback) {
        }
        
        public void unmarshalFault(MessageContext context, ObjectMessageContext objContext, 
                                      DataBindingCallback callback) {
        }

        public void read(InputStreamMessageContext inContext, MessageContext msgContext)
            throws IOException {    
        }

        public void write(MessageContext msgContext, OutputStreamMessageContext outContext)
            throws IOException {
        }
       
    }
    
    public static class TestProtocolSystemHandler implements Handler, SystemHandler {

        public void close(MessageContext arg0) {     
        }

        public boolean handleFault(MessageContext arg0) {
            return false;
        }

        public boolean handleMessage(MessageContext arg0) {
            return false;
        }
        
    }

    public static class TestLogicalSystemHandler implements LogicalHandler, SystemHandler {
        
        public void close(MessageContext arg0) {
        }

        public boolean handleFault(MessageContext arg0) {
            return false;
        }

        public boolean handleMessage(MessageContext arg0) {
            return false;
        }
    }
    
    public static class TestStreamHandler implements StreamHandler {

        public void close(MessageContext arg0) {    
        }

        public boolean handleFault(StreamMessageContext arg0) {
            return false;
        }

        public boolean handleMessage(StreamMessageContext arg0) {
            return false;
        }
        
    }
  
}
