package org.objectweb.celtix.bus.transports.http;



import org.objectweb.celtix.bus.configuration.security.AuthorizationPolicy;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.transports.http.configuration.HTTPClientPolicy;

@ManagedResource(objectName = "HTTPClientTransport", 
                 description = "The Celtix bus HTTP client side transport componnet ", 
                 log = true,
                 logFile = "jmx.log", currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class HTTPClientTransportInstrumentation implements Instrumentation {  
    private static final String INSTRUMENTATION_NAME = "HTTPClientTransport";
    
    private static int instanceNumber;
    
    HTTPClientTransport httpClientTransport;
    String objectName;
    
    public HTTPClientTransportInstrumentation(HTTPClientTransport hcTransport) {
        super();
        httpClientTransport = hcTransport;
        objectName = INSTRUMENTATION_NAME + instanceNumber; 
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
