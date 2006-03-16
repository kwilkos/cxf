package org.objectweb.celtix.bus.transports;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.transports.TransportFactory;

@ManagedResource(objectName = "TransportFactoryManager", 
                 description = "The Celtix bus transport factory manager component ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class TransportFactoryManagerInstrumentation implements Instrumentation {
    private static int instanceNumber;
    private static final String INSTRUMENTED_NAME = "TransportFactoryManager";
    private String objectName;
    private Map<String, TransportFactory> transportFactories;
    private TransportFactoryManagerImpl transportManager;
    

    public TransportFactoryManagerInstrumentation(TransportFactoryManagerImpl transport) {
        transportManager = transport;
        transportFactories = transportManager.transportFactories;
        objectName = INSTRUMENTED_NAME + instanceNumber;
        
    }
    
    public static void resetInstanceNumber() {
        instanceNumber = 0;
    }
    
    public Object getComponent() {
        return transportManager;
    }
    
     
    public String getInstrumentationName() {
        return INSTRUMENTED_NAME;
    }

    @ManagedAttribute(description = "The celtix bus transport factory information",
                      persistPolicy = "OnUpdate")
    public String[] getTransportFactories() {  
        String[] strings = new String[transportFactories.size()];
        Set<Map.Entry<String, TransportFactory>> entries = transportFactories.entrySet();
        int i = 0;
        for (Iterator<Map.Entry<String, TransportFactory>> index = entries.iterator();
                index.hasNext();) {
            Map.Entry<String, TransportFactory> entry = index.next();
            strings[i] = "NameSpace: " + entry.getKey() 
                            + "\n FactoryClass: " + entry.getValue().getClass().getName();
            i++;
        }  
        return strings;
    }
        

    public String getUniqueInstrumentationName() {
        return objectName;
    }

}
