package org.objectweb.celtix.bus.transports.http;



import org.objectweb.celtix.bus.management.counters.TransportCounters;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;

@ManagedResource(objectName = "HTTPServerTransport", 
                 description = "The Celtix bus HTTP Server Transport component ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class HTTPServerTransportInstrumentation implements Instrumentation {  
    private static final String INSTRUMENTED_NAME = "HTTPServerTransport";
    private static int instanceNumber;
    
    JettyHTTPServerTransport httpServerTransport; 
    HTTPServerPolicy policy;
    String objectName;
    TransportCounters counters;
    
    public HTTPServerTransportInstrumentation(JettyHTTPServerTransport hsTransport) {
        super();
        httpServerTransport = hsTransport;
        objectName = INSTRUMENTED_NAME + instanceNumber;
        instanceNumber++;
        counters = hsTransport.counters;
    }
    
    @ManagedAttribute(description = "The http server url",
                      persistPolicy = "OnUpdate")
    //define the basic management operation for the instrumentation
    public String getUrl() {
        return httpServerTransport.url;
    }
    
    @ManagedAttribute(description = "The http server request error",
                      persistPolicy = "OnUpdate")
    public int getTotalError() {
        return counters.getTotalError().getValue();
    }
    
    @ManagedAttribute(description = "The http server total request counter",
                      persistPolicy = "OnUpdate")
    public int getRequestTotal() {
        return counters.getRequestTotal().getValue();
    }
    
    @ManagedAttribute(description = "The http server one way request counter",
                      persistPolicy = "OnUpdate")
    public int getRequestOneWay() {
        return counters.getRequestOneWay().getValue();
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
   
    
}
