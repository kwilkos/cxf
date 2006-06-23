package org.objectweb.celtix.transports.jms;



import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.management.annotation.ManagedAttribute;
import org.objectweb.celtix.management.annotation.ManagedResource;
import org.objectweb.celtix.management.counters.TransportClientCounters;

@ManagedResource(componentName = "JMSClientTransport", 
                 description = "The Celtix bus JMS Client Transport component ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class JMSClientTransportInstrumentation implements Instrumentation {

    private static final String INSTRUMENTATION_NAME = "Bus.Service.Port.JMSClientTransport";
    
    private static int instanceNumber;
    
    JMSClientTransport jmsClientTransport;
    String objectName;
    TransportClientCounters counters;
   
    
    public JMSClientTransportInstrumentation(JMSClientTransport jmsTransport) {        
        jmsClientTransport = jmsTransport;  
        counters = jmsTransport.counters;
        objectName = "JMSClientTransport" + instanceNumber; 
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
