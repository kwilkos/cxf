package org.objectweb.celtix.bus.transports.jms;


import org.objectweb.celtix.bus.management.counters.TransportClientCounters;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.management.Instrumentation;

@ManagedResource(objectName = "JMSClientTransport", 
                 description = "The Celtix bus JMS Client Transport component ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class JMSClientTransportInstrumentation implements Instrumentation {

    private static final String INSTRUMENTATION_NAME = "JMSClientTransport";
    
    private static int instanceNumber;
    
    JMSClientTransport jmsClientTransport;
    String objectName;
    TransportClientCounters counters;
   
    
    public JMSClientTransportInstrumentation(JMSClientTransport jmsTransport) {        
        jmsClientTransport = jmsTransport;  
        counters = jmsTransport.counters;
        objectName = INSTRUMENTATION_NAME + instanceNumber; 
        instanceNumber++;
    }
    
    public static void resetInstanceNumber() {
        instanceNumber = 0;
    }
    
   
    @ManagedAttribute(description = "The JMS client invoke counter",
                      persistPolicy = "OnUpdate")
    public int getInvoke() {
        return counters.getInvoke().getValue();
    }
    
    @ManagedAttribute(description = "The JMS client invoke Async counter",
                      persistPolicy = "OnUpdate")
    public int getInvokeAsync() {
        return counters.getInvokeAsync().getValue();
    }
    
    @ManagedAttribute(description = "The JMS client one way invoke counter",
                      persistPolicy = "OnUpdate")
    public int getInvokeOneWay() {
        return counters.getInvokeOneWay().getValue();
    }
    
    @ManagedAttribute(description = "The JMS client error invoke counter",
                      persistPolicy = "OnUpdate")
    public int getInvokeError() {
        return counters.getInvokeError().getValue();
    }
        
    public Object getComponent() {        
        return jmsClientTransport;
    }
   
    public String getInstrumentationName() {
        return INSTRUMENTATION_NAME;
    }

    public String getUniqueInstrumentationName() {
        return objectName;
    }
}
