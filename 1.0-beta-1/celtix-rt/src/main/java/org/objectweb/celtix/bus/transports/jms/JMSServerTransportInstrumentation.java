package org.objectweb.celtix.bus.transports.jms;

import org.objectweb.celtix.bus.management.counters.TransportServerCounters;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;

@ManagedResource(objectName = "JMSServerTransport", 
                 description = "The Celtix bus JMS Server Transport component ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class JMSServerTransportInstrumentation implements Instrumentation {
    private static final String INSTRUMENTED_NAME = "JMSServerTransport";
    private static int instanceNumber;
    
    JMSServerTransport jmsServerTransport; 
    HTTPServerPolicy policy;
    String objectName;
    TransportServerCounters counters;
    
    public JMSServerTransportInstrumentation(JMSServerTransport jsTransport) {
        
        jmsServerTransport = jsTransport;
        objectName = INSTRUMENTED_NAME + instanceNumber;
        instanceNumber++;
        counters = jsTransport.counters;
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
    
    public static void resetInstanceNumber() {
        instanceNumber = 0;
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
