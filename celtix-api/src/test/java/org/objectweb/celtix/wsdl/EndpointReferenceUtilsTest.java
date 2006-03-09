package org.objectweb.celtix.wsdl;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;

import org.w3c.dom.Element;

import junit.framework.TestCase;

import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.addressing.ObjectFactory;

public class EndpointReferenceUtilsTest extends TestCase {
    
    private static final QName WSDL_LOCATION = 
        new QName("http://www.w3.org/2004/08/wsdl-instance", "wsdlLocation");
    private static final QName SEI = new QName("http://www.w3.org/2004/08/wsdl-instance", "sei");

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

    @WebServiceProvider(wsdlLocation = "resources/Test.wsdl",
                        portName = "TestPort",
                        serviceName = "TestService",
                        targetNamespace = "http://schemas.xmlsoap.org/wsdl/soap/http")
    class TestProvider1 implements Provider<Source> {
        TestProvider1() {
            //Complete
        }
        
        public Source invoke(Source source) {
            return null;
        }        
    }

    @WebServiceProvider    
    class TestProvider2 implements Provider<Source> {
        TestProvider2() {
            //Complete
        }
        
        public Source invoke(Source source) {
            return null;
        }
    }
    
    
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
    
    public void testGetEndpointReferenceFromProvider() throws Exception {
        WSDLManager manager = new TestWSDLManager();

        //Test with fully populated WebServiceProvider Annotation 
        TestProvider1 provider1 = new TestProvider1();
        
        EndpointReferenceType ref = 
            EndpointReferenceUtils.getEndpointReference(manager, provider1);
        assertNotNull(ref);
        
        WebServiceProvider wsp = provider1.getClass().getAnnotation(WebServiceProvider.class);
        assertNotNull(wsp);
        assertEquals(wsp.portName(), EndpointReferenceUtils.getPortName(ref));
        assertNotNull(EndpointReferenceUtils.getServiceName(ref));
        
        Map<QName, String> attribMap = ref.getMetadata().getOtherAttributes();
        assertEquals(wsp.wsdlLocation(), attribMap.get(WSDL_LOCATION));
        assertEquals(TestProvider1.class.getName(), attribMap.get(SEI));
        
        //Test with default values for WebServiceProvider Annotation
        TestProvider2 provider2 = new TestProvider2();
        
        ref =  EndpointReferenceUtils.getEndpointReference(manager, provider2);
        assertNotNull(ref);
        
        assertNull(EndpointReferenceUtils.getPortName(ref));
        assertNotNull(EndpointReferenceUtils.getServiceName(ref));
        
        attribMap = ref.getMetadata().getOtherAttributes();
        assertNull(attribMap.get(WSDL_LOCATION));
        assertEquals(TestProvider2.class.getName(), attribMap.get(SEI));
        
        //Test for No WebServiceProvider Annotation.
        ref =  EndpointReferenceUtils.getEndpointReference(manager, ref);
        assertNull(ref);
    }
    
    public void testSetServiceName() throws Exception {
        QName serviceName1 = new QName("http://objectweb.org/soap_http1", "SOAPService_Test1");

        EndpointReferenceType ref = new EndpointReferenceType();
        
        EndpointReferenceUtils.setServiceName(ref, serviceName1);
        assertEquals(serviceName1, EndpointReferenceUtils.getServiceName(ref));

        EndpointReferenceUtils.setServiceName(ref, null);
        assertEquals(serviceName1, EndpointReferenceUtils.getServiceName(ref));
       
    }

    public void testSetPortName() throws Exception {
        QName portName1 = new QName("http://objectweb.org/soap_http1", "SoapPort_Test1");

        EndpointReferenceType ref = new EndpointReferenceType();

        EndpointReferenceUtils.setPortName(ref, portName1.toString());
        assertEquals(portName1, QName.valueOf(EndpointReferenceUtils.getPortName(ref)));

        EndpointReferenceUtils.setPortName(ref, null);
        assertEquals(portName1, QName.valueOf(EndpointReferenceUtils.getPortName(ref)));
    }
    
    public void testSetMetaData() throws Exception {
        EndpointReferenceType ref = new EndpointReferenceType();
        List<Source> metadata = new ArrayList<Source>();
        //Read a Schema File
        InputStream isXsd =  getClass().getResourceAsStream("resources/addressing.xsd");
        StreamSource ssXsd = new StreamSource(isXsd);
        metadata.add(ssXsd);
        
        //Read a WSDL File
        InputStream isWSDL =  getClass().getResourceAsStream("resources/hello_world.wsdl");
        StreamSource ssWSDL = new StreamSource(isWSDL);
        metadata.add(ssWSDL);
        
        EndpointReferenceUtils.setMetadata(ref, metadata);
        assertNotNull("MetaData should not be empty", ref.getMetadata());
        List<Object> anyList = ref.getMetadata().getAny();
        assertNotNull("AnyList in MetaData should not be empty", anyList);
        assertEquals(2, anyList.size());
        
        WSDLManager manager = new TestWSDLManager();
        assertNotNull("Defintion element should be present", 
                      EndpointReferenceUtils.getWSDLDefinition(manager, ref));
        isXsd.close();
        isWSDL.close();
    }
}
