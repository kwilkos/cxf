package org.objectweb.hello_world_soap_http;


//import java.util.logging.Logger;
import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

@WebServiceProvider(portName = "SoapPort", serviceName = "SOAPService",
                      targetNamespace = "http://objectweb.org/hello_world_soap_http",
                      wsdlLocation = "resources/wsdl/hello_world.wsdl")
@ServiceMode(value = Service.Mode.MESSAGE)                      
public class HelloWorldServiceProvider implements Provider<Source> {

    //private static final Logger LOG =
    //    Logger.getLogger(AnnotatedGreeterImpl.class.getName());
    private int invokeCount;
    
    public HelloWorldServiceProvider() {
        //Complete
    }

    public Source invoke(Source source) {
        invokeCount++;
        return source;
    }
    
    public int getInvokeCount() {
        return invokeCount;
    }
}
