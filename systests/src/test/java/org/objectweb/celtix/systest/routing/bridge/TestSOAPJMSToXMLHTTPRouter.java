package org.objectweb.celtix.systest.routing.bridge;

import javax.xml.namespace.QName;

import org.objectweb.celtix.systest.routing.DocLitGreeterRouterBase;

public class TestSOAPJMSToXMLHTTPRouter extends DocLitGreeterRouterBase {

    public TestSOAPJMSToXMLHTTPRouter() {
        super();

        serviceName =
            new QName("http://objectweb.org/hello_world_doc_lit", "SOAPService4");
        portName =
            new QName("http://objectweb.org/hello_world_doc_lit", "SoapPort4");
    }
}
