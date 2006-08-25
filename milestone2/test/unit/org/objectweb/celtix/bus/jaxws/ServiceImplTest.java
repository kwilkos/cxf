package org.objectweb.celtix.bus.jaxws;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.HandlerResolver;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.busimpl.CeltixBus;

public class ServiceImplTest extends TestCase {

    public void testHandlerResolverAttribute() {
        
        Bus bus = org.easymock.classextension.EasyMock.createMock(CeltixBus.class);        
        bus.getConfiguration();
        org.easymock.classextension.EasyMock.expectLastCall().andReturn(null);   
        ServiceImpl s = new ServiceImpl(bus, 
                                        null, 
                                        new QName("http://objectweb.org/hello_world_soap_http", "Greeter"), 
                                        null);
        
        HandlerResolver defaultResolver = s.getHandlerResolver();
        assertNotNull(defaultResolver);
        
        HandlerResolver hr = EasyMock.createMock(HandlerResolver.class);
        
        s.setHandlerResolver(hr);
        assertSame(hr, s.getHandlerResolver());
        assertTrue(defaultResolver != hr);
        
    }   
}
