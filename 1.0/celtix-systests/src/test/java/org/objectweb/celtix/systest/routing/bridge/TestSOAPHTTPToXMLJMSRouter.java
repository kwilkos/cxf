package org.objectweb.celtix.systest.routing.bridge;

import javax.xml.namespace.QName;

import org.objectweb.celtix.systest.routing.DocLitGreeterRouterBase;

public class TestSOAPHTTPToXMLJMSRouter extends DocLitGreeterRouterBase {

    public TestSOAPHTTPToXMLJMSRouter() {
        super();

        serviceName =
            new QName("http://objectweb.org/hello_world_doc_lit", "SOAPService3");
        portName =
            new QName("http://objectweb.org/hello_world_doc_lit", "SoapPort3");
        enableOneway = false;
    }
}
