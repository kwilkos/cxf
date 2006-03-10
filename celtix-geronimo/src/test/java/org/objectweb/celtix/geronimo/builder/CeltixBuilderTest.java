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
import org.objectweb.hello_world_soap_http.Greeter;

public class CeltixBuilderTest extends TestCase {

    private static final String SERVLET_NAME = "HelloWorldServlet";
    private static final String PORT_NAME = "SoapPort"; 
    private static final String WSDL_FILE = "hello_world.wsdl";

    private final CeltixBuilder builder = new CeltixBuilder();
    private final JarFile moduleFile = EasyMock.createMock(JarFile.class);
    
    private URL wsDescriptor;
    private PortInfo portInfo; 
    
    public void setUp() throws Exception {
        
        builder.doStart();
        wsDescriptor = getClass().getResource("webservices.xml");
        assertNotNull("failed to load test fixture", wsDescriptor);

        portInfo = new PortInfo();
        portInfo.setServletLink(SERVLET_NAME);
        portInfo.setServiceEndpointInterfaceName(Greeter.class.getName());
        portInfo.setPortName(PORT_NAME);
        portInfo.setWsdlFile(WSDL_FILE);

    }
    
    
    public void testGetGBeanInfo() { 
        
        GBeanInfo beanInfo = CeltixBuilder.getGBeanInfo();
        assertNotNull("getGBeanInfo must not return null", beanInfo);
        assertEquals("GBean must support correct interfaces", 
                     1, beanInfo.getInterfaces().size());
        System.out.println(beanInfo.getInterfaces());
        assertTrue("GBean must support WebServicesBuilder interface",
                   beanInfo.getInterfaces().contains(WebServiceBuilder.class.getName()));
    }
    
    public void testConfigurePojo() throws DeploymentException, ClassNotFoundException {
     
        GBeanData gbeanData = EasyMock.createMock(GBeanData.class);
        ClassLoader loader = EasyMock.createMock(ClassLoader.class);
        
        gbeanData.setAttribute("pojoClassName", Greeter.class.getName());
        loader.loadClass(Greeter.class.getName());
        EasyMock.expectLastCall().andReturn(Greeter.class);
        gbeanData.setAttribute(EasyMock.matches("webServiceContainer"), 
                               EasyMock.isA(StoredObject.class));
       
        EasyMock.replay(gbeanData);
        
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
}
