package org.objectweb.celtix.systest.basicRPCLit;


import javax.jws.WebService;

import org.objectweb.hello_world_rpclit.GreeterRPCLit;


@WebService(serviceName = "SOAPServiceRPCLit", portName = "SoapPortRPCLit", name = "GreeterRPCLit",
            targetNamespace = "http://objectweb.org/hello_world_rpclit")
public class GreeterImplRPCLit implements GreeterRPCLit {

    public String greetMe(String me) {
        System.out.println("Reached here :" + me);
        return "Hello " + me;
    }

    public String sayHi() {
        return "Bonjour";
    }
}
