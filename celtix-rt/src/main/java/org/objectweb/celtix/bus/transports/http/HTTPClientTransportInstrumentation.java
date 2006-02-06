package org.objectweb.celtix.bus.transports.http;



import org.objectweb.celtix.bus.configuration.security.AuthorizationPolicy;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.transports.http.configuration.HTTPClientPolicy;

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
