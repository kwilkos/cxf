package org.objectweb.celtix.wsdl11;

import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.service.model.ServiceInfo;

public class WSDLServiceBuilderTest extends TestCase {
    
    private static final Logger LOG = Logger.getLogger(WSDLServiceBuilderTest.class.getName());
    private static final String WSDL_PATH = "/wsdl/hello_world.wsdl";
    private Definition def;
    private Service service;
    private WSDLFactory wsdlFactory;
    private WSDLReader wsdlReader;
    private Bus bus;
    private WSDLServiceBuilder wsdlServiceBuilder;
    private ServiceInfo serviceInfo;
    
    public void setUp() throws Exception {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        String wsdlUrl = getClass().getResource(WSDL_PATH).toString();
        LOG.info("the path of wsdl file is " + wsdlUrl);
        wsdlFactory = WSDLFactory.newInstance();
        wsdlReader = wsdlFactory.newWSDLReader();
        def = wsdlReader.readWSDL(wsdlUrl);
        bus = Bus.init();
        wsdlServiceBuilder = new WSDLServiceBuilder(bus);
        for (Service serv : WSDLServiceBuilder.cast(def.getServices().values(), Service.class)) {
            if (serv != null) {
                service = serv;
                break;
            }
        }
        serviceInfo = wsdlServiceBuilder.buildService(def, service);
    }
    
    public void tearDown() throws Exception {
        
    }
    
    public void testServiceInfo() throws Exception {
        assertEquals("SOAPService", serviceInfo.getName().getLocalPart());
        assertEquals("http://objectweb.org/hello_world_soap_http", serviceInfo.getName().getNamespaceURI());
        assertEquals("http://objectweb.org/hello_world_soap_http", serviceInfo.getTargetNamespace());
        assertTrue(serviceInfo.getProperty(WSDLServiceBuilder.WSDL_DEFINITION) == def);
        assertTrue(serviceInfo.getProperty(WSDLServiceBuilder.WSDL_SERVICE) == service);
    }
    
    public void testInterfaceInfo() throws Exception {
        assertEquals("Greeter", serviceInfo.getInterface().getName().getLocalPart());
    }
    
    public void testOperationInfo() throws Exception {
        OperationInfo sayHi = serviceInfo.getInterface().getOperation("sayHi"); 
        assertNotNull(sayHi);
        assertEquals(sayHi.getName(), "sayHi");
        assertFalse(sayHi.isOneWay());
        assertTrue(sayHi.hasInput());
        assertTrue(sayHi.hasOutput());
        
        OperationInfo greetMe = serviceInfo.getInterface().getOperation("greetMe");
        assertNotNull(greetMe);
        assertEquals(greetMe.getName(), "greetMe");
        assertFalse(greetMe.isOneWay());
        assertTrue(greetMe.hasInput());
        assertTrue(greetMe.hasOutput());
        
        OperationInfo greetMeOneWay = serviceInfo.getInterface().getOperation("greetMeOneWay");
        assertNotNull(greetMeOneWay);
        assertEquals(greetMeOneWay.getName(), "greetMeOneWay");
        assertTrue(greetMeOneWay.isOneWay());
        assertTrue(greetMeOneWay.hasInput());
        assertFalse(greetMeOneWay.hasOutput());
        
        OperationInfo pingMe = serviceInfo.getInterface().getOperation("pingMe");
        assertNotNull(pingMe);
        assertEquals(pingMe.getName(), "pingMe");
        assertFalse(pingMe.isOneWay());
        assertTrue(pingMe.hasInput());
        assertTrue(pingMe.hasOutput());
                
        assertNull(serviceInfo.getInterface().getOperation("what ever"));
    }
    
    public void testBindingInfo() throws Exception {
        BindingInfo bindingInfo = null;
        assertEquals(1, serviceInfo.getBindings().size());
        bindingInfo = serviceInfo.getBindings().iterator().next();
        assertNotNull(bindingInfo);
        assertEquals(bindingInfo.getInterface().getName().getLocalPart(), "Greeter");
        assertEquals(bindingInfo.getName().getLocalPart(), "Greeter_SOAPBinding");
        assertEquals(bindingInfo.getName().getNamespaceURI(), "http://objectweb.org/hello_world_soap_http");
    }
}
