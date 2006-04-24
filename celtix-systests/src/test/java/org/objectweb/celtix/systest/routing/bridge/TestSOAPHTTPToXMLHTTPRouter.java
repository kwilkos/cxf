package org.objectweb.celtix.systest.routing.bridge;

import javax.xml.namespace.QName;

import org.objectweb.celtix.systest.routing.DocLitGreeterRouterBase;

public class TestSOAPHTTPToXMLHTTPRouter extends DocLitGreeterRouterBase {

    public TestSOAPHTTPToXMLHTTPRouter() {
        super();

        serviceName =
            new QName("http://objectweb.org/hello_world_doc_lit", "SOAPService");
        portName =
            new QName("http://objectweb.org/hello_world_doc_lit", "SoapPort");
        
        enableOneway = false;
    }
}
