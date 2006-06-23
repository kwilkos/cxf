package org.objectweb.celtix.systest.routing.passthrough;

import javax.xml.namespace.QName;

import org.objectweb.celtix.systest.routing.DocLitGreeterRouterBase;

public class SOAPHTTPToSOAPJMSRouter extends DocLitGreeterRouterBase {

    public SOAPHTTPToSOAPJMSRouter() {
        super();

        serviceName = 
            new QName("http://objectweb.org/hello_world_doc_lit", "SOAPService3");
        portName = 
            new QName("http://objectweb.org/hello_world_doc_lit", "SoapPort3");
        
        enableOneway = false;
    }
    
}
