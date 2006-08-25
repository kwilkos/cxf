package demo.hw.client;

import java.io.File;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.ws.Response;

import org.objectweb.hello_world_async_soap_http.GreeterAsync;
import org.objectweb.hello_world_async_soap_http.SOAPService;
import org.objectweb.hello_world_async_soap_http.types.GreetMeSometimeResponse;

public final class Client {

    private static final QName SERVICE_NAME 
        = new QName("http://objectweb.org/hello_world_async_soap_http", "SOAPService");


    private Client() {
    } 

    public static void main(String args[]) throws Exception {
        
        if (args.length == 0) { 
            System.out.println("please specify wsdl");
            System.exit(1); 
        }

        File wsdl = new File(args[0]);
        
        SOAPService ss = new SOAPService(wsdl.toURL(), SERVICE_NAME);
        GreeterAsync port = ss.getSoapPort();
        String resp; 
        
        // callback method
        TestAsyncHandler testAsyncHandler = new TestAsyncHandler();
        System.out.println("Invoking greetMeSometimeAsync using callback object...");
        Future<?> response = port.greetMeSometimeAsync(System.getProperty("user.name"), testAsyncHandler);
        while (!response.isDone()) {
            Thread.sleep(100);
        }  
        resp = testAsyncHandler.getResponse();
        System.out.println();
        System.out.println("Server responded through callback with: " + resp);
        System.out.println();
        
        //polling method
        System.out.println("Invoking greetMeSometimeAsync using polling...");
        Response<GreetMeSometimeResponse> greetMeSomeTimeResp = 
            port.greetMeSometimeAsync(System.getProperty("user.name"));
        while (!greetMeSomeTimeResp.isDone()) {
            Thread.sleep(100);
        }
        GreetMeSometimeResponse reply = greetMeSomeTimeResp.get();
        System.out.println("Server responded through polling with: " + reply.getResponseType());    

        System.exit(0); 

    }

}