package org.objectweb.celtix.bus;

import javax.xml.ws.handler.HandlerResolver;

import junit.framework.TestCase;

import org.easymock.EasyMock;

public class ServiceImplTest extends TestCase {

    public void testHandlerResolverAttribute() {
        
        HandlerResolver hr = EasyMock.createMock(HandlerResolver.class);
        ServiceImpl s = new ServiceImpl(null, null, null, null);
        s.setHandlerResolver(hr);
        assertSame(hr, s.getHandlerResolver());
    }   
}
