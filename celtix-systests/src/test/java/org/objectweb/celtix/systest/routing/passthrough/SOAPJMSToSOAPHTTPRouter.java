package org.objectweb.celtix.systest.routing.passthrough;

import javax.xml.namespace.QName;

import org.objectweb.celtix.systest.routing.DocLitGreeterRouterBase;

public class SOAPJMSToSOAPHTTPRouter extends DocLitGreeterRouterBase {
    
    public SOAPJMSToSOAPHTTPRouter() {
        super();

        serviceName = 
            new QName("http://objectweb.org/hello_world_doc_lit", "SOAPService2");
        portName = 
            new QName("http://objectweb.org/hello_world_doc_lit", "SoapPort2");
        
        this.setName(SOAPJMSToSOAPHTTPRouter.class.getSimpleName());
    }
    
}
