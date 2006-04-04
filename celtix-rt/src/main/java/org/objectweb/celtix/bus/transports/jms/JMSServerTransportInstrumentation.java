package org.objectweb.celtix.bus.transports.jms;

import org.objectweb.celtix.bus.management.TransportInstrumentation;
import org.objectweb.celtix.bus.management.counters.TransportServerCounters;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;

@ManagedResource(objectName = "JMSServerTransport", 
                 description = "The Celtix bus JMS Server Transport component ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class JMSServerTransportInstrumentation 
    extends TransportInstrumentation
    implements Instrumentation {
    private static final String INSTRUMENTED_NAME = "Bus.Service.Port.JMSServerTransport";  
    
    JMSServerTransport jmsServerTransport; 
    HTTPServerPolicy policy;
    String objectName;
    TransportServerCounters counters;
    
    public JMSServerTransportInstrumentation(JMSServerTransport jsTransport) {
        super(jsTransport.bus);        
        jmsServerTransport = jsTransport;
        serviceName = findServiceName(jmsServerTransport.targetEndpoint);
        portName = findPortName(jmsServerTransport.targetEndpoint);        
        objectName = getPortObjectName();
        counters = jsTransport.counters;
    }
    
    @ManagedAttribute(description = "Get the Service name",
                      persistPolicy = "OnUpdate")
    public String getServiceName() {
        return serviceName;
    }
    
    @ManagedAttribute(description = "Get the Port name",
                      persistPolicy = "OnUpdate")
    public String getPortName() {
        return portName;
    }
   
    @ManagedAttribute(description = "The JMS server request error",
                      persistPolicy = "OnUpdate")
    public int getTotalError() {
        return counters.getTotalError().getValue();
    }
    
    @ManagedAttribute(description = "The JMS server total request counter",
                      persistPolicy = "OnUpdate")
    public int getRequestTotal() {
        return counters.getRequestTotal().getValue();
    }
    
    @ManagedAttribute(description = "The JMS server one way request counter",
                      persistPolicy = "OnUpdate")
    public int getRequestOneWay() {
        return counters.getRequestOneWay().getValue();
    }   
   
    
    public String getInstrumentationName() {
        return INSTRUMENTED_NAME;
    }

    public Object getComponent() {        
        return jmsServerTransport;
    }

    public String getUniqueInstrumentationName() {        
        return objectName;
    }
    
    

}
