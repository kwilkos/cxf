package org.objectweb.celtix.systest.routing.bridge;

import javax.xml.namespace.QName;

import org.objectweb.celtix.systest.routing.DocLitGreeterRouterBase;

public class TestXMLHTTPToSOAPJMSRouter extends DocLitGreeterRouterBase {

    public TestXMLHTTPToSOAPJMSRouter() {
        super();

        serviceName =
            new QName("http://objectweb.org/hello_world_doc_lit", "XMLService4");
        portName =
            new QName("http://objectweb.org/hello_world_doc_lit", "XMLPort4");
    }
}
