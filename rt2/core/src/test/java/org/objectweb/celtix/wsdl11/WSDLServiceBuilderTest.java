package org.objectweb.celtix.wsdl11;


import java.util.Collection;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import junit.framework.TestCase;


import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactoryManager;
import org.objectweb.celtix.service.model.BindingFaultInfo;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingMessageInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.service.model.ServiceInfo;

public class WSDLServiceBuilderTest extends TestCase {
    
    private static final Logger LOG = Logger.getLogger(WSDLServiceBuilderTest.class.getName());
    private static final String WSDL_PATH = "/wsdl/hello_world.wsdl";
    private Definition def;
    private Service service;    
    private ServiceInfo serviceInfo;
    
    private IMocksControl control;
    private Bus bus;
    private BindingFactoryManager bindingFactoryManager;
    
    public void setUp() throws Exception {
            
        String wsdlUrl = getClass().getResource(WSDL_PATH).toString();
        LOG.info("the path of wsdl file is " + wsdlUrl);
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        def = wsdlReader.readWSDL(wsdlUrl);

        WSDLServiceBuilder wsdlServiceBuilder = new WSDLServiceBuilder(bus);
        for (Service serv : WSDLServiceBuilder.cast(def.getServices().values(), Service.class)) {
            if (serv != null) {
                service = serv;
                break;
            }
        }
        
        control = EasyMock.createNiceControl();
        bus = control.createMock(Bus.class);
        bindingFactoryManager = control.createMock(BindingFactoryManager.class);
        wsdlServiceBuilder = new WSDLServiceBuilder(bus);
        
        EasyMock.expect(bus.getBindingManager()).andReturn(bindingFactoryManager);
        
        control.replay();
        serviceInfo = wsdlServiceBuilder.buildService(def, service);
    }
    
    public void tearDown() throws Exception {
        control.verify(); 
    }
    
    public void testServiceInfo() throws Exception {
        assertEquals("SOAPService", serviceInfo.getName().getLocalPart());
        assertEquals("http://objectweb.org/hello_world_soap_http", serviceInfo.getName().getNamespaceURI());
        assertEquals("http://objectweb.org/hello_world_soap_http", serviceInfo.getTargetNamespace());
        assertTrue(serviceInfo.getProperty(WSDLServiceBuilder.WSDL_DEFINITION) == def);
        assertTrue(serviceInfo.getProperty(WSDLServiceBuilder.WSDL_SERVICE) == service);
        
        assertEquals("Incorrect number of endpoints", serviceInfo.getEndpoints().size(), 1);
        EndpointInfo ei = serviceInfo.getEndpoint("SoapPort");
        assertNotNull(ei);
        assertEquals("http://schemas.xmlsoap.org/wsdl/soap/", ei.getNamespaceURI());
        assertNotNull(ei.getBinding());
    }
    
    public void testInterfaceInfo() throws Exception {
        assertEquals("Greeter", serviceInfo.getInterface().getName().getLocalPart());
    }
    
    public void testOperationInfo() throws Exception {
        
        assertEquals(serviceInfo.getInterface().getOperations().size(), 4);
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
    
    public void testBindingOperationInfo() throws Exception {
        BindingInfo bindingInfo = null;
        bindingInfo = serviceInfo.getBindings().iterator().next();
        Collection<BindingOperationInfo> bindingOperationInfos = bindingInfo.getOperations();
        assertNotNull(bindingOperationInfos);
        assertEquals(bindingOperationInfos.size(), 4);
        LOG.info("the binding operation is " + bindingOperationInfos.iterator().next().getName());
        
        BindingOperationInfo sayHi = bindingInfo.getOperation("sayHi");
        assertNotNull(sayHi);
        assertEquals(sayHi.getName(), "sayHi");
        
        BindingOperationInfo greetMe = bindingInfo.getOperation("greetMe");
        assertNotNull(greetMe);
        assertEquals(greetMe.getName(), "greetMe");
        
        BindingOperationInfo greetMeOneWay = bindingInfo.getOperation("greetMeOneWay");
        assertNotNull(greetMeOneWay);
        assertEquals(greetMeOneWay.getName(), "greetMeOneWay");
        
        BindingOperationInfo pingMe = bindingInfo.getOperation("pingMe");
        assertNotNull(pingMe);
        assertEquals(pingMe.getName(), "pingMe");
    }
    
    public void testBindingMessageInfo() throws Exception {
        BindingInfo bindingInfo = null;
        bindingInfo = serviceInfo.getBindings().iterator().next();
        
        BindingOperationInfo sayHi = bindingInfo.getOperation("sayHi");
        BindingMessageInfo input = sayHi.getInput();
        assertNotNull(input);
        assertEquals(input.getMessageInfo().getName().getLocalPart(), "sayHiRequest");
        assertEquals(input.getMessageInfo().getName().getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http");
        assertEquals(input.getMessageInfo().getMessageParts().size(), 1);
        assertEquals(input.getMessageInfo().getMessageParts().get(0).getName().getLocalPart(), "in");
        assertEquals(input.getMessageInfo().getMessageParts().get(0).getName().getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http");
        assertTrue(input.getMessageInfo().getMessageParts().get(0).isElement());
        QName elementName = input.getMessageInfo().getMessageParts().get(0).getElementQName();
        assertEquals(elementName.getLocalPart(),
                     "sayHi");
        assertEquals(elementName.getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http/types");
        
        BindingMessageInfo output = sayHi.getOutput();
        assertNotNull(output);
        assertEquals(output.getMessageInfo().getName().getLocalPart(), "sayHiResponse");
        assertEquals(output.getMessageInfo().getName().getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http");
        assertEquals(output.getMessageInfo().getMessageParts().size(), 1);
        assertEquals(output.getMessageInfo().getMessageParts().get(0).getName().getLocalPart(), "out");
        assertEquals(output.getMessageInfo().getMessageParts().get(0).getName().getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http");
        assertTrue(output.getMessageInfo().getMessageParts().get(0).isElement());
        elementName = output.getMessageInfo().getMessageParts().get(0).getElementQName();
        assertEquals(elementName.getLocalPart(),
                     "sayHiResponse");
        assertEquals(elementName.getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http/types");
        
        assertTrue(sayHi.getFaults().size() == 0);
        
        BindingOperationInfo pingMe = bindingInfo.getOperation("pingMe");
        assertNotNull(pingMe);
        assertEquals(1, pingMe.getFaults().size());
        BindingFaultInfo fault = pingMe.getFaults().iterator().next();
        
        assertNotNull(fault);
        assertEquals(fault.getFaultInfo().getName().getLocalPart(), "pingMeFault");
        assertEquals(fault.getFaultInfo().getName().getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http");
        assertEquals(fault.getFaultInfo().getMessageParts().size(), 1);
        assertEquals(fault.getFaultInfo().getMessageParts().get(0).getName().getLocalPart(), "faultDetail");
        assertEquals(fault.getFaultInfo().getMessageParts().get(0).getName().getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http");
        assertTrue(fault.getFaultInfo().getMessageParts().get(0).isElement());
        elementName = fault.getFaultInfo().getMessageParts().get(0).getElementQName();
        assertEquals(elementName.getLocalPart(),
                     "faultDetail");
        assertEquals(elementName.getNamespaceURI(),
                     "http://objectweb.org/hello_world_soap_http/types");
    }
}
