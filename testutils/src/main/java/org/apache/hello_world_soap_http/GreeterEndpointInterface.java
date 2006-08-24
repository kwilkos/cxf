package org.apache.hello_world_soap_http;

@javax.jws.WebService(name = "Greeter",
                      serviceName = "SOAPService",
                      targetNamespace = "http://apache.org/hello_world_soap_http")

public interface GreeterEndpointInterface extends Greeter {

}
