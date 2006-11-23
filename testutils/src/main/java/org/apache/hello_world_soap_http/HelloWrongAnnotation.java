package org.apache.hello_world_soap_http;

import javax.jws.soap.SOAPBinding;

@javax.jws.WebService(name = "HelloWrongAnnotation", 
                      serviceName = "HelloService",
                      portName = "HelloPort",
                      targetNamespace = "http://apache.org/hello_world_soap_http" 
                      )

public class HelloWrongAnnotation {
    @SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
    public String sayHi() {
        return "Hello CXF";
    }
}
