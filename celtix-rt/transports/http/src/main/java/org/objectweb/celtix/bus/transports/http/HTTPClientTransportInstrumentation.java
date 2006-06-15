package org.objectweb.celtix.bus.transports.http;



import org.objectweb.celtix.bus.configuration.security.AuthorizationPolicy;
import org.objectweb.celtix.bus.management.counters.TransportClientCounters;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.management.annotation.ManagedAttribute;
import org.objectweb.celtix.management.annotation.ManagedResource;
import org.objectweb.celtix.transports.http.configuration.HTTPClientPolicy;

@ManagedResource(componentName = "HTTPClientTransport", 
                 description = "The Celtix bus HTTP client side transport componnet ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class HTTPClientTransportInstrumentation implements Instrumentation {  
    private static final String INSTRUMENTATION_NAME = "Bus.Service.Port.HTTPClientTransport";
    
    private static int instanceNumber;
    
    HTTPClientTransport httpClientTransport;
    String objectName;
    TransportClientCounters counters;
   
    
    public HTTPClientTransportInstrumentation(HTTPClientTransport hcTransport) {        
        httpClientTransport = hcTransport; 
        counters = hcTransport.counters;
        objectName = "HTTPClientTransport" + instanceNumber; 
        instanceNumber++;
    }
    
    public static void resetInstanceNumber() {
        instanceNumber = 0;
    }
    
    
    public HTTPClientPolicy getHTTPClientPolicy() {
        return httpClientTransport.policy;
    }
    
    public AuthorizationPolicy getAuthPolicy() {
        return httpClientTransport.authPolicy;
    }
    
    public AuthorizationPolicy getProxyAuthPolicy() {
        return httpClientTransport.proxyAuthPolicy;
    }        
    
    @ManagedAttribute(description = "The http request url",
                      persistPolicy = "OnUpdate")
    public String getUrl() {
        return httpClientTransport.url.toString();
    }
    
    @ManagedAttribute(description = "The http client invoke counter",
                      persistPolicy = "OnUpdate")
    public int getInvoke() {
        return counters.getInvoke().getValue();
    }
    
    @ManagedAttribute(description = "The http client invoke Async counter",
                      persistPolicy = "OnUpdate")
    public int getInvokeAsync() {
        return counters.getInvokeAsync().getValue();
    }
    
    @ManagedAttribute(description = "The http client one way invoke counter",
                      persistPolicy = "OnUpdate")
    public int getInvokeOneWay() {
        return counters.getInvokeOneWay().getValue();
    }
    
    @ManagedAttribute(description = "The http client error invoke counter",
                      persistPolicy = "OnUpdate")
    public int getInvokeError() {
        return counters.getInvokeError().getValue();
    }
        
    public Object getComponent() {        
        return httpClientTransport;
    }
   
    public String getInstrumentationName() {
        return INSTRUMENTATION_NAME;
    }

    public String getUniqueInstrumentationName() {
        return objectName;
    }
}
