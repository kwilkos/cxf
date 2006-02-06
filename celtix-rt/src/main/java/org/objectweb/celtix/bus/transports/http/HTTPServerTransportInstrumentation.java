package org.objectweb.celtix.bus.transports.http;



import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;

public class HTTPServerTransportInstrumentation implements Instrumentation {
    static String iNAME = "HTTPServerTransport";
    static int instanceNumber;
    AbstractHTTPServerTransport httpServerTransport; 
    HTTPServerPolicy policy;
    String objectName;
    
    public HTTPServerTransportInstrumentation(AbstractHTTPServerTransport ahsTransport) {
        super();
        httpServerTransport = ahsTransport;
        objectName = iNAME + instanceNumber;
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
  

    public Object getComponent() {
        // TODO Auto-generated method stub
        return httpServerTransport;
    }  

    public String getInstrumentationName() {
        // TODO Auto-generated method stub
        return iNAME;
    }

    public String getUniqueInstrumentationName() {
        // TODO Auto-generated method stub
        return objectName;
    }
    
    // TODO need to set up the performance counter
    
}
