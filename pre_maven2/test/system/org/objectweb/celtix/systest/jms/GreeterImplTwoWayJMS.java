package org.objectweb.celtix.systest.jms;

import javax.jws.WebService;

import org.objectweb.celtix.hello_world_jms.HelloWorldPortType;


@WebService(serviceName = "HelloWorldService", portName = "HelloWorldPort", name = "HelloWorldPortType",
            targetNamespace = "http://celtix.objectweb.org/hello_world_jms")
public class GreeterImplTwoWayJMS implements HelloWorldPortType {

    public String greetMe(String me) {
        System.out.println("Reached here :" + me);
        return "Hello " + me;
    }

    public String sayHi() {
        return "Bonjour";
    }
    
    public void greetMeOneWay(String requestType) {
        System.out.println("*********  greetMeOneWay: " + requestType);
    }
    
}
