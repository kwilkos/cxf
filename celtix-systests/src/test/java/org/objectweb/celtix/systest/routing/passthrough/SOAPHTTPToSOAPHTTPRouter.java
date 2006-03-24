package org.objectweb.celtix.systest.routing.passthrough;

import java.net.URL;
import javax.xml.namespace.QName;

import org.objectweb.celtix.systest.routing.DocLitGreeterRouterBase;
import org.objectweb.hello_world_doc_lit.Greeter;
import org.objectweb.hello_world_doc_lit.SOAPService;

public class SOAPHTTPToSOAPHTTPRouter extends DocLitGreeterRouterBase {
    private final QName serviceName = new QName("http://objectweb.org/hello_world_doc_lit",
                                                "SOAPService");
    private final QName portName = new QName("http://objectweb.org/hello_world_doc_lit",
                                             "SoapPort");

    protected void setUp() throws Exception {
        super.setUp();
        URL wsdl = getClass().getResource("/wsdl/hello_world_doc_lit.wsdl");
        assertNotNull(wsdl);
        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);

        greeter = service.getPort(portName, Greeter.class);
    }
}
