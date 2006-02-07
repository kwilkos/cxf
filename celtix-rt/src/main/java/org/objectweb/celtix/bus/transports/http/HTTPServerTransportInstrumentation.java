package org.objectweb.celtix.bus.transports.http;



import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;

public class HTTPServerTransportInstrumentation implements Instrumentation {  
    private static final String INSTRUMENTED_NAME = "HTTPServerTransport";
    private static int instanceNumber;
    
    AbstractHTTPServerTransport httpServerTransport; 
    HTTPServerPolicy policy;
    String objectName;
    
    public HTTPServerTransportInstrumentation(AbstractHTTPServerTransport ahsTransport) {
        super();
        httpServerTransport = ahsTransport;
        objectName = INSTRUMENTED_NAME + instanceNumber;
        instanceNumber++;
    }
    
    //define the basic management operation for the instrumentation
    public String getUrl() {
        return httpServerTransport.url;
    }
    
    // return the policy object ......
    public HTTPServerPolicy getHTTPServerPolicy() {
        return httpServerTransport.policy;    
    }
  
    public static void resetInstanceNumber() {
        instanceNumber = 0;
    }

    public Object getComponent() {        
        return httpServerTransport;
    }  

    public String getInstrumentationName() {        
        return INSTRUMENTED_NAME;
    }

    public String getUniqueInstrumentationName() {        
        return objectName;
    }
    
    // TODO need to set up the performance counter
    
}
