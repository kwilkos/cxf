package org.objectweb.celtix.bus.handlers;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;

import junit.framework.TestCase;

import org.easymock.EasyMock;

public class HandlerResolverImplTest extends TestCase {

    private HandlerResolverImpl resolver = new HandlerResolverImpl(); 
    

    public void testGetHandlerChain() { 
        //public List getHandlerChain(PortInfo portinfo);
        PortInfoImpl p = new PortInfoImpl(new QName("http://objectweb.org/hello_world_soap_http", "Greeter"),
                                          new QName("http://objectweb.org/hello_world_soap_http",
                                                    "SOAP_Service"),
                                          "Greeter_SOAPBinding");

        List<Handler> handlerChain = resolver.getHandlerChain(p);
        assertNotNull(handlerChain);
        assertEquals(0, handlerChain.size());
        
        Handler handler = EasyMock.createMock(Handler.class);
        handlerChain.add(handler);
        
        handlerChain = resolver.getHandlerChain(p);
        assertEquals(1, handlerChain.size());
        assertSame(handler, handlerChain.get(0));
    }
}
