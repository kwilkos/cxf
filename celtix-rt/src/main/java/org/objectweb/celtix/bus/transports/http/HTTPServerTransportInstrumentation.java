package org.objectweb.celtix.bus.transports.http;

import org.objectweb.celtix.bus.management.TransportInstrumentation;
import org.objectweb.celtix.bus.management.counters.TransportServerCounters;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;

@ManagedResource(objectName = "HTTPServerTransport", 
                 description = "The Celtix bus HTTP Server Transport component ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class HTTPServerTransportInstrumentation
    extends TransportInstrumentation
    implements Instrumentation {  
    private static final String INSTRUMENTED_NAME = "Bus.Service.Port.HTTPServerTransport";
    
    JettyHTTPServerTransport httpServerTransport; 
    HTTPServerPolicy policy;   
    TransportServerCounters counters;
  
    //EndpointReference eprf;
    
    public HTTPServerTransportInstrumentation(JettyHTTPServerTransport hsTransport) {
        super(hsTransport.bus);         
        httpServerTransport = hsTransport;        
        // servicename, portname, transport type
        serviceName = findServiceName(httpServerTransport.reference);
        portName = findPortName(httpServerTransport.reference);
        /* Transprot:
            type=Bus.Service.Port.HTTPServerTransport,Bus.Service.Port=SoapPort,
            Bus.Service="{http://ws.celtix.objectweb.org}SOAPService",
            Bus=demos.jmx_runtime*/
        objectName = getPortObjectName() + ",name=HTTP";
        counters = hsTransport.counters;        
    }
    
   
    @ManagedAttribute(description = "Get the Service name",
                      persistPolicy = "OnUpdate")
    public String getServiceName() {
        return serviceName;
    }
    
    @ManagedAttribute(description = "Get the port name",
                      persistPolicy = "OnUpdate")
    public String getPortName() {
        return portName;
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
