package org.objectweb.celtix.bus.busimpl;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingListType;
import org.objectweb.celtix.configuration.types.ClassNamespaceMappingType;

import org.objectweb.celtix.management.Instrumentation;


@ManagedResource(objectName = "CeltixBus", 
                 description = "The Celtix bus managed component ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class CeltixBusInstrumentation implements Instrumentation {
   
    private static final String INSTRUMENTED_NAME = "CeltixBus";
    private String objectName;
    private CeltixBus bus;
    private String[] transportFactories;
    private String[] bindingFactories;
  

    public CeltixBusInstrumentation(CeltixBus b) {
        bus = b;
        objectName = INSTRUMENTED_NAME;
        bindingFactories = getFactoriesInfor("bindingFactories");
        transportFactories = getFactoriesInfor("transportFactories");
    }
        
    
    public Object getComponent() {
        return bus;
    }
    
     
    public String getInstrumentationName() {
        return INSTRUMENTED_NAME;
    }
    
    public final String[] getFactoriesInfor(String cfgName) {  
        List<String> factoriesList = new ArrayList<String>();        
        Object obj = bus.getConfiguration().getObject(cfgName);
        assert obj != null;
        List<ClassNamespaceMappingType> factoryMappings = ((ClassNamespaceMappingListType)obj).getMap();
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
    
    public String getUniqueInstrumentationName() {
        return objectName;
    }
}
