package org.objectweb.celtix.bus.jaxws;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.HandlerResolver;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.busimpl.CeltixBus;
import org.objectweb.celtix.workqueue.WorkQueueManager;

public class ServiceImplTest extends TestCase {

    public void testHandlerResolverAttribute() {
        QName sn = new QName("http://objectweb.org/hello_world_soap_http", "Greeter");
        Bus bus = EasyMock.createMock(CeltixBus.class);
        WorkQueueManager wm = EasyMock.createMock(WorkQueueManager.class);
        bus.getWorkQueueManager();
        EasyMock.expectLastCall().andReturn(wm);
        wm.getAutomaticWorkQueue();
        EasyMock.expectLastCall().andReturn(null);

        EasyMock.replay(bus);
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
