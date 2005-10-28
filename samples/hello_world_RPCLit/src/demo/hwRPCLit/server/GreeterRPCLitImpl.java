package demo.hwRPCLit.server;

import java.util.logging.Logger;
import org.objectweb.hello_world_rpclit.GreeterRPCLit;
import org.objectweb.hello_world_rpclit.types.MyComplexStruct;

@javax.jws.WebService(name = "GreeterRPCLit", serviceName = "SOAPServiceRPCLit",
                      targetNamespace = "http://objectweb.org/hello_world_rpclit",
                      wsdlLocation = "file:./wsdl/hello_world_RPCLit.wsdl")

public class GreeterRPCLitImpl implements GreeterRPCLit {

    private static Logger logger =
        Logger.getLogger(GreeterRPCLitImpl.class.getPackage().getName());

    public String greetMe(String me) {
        logger.info("Executing operation greetMe");
        System.out.println("Executing operation greetMe");
        System.out.println("Message received: " + me);
        return "Hello " + me;
    }

    public String sayHi() {
        logger.info("Executing operation sayHi");
        System.out.println("Executing operation sayHi");
        return "Bonjour";
    }

     public MyComplexStruct sendReceiveData(MyComplexStruct in) {
		 logger.info("Executing operation sendReceiveData");
		 System.out.println("Executing operation sendReceiveData");
	     System.out.println("Received struct with values :\nElement-1 : " + in.getElem1()
	                  + "\nElement-2 : " + in.getElem2()
	                  + "\nElement-3 : " + in.getElem3()
	                  + "\n");
	     return in;
    }
}
