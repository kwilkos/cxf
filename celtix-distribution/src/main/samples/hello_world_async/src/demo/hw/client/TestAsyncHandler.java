package demo.hw.client;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.objectweb.hello_world_async_soap_http.types.GreetMeSometimeResponse;


public class TestAsyncHandler implements AsyncHandler<GreetMeSometimeResponse> {
    
    private GreetMeSometimeResponse reply;

    public void handleResponse(Response<GreetMeSometimeResponse> response) {
        try {
            System.err.println("handleResponse called");
            reply = response.get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public String getResponse() {
        return reply.getResponseType();
    }
    
}
