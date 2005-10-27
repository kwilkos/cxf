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
        return "Hello " + me;
    }

    public String sayHi() {
        logger.info("Executing operation sayHi");
        return "Bonjour";
    }

     public MyComplexStruct sendReceiveData(MyComplexStruct in) {
		 logger.info("Executing operation sayHi");
	     logger.info("Received struct with values : Elem1 : " + in.getElem1()
	                  + " Elem2 : " + in.getElem2()
	                  + " Elem3 : " + in.getElem3()
	                  + "\n");
	     return in;
    }
}
