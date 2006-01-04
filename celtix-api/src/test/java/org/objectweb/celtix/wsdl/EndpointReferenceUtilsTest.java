package org.objectweb.celtix.wsdl;

import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

import junit.framework.TestCase;

import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.addressing.ObjectFactory;

public class EndpointReferenceUtilsTest extends TestCase {

    public EndpointReferenceUtilsTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(EndpointReferenceUtilsTest.class);
    }

    class TestWSDLManager implements WSDLManager {
        WSDLFactory factory;
        TestWSDLManager() {
            try {
                factory = WSDLFactory.newInstance();
                //registry = factory.newPopulatedExtensionRegistry();
            } catch (WSDLException e) {
                e.printStackTrace();
            }
        }

        public ExtensionRegistry getExtenstionRegistry() {
            return null;
        }

        public WSDLFactory getWSDLFactory() {
            return factory;
        }

        public Definition getDefinition(URL url) throws WSDLException {
            return loadDefinition(url.toString());
        }

        public Definition getDefinition(String url) throws WSDLException {
            return loadDefinition(url);
        }

        public Definition getDefinition(Element element) throws WSDLException {
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", false);
            reader.setExtensionRegistry(getExtenstionRegistry());
            return reader.readWSDL(null, element);
        }

        public Definition getDefinition(Class<?> sei) throws WSDLException {
            // TODO Auto-generated method stub
            return null;
        }
        private Definition loadDefinition(String url) throws WSDLException {
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", false);
            reader.setExtensionRegistry(getExtenstionRegistry());
            return reader.readWSDL(url);
        }

    };

    public void testGetWSDLDefinition() throws Exception  {
        Class cls[] = new Class[] {
            ObjectFactory.class,
            org.objectweb.celtix.ws.addressing.wsdl.ObjectFactory.class
        };
        JAXBContext context = JAXBContext.newInstance(cls);
        Unmarshaller u = context.createUnmarshaller();

        EndpointReferenceType ref = (EndpointReferenceType) 
                                    ((JAXBElement<?>)u.unmarshal(
                                        getClass().getResource("resources/reference1.xml"))).getValue();

        ref.getMetadata().getOtherAttributes().put(
                                                  new QName("http://www.w3.org/2004/08/wsdl-instance",
                                                            "wsdlLocation"),
                                                  getClass()
                                                    .getResource("/wsdl/hello_world.wsdl")
                                                    .toString());


        WSDLManager manager = new TestWSDLManager();

        Definition def = EndpointReferenceUtils.getWSDLDefinition(manager,
                                                                  ref);
        assertNotNull("Could not load wsdl", def);


        Port port = EndpointReferenceUtils.getPort(manager, ref);
        assertNotNull("Could not find port", port);             
    }

    public void testGetWSDLDefinitionEmbeddedWsdl() throws Exception  {
        WSDLManager manager = new TestWSDLManager();

        Class cls[] = new Class[] {
            ObjectFactory.class,
            org.objectweb.celtix.ws.addressing.wsdl.ObjectFactory.class
        };
        JAXBContext context = JAXBContext.newInstance(cls);
        Unmarshaller u = context.createUnmarshaller();

        EndpointReferenceType ref = (EndpointReferenceType) 
                                    ((JAXBElement<?>)u.unmarshal(
                                        getClass().getResource("resources/reference2.xml"))).getValue();

        Definition def = EndpointReferenceUtils.getWSDLDefinition(manager,
                                                                  ref);
        assertNotNull("Could not load wsdl", def);


        Port port = EndpointReferenceUtils.getPort(manager, ref);
        assertNotNull("Could not find port", port);             
    } 

    public void testGetEndpointReference() throws Exception  {
        WSDLManager manager = new TestWSDLManager();
        URL url = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(url);
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService_Test1");
        String portName = new String("SoapPort_Test2");

        EndpointReferenceType ref = 
            EndpointReferenceUtils.getEndpointReference(url, serviceName, portName);

        assertNotNull("Could not create endpoint reference", ref);

        Port port = EndpointReferenceUtils.getPort(manager, ref);

        assertNotNull("Could not find port", port);        
        assertEquals(portName, port.getName());
    }  

    public void testGetAddress() throws Exception {
        URL url = getClass().getResource("/wsdl/hello_world.wsdl");
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService_Test1");
        String portName = new String("SoapPort_Test2");

        EndpointReferenceType ref = 
            EndpointReferenceUtils.getEndpointReference(url, serviceName, portName);

        String address = EndpointReferenceUtils.getAddress(ref);
        assertNull(address);
        // only when getAddress implements search in wsdl
        // assertEquals("http://localhost:9101", address);

        address = "http://localhost:8080/hello_world_soap_http";
        EndpointReferenceUtils.setAddress(ref, address);
        assertEquals(address, EndpointReferenceUtils.getAddress(ref));

        EndpointReferenceUtils.setAddress(ref, null);
        assertNull(EndpointReferenceUtils.getAddress(ref));
    }
}
