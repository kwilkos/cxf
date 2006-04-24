package org.objectweb.celtix.systest.xml_wrapped;

import org.objectweb.hello_world_xml_http.wrapped.Greeter;
import org.objectweb.hello_world_xml_http.wrapped.PingMeFault;
import org.objectweb.hello_world_xml_http.wrapped.types.FaultDetail;

@javax.jws.WebService(serviceName = "XMLService", 
                      portName = "XMLPort",
                      endpointInterface = "org.objectweb.hello_world_xml_http.wrapped.Greeter",
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

    public void pingMe() throws PingMeFault {
        FaultDetail faultDetail = new FaultDetail();
        faultDetail.setMajor((short)2);
        faultDetail.setMinor((short)1);
        throw new PingMeFault("PingMeFault raised by server", faultDetail);
    }
}
