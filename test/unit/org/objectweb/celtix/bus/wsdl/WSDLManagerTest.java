package org.objectweb.celtix.bus.wsdl;

import java.io.StringWriter;
import java.net.URL;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.transports.jms.AddressType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.celtix.wsdl.JAXBExtensionHelper;
import org.objectweb.celtix.wsdl.WSDLManager;

public class WSDLManagerTest extends TestCase {
    
    public WSDLManagerTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(WSDLManagerTest.class);
    }

    /*
     * Test method for 'org.objectweb.celtix.bus.WSDLManagerImpl.getExtenstionRegistry()'
     */
    public void testGetExtenstionRegistry() throws Exception {
        assertNotNull(new WSDLManagerImpl(null).getExtenstionRegistry());
    }

    /*
     * Test method for 'org.objectweb.celtix.bus.WSDLManagerImpl.getDefinition(URL)'
     */
    public void testGetDefinitionURL() throws Exception {
        URL url = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull("Could not find WSDL", url);
        WSDLManager wsdlManager = new WSDLManagerImpl(null);
        Definition def = wsdlManager.getDefinition(url);
        assertNotNull(def);
            
        Definition def2 = wsdlManager.getDefinition(url);
        assertTrue(def == def2);
            
        url = null;
        System.gc();
        System.gc();
        url = getClass().getResource("/wsdl/hello_world.wsdl");
        Definition def3 = wsdlManager.getDefinition(url);
        assertTrue(def != def3);
    }

    /*
     * Test method for 'org.objectweb.celtix.bus.WSDLManagerImpl.getDefinition(String)'
     */
    public void testGetDefinitionString() throws Exception {
        URL neturl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull("Could not find WSDL", neturl);
        String url = neturl.toString();
        WSDLManager wsdlManager = new WSDLManagerImpl(null);
        Definition def = wsdlManager.getDefinition(url);
        assertNotNull(def);
            
        Definition def2 = wsdlManager.getDefinition(url);
        assertTrue(def == def2);
            
        url = null;
        System.gc();
        System.gc();
        url = getClass().getResource("/wsdl/hello_world.wsdl").toString();
        Definition def3 = wsdlManager.getDefinition(url);
        assertTrue(def != def3);
    }
    

    public void testExtensions() throws Exception {
        URL neturl = getClass().getResource("/wsdl/jms_test.wsdl");
        assertNotNull("Could not find WSDL", neturl);
        String url = neturl.toString();
        WSDLManager wsdlManager = new WSDLManagerImpl(null);
        JAXBExtensionHelper.addExtensions(wsdlManager.getExtenstionRegistry(),
                                          javax.wsdl.Port.class,
                                          org.objectweb.celtix.transports.jms.AddressType.class);
            
        Definition def = wsdlManager.getDefinition(url);
        assertNotNull(def);
            
        StringWriter writer = new StringWriter();
        wsdlManager.getWSDLFactory().newWSDLWriter().writeWSDL(def, writer);
        assertTrue(writer.toString().indexOf("jms:address") != -1);
    }
    
    public void testExtensionReturnsProperJAXBType() throws Exception {
        URL neturl = getClass().getResource("/wsdl/jms_test.wsdl");
        Bus bus = Bus.init();
        assertNotNull("Could not find WSDL", neturl);
        WSDLManager wsdlManager = bus.getWSDLManager();
        JAXBExtensionHelper.addExtensions(wsdlManager.getExtenstionRegistry(),
                                          javax.wsdl.Port.class,
                                          org.objectweb.celtix.transports.jms.AddressType.class);
        
        QName serviceName = new QName("http://celtix.objectweb.org/hello_world_jms", "HelloWorldService");
        
        EndpointReferenceType ref = 
            EndpointReferenceUtils.getEndpointReference(neturl, serviceName, "");
        
        assertNotNull("Unable to create EndpointReference ", ref);
        
        Port port = EndpointReferenceUtils.getPort(wsdlManager, ref);
        List<?> list = port.getExtensibilityElements();
        AddressType jmsAddressDetails = null;
        for (Object ep : list) {
            ExtensibilityElement ext = (ExtensibilityElement)ep;
            if (ext instanceof AddressType) {
                jmsAddressDetails = (AddressType)ext;
            }
        }
        assertNotNull(jmsAddressDetails);
    }
}
