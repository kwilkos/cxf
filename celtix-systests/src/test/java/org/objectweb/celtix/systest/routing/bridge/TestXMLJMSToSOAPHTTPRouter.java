package org.objectweb.celtix.systest.routing.bridge;

import javax.xml.namespace.QName;

import org.objectweb.celtix.systest.routing.DocLitGreeterRouterBase;

public class TestXMLJMSToSOAPHTTPRouter extends DocLitGreeterRouterBase {

    public TestXMLJMSToSOAPHTTPRouter() {
        super();

        serviceName =
            new QName("http://objectweb.org/hello_world_doc_lit", "XMLService3");
        portName =
            new QName("http://objectweb.org/hello_world_doc_lit", "XMLPort3");
        enableOneway = false;
    }
}
