package org.objectweb.celtix.bus.busimpl;

import java.util.ArrayList;
import java.util.List;


import org.objectweb.celtix.bus.bindings.BindingManagerImpl;
import org.objectweb.celtix.bus.transports.TransportFactoryManagerImpl;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingType;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.management.annotation.ManagedAttribute;
import org.objectweb.celtix.management.annotation.ManagedResource;


@ManagedResource(componentName = "CeltixBus", 
                 description = "The Celtix bus managed component ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class CeltixBusInstrumentation implements Instrumentation {
   
    private static final String INSTRUMENTED_NAME = "Bus";
    private String objectName;
    private CeltixBus bus;
    private String[] transportFactories;
    private String[] bindingFactories;
  

    public CeltixBusInstrumentation(CeltixBus b) {
        bus = b;       
        objectName = b.getBusID();
        // TODO: asmyth
        // extend BindingManager, TransportFactoryManager API to avoid the cast
        // or (better) present a dynamic view of the available binding and
        // transport factories
        bindingFactories = getFactoriesInfor(((BindingManagerImpl)b.getBindingManager())
                                             .getFactoryNamespaceMappings());
        
        transportFactories = getFactoriesInfor(((TransportFactoryManagerImpl)b.getTransportFactoryManager())
                                               .getFactoryNamespaceMappings());
    }
        
    
    public Object getComponent() {
        return bus;
    }
    
     
    public String getInstrumentationName() {
        return INSTRUMENTED_NAME;
    }
    
    public final String[] getFactoriesInfor(List<ClassNamespaceMappingType> factoryMappings) {  
        List<String> factoriesList = new ArrayList<String>();        
        for (ClassNamespaceMappingType mapping : factoryMappings) {
            String classname = mapping.getClassname();
            List<String> namespaceList = mapping.getNamespace();
            for (String namespace : namespaceList) {
                String temp = "NameSpace: " + namespace 
                        + "\n FactoryClass: " + classname;
                factoriesList.add(temp);
            }
        }
        String[] factories = new String[factoriesList.size()];
        return factoriesList.toArray(factories);        
    }
      
    @ManagedAttribute(description = "The celtix bus Transport factories information",
                      persistPolicy = "OnUpdate")
    public String[] getTransportFactories() {  
        return transportFactories;
    }

    @ManagedAttribute(description = "The celtix bus binging factories information",
                      persistPolicy = "OnUpdate")
    public String[] getBindingFactories() {  
        return bindingFactories;
    }
    
    @ManagedAttribute(description = "The celtix bus Serivce monitors counters",
                      persistPolicy = "OnUpdate")
    public Boolean isServicesMonitoring() {
        return bus.getConfiguration().getBoolean("servicesMonitoring");
    }
    
    public void setServicesMonitoring(Boolean value) {
        if (value != bus.getConfiguration().getBoolean("servicesMonitoring")) {
            bus.getConfiguration().setObject("servicesMonitoring", value);
        }
    }
    
    public String getUniqueInstrumentationName() {
        return objectName;
    }
}
