package org.objectweb.celtix.systest.xml_bare;

import org.objectweb.hello_world_xml_http.bare.Greeter;
import org.objectweb.hello_world_xml_http.bare.types.MyComplexStruct;

@javax.jws.WebService(name = "Greeter", serviceName = "XMLService", portName = "XMLPort",
                      targetNamespace = "http://objectweb.org/hello_world_xml_http/bare")

@javax.xml.ws.BindingType(value = "http://celtix.objectweb.org/bindings/xmlformat")

public class GreeterImpl implements Greeter {

    public String greetMe(String me) {
        return "Hello " + me;
    }

    public String sayHi() {
        return "Bonjour";
    }

    public MyComplexStruct sendReceiveData(MyComplexStruct in) {
        return in;
    }
}
