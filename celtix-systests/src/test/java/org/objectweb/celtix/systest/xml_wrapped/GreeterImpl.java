package org.objectweb.celtix.systest.xml_wrapped;

import org.objectweb.hello_world_xml_http.wrapped.Greeter;

@javax.jws.WebService(name = "Greeter", serviceName = "XMLService", portName = "XMLPort",
                      targetNamespace = "http://objectweb.org/hello_world_xml_http/wrapped")

@javax.xml.ws.BindingType(value = "http://celtix.objectweb.org/bindings/xmlformat")

public class GreeterImpl implements Greeter {

    public String greetMe(String me) {
        return "Hello " + me;
    }

    public void greetMeOneWay(String me) {
        System.out.println("Executing operation greetMeOneWay\n");
        System.out.println("Hello there " + me);
    }

    public String sayHi() {
        return "Bonjour";
    }
}
