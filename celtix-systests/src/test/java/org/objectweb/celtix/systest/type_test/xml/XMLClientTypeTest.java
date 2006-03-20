package org.objectweb.celtix.systest.type_test.xml;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.type_test.AbstractTypeTestClient3;

public class XMLClientTypeTest extends AbstractTypeTestClient3 {
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
        };
    }  
}
