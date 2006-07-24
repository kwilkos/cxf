
package demo.servlet;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


@WebService(wsdlLocation = "",
targetNamespace = "http://objectweb.org/hello_world_soap_http", name = "Greeter")

public interface Greeter {

    @ResponseWrapper(targetNamespace = "http://objectweb.org/hello_world_soap_http/types",
        className = "org.objectweb.hello_world_soap_http.types.SayHiResponse", localName = "sayHiResponse")
    @RequestWrapper(targetNamespace = "http://objectweb.org/hello_world_soap_http/types",
        className = "org.objectweb.hello_world_soap_http.types.SayHi", localName = "sayHi")
    @WebResult(targetNamespace = "http://objectweb.org/hello_world_soap_http/types", name = "responseType")
    @WebMethod(operationName = "sayHi")
    java.lang.String sayHi();

    @ResponseWrapper(targetNamespace = "http://objectweb.org/hello_world_soap_http/types", 
        className = "org.objectweb.hello_world_soap_http.types.GreetMeResponse",
        localName = "greetMeResponse")
    @RequestWrapper(targetNamespace = "http://objectweb.org/hello_world_soap_http/types",
        className = "org.objectweb.hello_world_soap_http.types.GreetMe", localName = "greetMe")
    @WebResult(targetNamespace = "http://objectweb.org/hello_world_soap_http/types", name = "responseType")
    @WebMethod(operationName = "greetMe")
    java.lang.String greetMe(
        @WebParam(targetNamespace = "http://objectweb.org/hello_world_soap_http/types", name = "requestType")
        java.lang.String requestType
    );

    @Oneway
    @RequestWrapper(targetNamespace = "http://objectweb.org/hello_world_soap_http/types",
        className = "org.objectweb.hello_world_soap_http.types.GreetMeOneWay", localName = "greetMeOneWay")
    @WebMethod(operationName = "greetMeOneWay")
    void greetMeOneWay(
        @WebParam(targetNamespace = "http://objectweb.org/hello_world_soap_http/types", name = "requestType")
        java.lang.String requestType
    );

    @ResponseWrapper(targetNamespace = "http://objectweb.org/hello_world_soap_http/types",
        className = "org.objectweb.hello_world_soap_http.types.PingMeResponse", localName = "pingMeResponse")
    @RequestWrapper(targetNamespace = "http://objectweb.org/hello_world_soap_http/types",
        className = "org.objectweb.hello_world_soap_http.types.PingMe", localName = "pingMe")
    @WebMethod(operationName = "pingMe")
    void pingMe() throws PingMeFault;
}
