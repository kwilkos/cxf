package org.objectweb.celtix.systest.routing.passthrough;

import javax.xml.namespace.QName;

import org.objectweb.celtix.systest.routing.DocLitGreeterRouterBase;

public class SOAPHTTPToSOAPHTTPRouter extends DocLitGreeterRouterBase {

    public SOAPHTTPToSOAPHTTPRouter() {
        super();

        serviceName = 
            new QName("http://objectweb.org/hello_world_doc_lit", "SOAPService");
        portName = 
            new QName("http://objectweb.org/hello_world_doc_lit", "SoapPort");
        
        this.setName(SOAPHTTPToSOAPHTTPRouter.class.getSimpleName());
    }
    
}
