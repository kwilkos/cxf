package org.objectweb.celtix.bus.bindings;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.management.Instrumentation;

@ManagedResource(objectName = "BindingManager", 
                 description = "The Celtix bus binding manager component ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class BindingManagerInstrumentation implements Instrumentation {
    private static final String INSTRUMENTED_NAME = "BindingManager";
    private static int instanceNumber;
    String objectName;
    BindingManagerImpl bindingManager;
    Map<String, BindingFactory> bindingFactories;
    
    public BindingManagerInstrumentation(BindingManagerImpl binding) {
        bindingManager = binding;
        bindingFactories = binding.bindingFactories;
        objectName = INSTRUMENTED_NAME + instanceNumber;
        instanceNumber++;        
    }

    public static void resetInstanceNumber() {
        instanceNumber = 0;
    }
    
    @ManagedAttribute(description = "The celtix bus binging factories information",
                      persistPolicy = "OnUpdate")
    public String[] getBindingFactories() {  
        //NameSpace and Factories name        
        String[] strings = new String[bindingFactories.size()];
        Set<Map.Entry<String, BindingFactory>> entries = bindingFactories.entrySet();
        int i = 0;
        for (Iterator<Map.Entry<String, BindingFactory>> index = entries.iterator();
                index.hasNext();) {
            Map.Entry<String, BindingFactory> entry = index.next();
            strings[i] = "NameSpace: " + entry.getKey() 
                            + "\n FactoryClass: " + entry.getValue().getClass().getName();
            i++;
        }  
        return strings;
    }
    
    public String getInstrumentationName() {        
        return INSTRUMENTED_NAME;
    }

    public Object getComponent() {       
        return bindingManager;
    }

    public String getUniqueInstrumentationName() {        
        return objectName;
    }    
   

}
