package org.objectweb.celtix.systest.basicRPCLit;

import javax.jws.WebService;

import org.objectweb.hello_world_rpclit.GreeterRPCLit;
import org.objectweb.hello_world_rpclit.types.MyComplexStruct;


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
    
    public MyComplexStruct sendReceiveData(MyComplexStruct in) {
        System.out.println("Received struct with values : Elem1 : " + in.getElem1() 
                                     + " Elem2 : " + in.getElem2() 
                                     + " Elem3 : " + in.getElem3() 
                                     + "\n");
        return in;        
    }
}
