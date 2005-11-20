package org.objectweb.celtix.systest.type_test.soap;

import javax.xml.namespace.QName;

import org.objectweb.celtix.systest.type_test.AbstractTypeTestClient;

public class SOAPRpcLitClientTypeTest extends AbstractTypeTestClient {
    static final String WSDL_PATH = "/wsdl/type_test/type_test_rpclit_soap.wsdl";
    static final QName SERVICE_NAME =
        new QName("http://objectweb.org/type_test/rpc", "SOAPService");
    static final QName PORT_NAME = 
        new QName("http://objectweb.org/type_test/rpc", "SOAPPort");

    public SOAPRpcLitClientTypeTest(String name) {
        super(name, SERVICE_NAME, PORT_NAME, WSDL_PATH);
    }

    public void onetimeSetUp()  {
        try { 
            initBus(); 
            boolean ok = launchServer(SOAPRpcLitServerImpl.class); 
            assertTrue("failed to launch server", ok);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    } 
}
