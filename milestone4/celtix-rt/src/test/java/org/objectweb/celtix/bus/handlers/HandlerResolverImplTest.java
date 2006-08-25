package org.objectweb.celtix.bus.handlers;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;

import junit.framework.TestCase;

import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerChainType;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerType;
import org.objectweb.celtix.bus.jaxws.configuration.types.ObjectFactory;
import org.objectweb.celtix.configuration.Configuration;

import static org.easymock.EasyMock.*;


public class HandlerResolverImplTest extends TestCase {

    private final HandlerResolverImpl resolver = new HandlerResolverImpl();
    private final PortInfoImpl portInfo = new PortInfoImpl(
        new QName("http://objectweb.org/hello_world_soap_http", "Greeter"),
        new QName("http://objectweb.org/hello_world_soap_http", "SOAP_Service"),
        "Greeter_SOAPBinding");

    public void testGetHandlerChain() { 
        List<Handler> handlerChain = resolver.getHandlerChain(portInfo);
        assertNotNull(handlerChain);
        assertEquals(0, handlerChain.size());
        
        Handler handler = createMock(Handler.class);
        handlerChain.add(handler);
        
        handlerChain = resolver.getHandlerChain(portInfo);
        assertEquals(1, handlerChain.size());
        assertSame(handler, handlerChain.get(0));
    }
    
    public void testGetHandlerChainFromConfiguration() {
        ObjectFactory factory = new ObjectFactory();
        HandlerType h1 = factory.createHandlerType();
        h1.setHandlerClass(getClass().getPackage().getName() + ".TestHandler");
        h1.setHandlerName("first");
        HandlerType h2 = factory.createHandlerType();
        h2.setHandlerClass(getClass().getPackage().getName() + ".TestHandler");
        h2.setHandlerName("second");
        
        HandlerChainType chain = factory.createHandlerChainType();
        List<HandlerType> handlers = chain.getHandler();
        handlers.add(h1);
        handlers.add(h2);
        
        Configuration serviceConf = createMock(Configuration.class);
        HandlerResolverImpl res = new HandlerResolverImpl(serviceConf);
        Configuration portConf = createMock(Configuration.class);
        serviceConf.getChild("http://celtix.objectweb.org/bus/jaxws/port-config", 
                             portInfo.getPortName().getLocalPart());
        expectLastCall().andReturn(portConf);
        portConf.getObject("handlerChain");
        expectLastCall().andReturn(chain);
        replay(serviceConf);
        replay(portConf);
        
        List<Handler> handlerChain = res.getHandlerChain(portInfo);
        assertNotNull(handlerChain);
        assertEquals(2, handlerChain.size());       
        verify(serviceConf);
        verify(portConf);
    }
    
    public void testHandlerClassNotFound() {
        ObjectFactory factory = new ObjectFactory();
        HandlerType h3 = factory.createHandlerType();
        h3.setHandlerClass("a.b.c.TestHandler");
        h3.setHandlerName("nonExistingClassHandler");
        
        HandlerChainType chain = factory.createHandlerChainType();
        List<HandlerType> handlers = chain.getHandler();
        handlers.add(h3);
        
        Configuration serviceConf = createMock(Configuration.class);
        HandlerResolverImpl res = new HandlerResolverImpl(serviceConf);
        Configuration portConf = createMock(Configuration.class);
        serviceConf.getChild("http://celtix.objectweb.org/bus/jaxws/port-config", 
                             portInfo.getPortName().getLocalPart());
        expectLastCall().andReturn(portConf);
        portConf.getObject("handlerChain");
        expectLastCall().andReturn(chain);
        replay(serviceConf);
        replay(portConf);
        
        try {
            res.getHandlerChain(portInfo);
        } catch (WebServiceException ex) {
            assertTrue(ex.getCause() instanceof ClassNotFoundException);
        }
        verify(serviceConf);
        verify(portConf);
    }
    
    /*
    public void testHandlerIllegalAccess() {
        ObjectFactory factory = new ObjectFactory();
        HandlerType h4 = factory.createHandlerType();
        h4.setClassName("org.objectweb.celtix.bus.jaxb.JAXBUtils");
        h4.setName("privateConstructor");
        
        HandlerChainType chain = factory.createHandlerChainType();
        List<HandlerType> handlers = chain.getHandler();
        handlers.add(h4);
        
        Configuration conf = createMock(Configuration.class);
        HandlerResolverImpl res = new HandlerResolverImpl(conf);
        conf.getObject("handlerChain");
        expectLastCall().andReturn(chain);
        replay(conf);
        
        try {
            res.getHandlerChain(portInfo);
        } catch (WebServiceException ex) {
            assertTrue(ex.getCause() instanceof IllegalAccessException);
        }
        verify(conf);
    }
    */
    
    public void testHandlerInstantiation() {
        ObjectFactory factory = new ObjectFactory();
        HandlerType h5 = factory.createHandlerType();
        h5.setHandlerClass("javax.xml.ws.handler.Handler");
        h5.setHandlerName("interfaceHandler");
        
        HandlerChainType chain = factory.createHandlerChainType();
        List<HandlerType> handlers = chain.getHandler();
        handlers.add(h5);
        
        Configuration serviceConf = createMock(Configuration.class);
        HandlerResolverImpl res = new HandlerResolverImpl(serviceConf);
        Configuration portConf = createMock(Configuration.class);
        serviceConf.getChild("http://celtix.objectweb.org/bus/jaxws/port-config", 
                             portInfo.getPortName().getLocalPart());
        expectLastCall().andReturn(portConf);
        portConf.getObject("handlerChain");
        expectLastCall().andReturn(chain);
        replay(serviceConf);
        replay(portConf);
        
        try {
            res.getHandlerChain(portInfo);
        } catch (WebServiceException ex) {
            assertTrue(ex.getCause() instanceof InstantiationException);
        }
        verify(serviceConf);
        verify(portConf);
    }
}
