package org.objectweb.hello_world_soap_http;

@javax.jws.WebService(name = "Greeter", 
                      serviceName = "SOAPService",
                      targetNamespace = "http://objectweb.org/hello_world_soap_http", 
                      wsdlLocation = 
                          "resources/wsdl/hello_world.wsdl")
public interface GreeterEndpointInterface {

}
