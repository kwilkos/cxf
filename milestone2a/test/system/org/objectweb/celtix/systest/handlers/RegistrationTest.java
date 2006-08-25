package org.objectweb.celtix.systest.handlers;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;

import org.objectweb.celtix.bus.handlers.PortInfoImpl;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.SOAPService;

public class RegistrationTest extends ClientServerTestBase {
    
    private final QName serviceName = new QName("http://objectweb.org/hello_world_soap_http",
                                                "SOAPService");    
    private final QName portName = new QName("http://objectweb.org/hello_world_soap_http",
                                             "SoapPort");
    
    private URL wsdl;
    private SOAPService service;
    private Greeter greeter;
    
    public void setUp() {
        try { 
            super.setUp();
            
            wsdl = RegistrationTest.class.getResource("/wsdl/hello_world.wsdl");
            service = new SOAPService(wsdl, serviceName);
            greeter = (Greeter) service.getPort(portName, Greeter.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }
    
    
    public void testServerHandlerRegistrationOnBinding() {
        
    }
    

    /** 
     * test that a handler registered via Service HandlerResolver ends up 
     * in the port's handler chain
     *
     */
    public void testClientHandlerRegistrationOnService() {
        
        PortInfoImpl p1 = new PortInfoImpl(serviceName, portName, null);
        
        assertNotNull(service);
        HandlerResolver resolver = service.getHandlerResolver();        
        assertNotNull(resolver);

        List<Handler> handlers = resolver.getHandlerChain(p1);
        assertEquals(0, handlers.size());
        final TestHandler dummyHandler = new TestHandler();
        handlers.add(dummyHandler);
        assertEquals(1, handlers.size());
        
        Greeter g = service.getPort(portName, Greeter.class);
        assertTrue(g instanceof BindingProvider);
        
        List<Handler> bindingHandlers = ((BindingProvider)g).getBinding().getHandlerChain();
        assertNotNull(bindingHandlers);
        assertEquals(1, bindingHandlers.size());
        assertSame(dummyHandler, bindingHandlers.get(0));
    }
    
    public void testChangingServiceHandlerChainDoesNotAffectProxy() { 
        
        List<Handler> proxyHandlers = ((BindingProvider)greeter).getBinding().getHandlerChain();
        assertEquals(0, proxyHandlers.size());
        
        PortInfoImpl p1 = new PortInfoImpl(serviceName, portName, null);        
        HandlerResolver resolver = service.getHandlerResolver();        
        resolver.getHandlerChain(p1).add(new TestHandler());        
        assertEquals(0, proxyHandlers.size());
    }
   
}
