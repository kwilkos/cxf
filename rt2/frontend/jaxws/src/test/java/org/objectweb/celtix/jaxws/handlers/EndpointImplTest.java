package org.objectweb.celtix.jaxws.handlers;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.CeltixBus;

public class EndpointImplTest extends TestCase {
    public void testEndpoint() throws Exception {
        
//        AnnotatedGreeterImpl greeter = new AnnotatedGreeterImpl();
//        
//        EndpointImpl ep = new EndpointImpl(getBus(), greeter, "");
//
//        assertFalse(ep.isPublished());
//        
//        Server server = ep.getServer();
//        assertNotNull(server);
    }

    Bus getBus() throws Exception {
        CeltixBus bus = new CeltixBus();
        
        return bus;
    }
}
