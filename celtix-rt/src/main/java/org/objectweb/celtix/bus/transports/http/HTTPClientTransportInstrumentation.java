package org.objectweb.celtix.bus.transports.http;



import org.objectweb.celtix.bus.configuration.security.AuthorizationPolicy;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.transports.http.configuration.HTTPClientPolicy;

public class HTTPClientTransportInstrumentation implements Instrumentation {  
    private static int instanceNumber;
    private static String iName = "HTTPClientTransport";
    HTTPClientTransport httpClientTransport;
    String objectName;
    public HTTPClientTransportInstrumentation(HTTPClientTransport hcTransport) {
        super();
        httpClientTransport = hcTransport;
        objectName = iName + instanceNumber; 
        instanceNumber++;
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
        // TODO Auto-generated method stub
        return iName;
    }

    public String getUniqueInstrumentationName() {
        // TODO Auto-generated method stub
        return objectName;
    }
}
