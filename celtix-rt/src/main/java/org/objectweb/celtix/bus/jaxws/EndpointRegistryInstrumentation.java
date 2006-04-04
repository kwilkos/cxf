package org.objectweb.celtix.bus.jaxws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.handler.Handler;

import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedOperation;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.management.Instrumentation;

@ManagedResource(objectName = "EndpointRegistry", 
                 description = "The Celtix bus EndpointRegistry ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class EndpointRegistryInstrumentation implements Instrumentation {
    private static final String INSTRUMENTED_NAME = "Bus.EndpointRegistry";  
    
    private String objectName;
    private EndpointRegistryImpl epRegistry;
    
    public EndpointRegistryInstrumentation(EndpointRegistryImpl registry) {
        epRegistry = registry;
        objectName = "EndpointRegistry"; 
        
    }
    
    public String getInstrumentationName() {
        return INSTRUMENTED_NAME;
    }

    public Object getComponent() {
        return epRegistry;
    }
    
    public String getUniqueInstrumentationName() {
        return objectName;
    }
    
    @ManagedOperation(currencyTimeLimit = 30, 
                      description = "The celtix bus Registed Endpoints address")
    public String[] getEndpointsAddress() {
        List<String> strList = new ArrayList<String>();
        for (EndpointImpl endpoint : epRegistry.getEndpoints()) {
            String address = endpoint.getEndpointReferenceType().getAddress().getValue();
            strList.add(address);
        }        
        String[] strings = new String[strList.size()];
        return strList.toArray(strings);
    }
    
    @ManagedOperation(currencyTimeLimit = 30, 
                      description = "The celtix bus Registed Endpoints's handlerChains")
    public String[] getEndpointsHandlerChains() {
        List<String> strList = new ArrayList<String>();
        for (EndpointImpl endpoint : epRegistry.getEndpoints()) {
            String address = endpoint.getEndpointReferenceType().getAddress().getValue();
            List<Handler> handlers = endpoint.getServerBinding().getBinding().getHandlerChain();
            for (Handler h : handlers) {
                String handler = "Address " + address + ":" + h.getClass().getName();
                strList.add(handler);
            }    
        }
        String[] strings = new String[strList.size()];
        return strList.toArray(strings);
    }


}
