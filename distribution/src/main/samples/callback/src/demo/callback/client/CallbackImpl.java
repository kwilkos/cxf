package demo.callback.client;

//import java.util.logging.Logger;
import org.objectweb.callback.CallbackPortType;


@javax.jws.WebService(serviceName = "CallbackService", 
                      portName = "CallbackPort",
                      endpointInterface = "org.objectweb.callback.CallbackPortType",
                      targetNamespace = "http://objectweb.org/callback", 
                      wsdlLocation = "file:./wsdl/basic_callback.wsdl")
                  
public class CallbackImpl implements CallbackPortType  {

    //private static final Logger LOG = 
    //    Logger.getLogger(CallbackImpl.class.getPackage().getName());
    
    /**
     * serverSayHi
     * @param: return_message (String)
     * @return: String
     */
    public String serverSayHi(String message) {
        System.out.println("Callback object invoked");
        System.out.println("Message recieved: " + message);
        return new String("Hi " + message);
    }
    
}
