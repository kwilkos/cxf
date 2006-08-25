package org.objectweb.celtix.systest.jms;

import javax.jws.WebService;

import org.objectweb.celtix.hello_world_jms.HelloWorldOneWayPort;

@WebService(serviceName = "HelloWorldOneWayQueueService", 
            portName = "HelloWorldOneWayQueuePort", 
            endpointInterface = "org.objectweb.celtix.hello_world_jms.HelloWorldOneWayPort",
            targetNamespace = "http://celtix.objectweb.org/hello_world_jms")
public class GreeterImplQueueOneWay implements HelloWorldOneWayPort {

    public void greetMeOneWay(String stringParam0) {
        System.out.println("*********  greetMeOneWay: " + stringParam0);

    }
}
