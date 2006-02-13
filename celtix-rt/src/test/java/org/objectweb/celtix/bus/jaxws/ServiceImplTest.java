package org.objectweb.celtix.bus.jaxws;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.HandlerResolver;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.busimpl.CeltixBus;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.workqueue.WorkQueueManager;

public class ServiceImplTest extends TestCase {

    public void testHandlerResolverAttribute() {
        QName sn = new QName("http://objectweb.org/hello_world_soap_http", "Greeter");
        Bus bus = org.easymock.classextension.EasyMock.createMock(CeltixBus.class); 
        Configuration bc = EasyMock.createMock(Configuration.class);
        bus.getConfiguration();
        org.easymock.classextension.EasyMock.expectLastCall().andReturn(bc);
        bc.getChild("http://celtix.objectweb.org/bus/jaxws/service-config", sn.toString());
        EasyMock.expectLastCall().andReturn(null);
        WorkQueueManager wm = EasyMock.createMock(WorkQueueManager.class);
        bus.getWorkQueueManager();
        EasyMock.expectLastCall().andReturn(wm);
        wm.getAutomaticWorkQueue();
        EasyMock.expectLastCall().andReturn(null);
        
        org.easymock.classextension.EasyMock.replay(bus);
        EasyMock.replay(bc);
        EasyMock.replay(wm);
       
        ServiceImpl s = new ServiceImpl(bus, 
                                        null, 
                                        sn, 
                                        null);
        
        HandlerResolver defaultResolver = s.getHandlerResolver();
        assertNotNull(defaultResolver);
        
        HandlerResolver hr = EasyMock.createMock(HandlerResolver.class);
        
        s.setHandlerResolver(hr);
        assertSame(hr, s.getHandlerResolver());
        assertTrue(defaultResolver != hr);
        
    }   
}
