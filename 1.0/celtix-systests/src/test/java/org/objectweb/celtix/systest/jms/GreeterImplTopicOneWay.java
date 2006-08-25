package org.objectweb.celtix.systest.jms;

import javax.jws.WebService;

import org.objectweb.celtix.hello_world_jms.HelloWorldPubSubPort;

@WebService(serviceName = "HelloWorldPubSubService", 
            portName = "HelloWorldPubSubPort", 
            endpointInterface = "org.objectweb.celtix.hello_world_jms.HelloWorldPubSubPort",
            targetNamespace = "http://celtix.objectweb.org/hello_world_jms")
public class GreeterImplTopicOneWay implements HelloWorldPubSubPort {

    public void greetMeOneWay(String stringParam0) {
        System.out.println("*********  greetMeOneWay: " + stringParam0);
    }
}
