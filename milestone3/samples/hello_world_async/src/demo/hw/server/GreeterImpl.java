package demo.hw.server;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.objectweb.hello_world_async_soap_http.GreeterAsync;
import org.objectweb.hello_world_async_soap_http.types.GreetMeSometimeResponse;

@javax.jws.WebService(name = "GreeterAsync", serviceName = "SOAPService", 
                      targetNamespace = "http://objectweb.org/hello_world_soap_http", 
                      wsdlLocation = "file:./wsdl/hello_world_async.wsdl")
                  
public class GreeterImpl implements GreeterAsync {
    private static final Logger LOG = 
        Logger.getLogger(GreeterImpl.class.getPackage().getName());
 
     /* (non-Javadoc)
     * @see org.objectweb.hello_world_soap_http.Greeter#greetMeSometime(java.lang.String)
     */
    public String greetMeSometime(String me) {
        LOG.info("Executing operation greetMeSometime");
        System.out.println("Executing operation greetMeSometime\n");
        return "How are you " + me;
    }
    
    public Future<?>  greetMeSometimeAsync(String requestType, 
                                           AsyncHandler<GreetMeSometimeResponse> asyncHandler) {
        return null; 
        /*not called */
    }
    
    public Response<GreetMeSometimeResponse> greetMeSometimeAsync(String requestType) { 
        return null; 
        /*not called */
    }

    
}
