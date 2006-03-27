package org.objectweb.celtix.geronimo.builder;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import junit.framework.TestCase;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.kernel.StoredObject;
import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.geronimo.MockBusFactory;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.transports.TransportFactoryManager;
import org.objectweb.hello_world_soap_http.Greeter;

public class CeltixBuilderTest extends TestCase {

    private static final String SERVICE_NAME = "HelloWorldService";
    private static final String SERVLET_NAME = "HelloWorldServlet";
    private static final String PORT_NAME = "SoapPort"; 
    private static final String WSDL_FILE = "file:/hello_world.wsdl";

    private final JarFile moduleFile = EasyMock.createMock(JarFile.class);
    
    private URL wsDescriptor;
    private PortInfo portInfo; 
    
    private CeltixBuilder builder;
    private Bus mockBus; 
    
    private MockBusFactory mockBusFactory = new MockBusFactory(); 
    
    public void setUp() throws Exception {
        
        wsDescriptor = getClass().getResource("webservices.xml");
        assertNotNull("failed to load test fixture", wsDescriptor);

        portInfo = new PortInfo();
        portInfo.setServiceName(SERVICE_NAME);
        portInfo.setServletLink(SERVLET_NAME);
        portInfo.setServiceEndpointInterfaceName(Greeter.class.getName());
        portInfo.setPortName(PORT_NAME);
        portInfo.setWsdlFile(WSDL_FILE);

        mockBus = mockBusFactory.createMockBus();
        builder = new CeltixBuilder(mockBusFactory.getBus());
    }
    
    
    public void testStart() throws Exception {
    
        TransportFactoryManager tfm = EasyMock.createMock(TransportFactoryManager.class);
        
        // initialise bus 
        // register transport factory
        mockBus.getTransportFactoryManager();
        EasyMock.expectLastCall().andReturn(tfm);
        
        tfm.registerTransportFactory(EasyMock.eq("http://schemas.xmlsoap.org/wsdl/soap/"),
                EasyMock.isA(TransportFactory.class));
        tfm.registerTransportFactory(EasyMock.eq("http://schemas.xmlsoap.org/wsdl/soap/http"),
                EasyMock.isA(TransportFactory.class));
        tfm.registerTransportFactory(EasyMock.eq("http://celtix.objectweb.org/transports/http/configuration"),
                EasyMock.isA(TransportFactory.class));
        
        mockBusFactory.replay();
        EasyMock.replay(tfm);
        
        CeltixBuilder cb = new CeltixBuilder(mockBus);
        cb.doStart();

        EasyMock.verify(mockBus);
        EasyMock.verify(tfm);

    }
    
    
    public void testGetGBeanInfo() { 
        
        GBeanInfo beanInfo = CeltixBuilder.getGBeanInfo();
        assertNotNull("getGBeanInfo must not return null", beanInfo);
        assertEquals("GBean must support correct interfaces", 
                     1, beanInfo.getInterfaces().size());
        assertTrue("GBean must support WebServicesBuilder interface",
                   beanInfo.getInterfaces().contains(WebServiceBuilder.class.getName()));
    }
    
    public void testConfigurePojo() throws Exception {
     
        GBeanData gbeanData = EasyMock.createMock(GBeanData.class);
        ClassLoader loader = EasyMock.createMock(ClassLoader.class);
        
        gbeanData.setAttribute("pojoClassName", Greeter.class.getName());
        loader.loadClass(Greeter.class.getName());
        EasyMock.expectLastCall().andReturn(Greeter.class);
        gbeanData.setAttribute(EasyMock.matches("webServiceContainer"), 
                               EasyMock.isA(StoredObject.class));
       
        EasyMock.replay(gbeanData);
        mockBusFactory.replay();
        /*
        EasyMock.replay(loader); 
        EasyMock.replay(mockBus);
        EasyMock.replay(mockBindingMgr);
        EasyMock.replay(bindingFact);
        EasyMock.replay(mockServerBinding);
        */
        builder.configurePOJO(gbeanData, moduleFile, portInfo, Greeter.class.getName(),
                              getClass().getClassLoader());
        
        EasyMock.verify(gbeanData);
    }
    
    @SuppressWarnings("unchecked")
    public void testParseWebServiceDescriptor() throws DeploymentException {

        boolean isEJB = false;
        Map<String, String> correctedPortLocations = new HashMap<String, String>();
        correctedPortLocations.put("HelloWorldServlet", "//wstest/Hello");
        
        Map ret = builder.parseWebServiceDescriptor(wsDescriptor, moduleFile, isEJB, correctedPortLocations);
        assertNotNull("parseWebServiceDescriptor must return port-info map",
                      ret);
        assertTrue("port info map must not be empty", !ret.isEmpty());
        
        Map<String, PortInfo> map = (Map<String, PortInfo>)ret;
        
        PortInfo pi = map.get("HelloWorldServlet");
        assertNotNull("could not find port info object", pi);    
    }
    
    public void testLoadSEI() throws Exception {
        
        Class<?> seiClass = builder.loadSEI(Greeter.class.getName(), getClass().getClassLoader());
        assertNotNull("method must not return null class", seiClass);
        assertEquals("method returned correct class", Greeter.class, seiClass);
    }
    
}
