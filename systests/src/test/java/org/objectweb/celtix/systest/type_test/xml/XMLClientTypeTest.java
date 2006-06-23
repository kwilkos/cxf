package org.objectweb.celtix.systest.type_test.xml;

import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.type_test.AbstractTypeTestClient5;

public class XMLClientTypeTest extends AbstractTypeTestClient5 {
    static final String WSDL_PATH = "/wsdl/type_test/type_test_xml.wsdl";
    static final QName SERVICE_NAME = new QName("http://objectweb.org/type_test/doc", "XMLService");
    static final QName PORT_NAME = new QName("http://objectweb.org/type_test/doc", "XMLPort");

    public XMLClientTypeTest(String name) {
        super(name, SERVICE_NAME, PORT_NAME, WSDL_PATH);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(XMLClientTypeTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                boolean ok = launchServer(XMLServerImpl.class); 
                assertTrue("failed to launch server", ok);
            }
            
            public void setUp() throws Exception {
                // set up configuration to enable schema validation
                URL url = getClass().getResource("../celtix-config.xml"); 
                assertNotNull("cannot find test resource", url);
                configFileName = url.toString(); 
                super.setUp();
            }
        };
    }  
}
