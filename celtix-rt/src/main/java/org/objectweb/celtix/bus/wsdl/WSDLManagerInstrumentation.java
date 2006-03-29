package org.objectweb.celtix.bus.wsdl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedOperation;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.management.Instrumentation;

@ManagedResource(objectName = "WSDLManager", 
                 description = "The Celtix bus wsdl model component ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class WSDLManagerInstrumentation implements Instrumentation {
    private static final String INSTRUMENTED_NAME = "WSDLManager";    
    private String objectName;
    private WSDLManagerImpl wsdlManager;
    private WeakHashMap<Object, Definition> definitionsMap;
   
    
    public WSDLManagerInstrumentation(WSDLManagerImpl wsdl) {
        wsdlManager = wsdl;
        objectName = INSTRUMENTED_NAME;
        definitionsMap = wsdlManager.definitionsMap;         
    }
       
    // get the wsdl model information 
    
    @ManagedAttribute(description = "The celtix bus WSDL provider the Service",
                      persistPolicy = "OnUpdate")
    public String[] getServices() {
        List<String> strList = new ArrayList<String>();
        Set<Definition> defSet = new HashSet<Definition>(definitionsMap.values());     
        for (Iterator<Definition> it = defSet.iterator();
            it.hasNext();) {
            Definition definition = it.next();
            String defName = "Definition: " + definition.getQName().toString();          
            for (Iterator jt = definition.getServices().keySet().iterator();
                jt.hasNext();) {
                QName serviceQName = (QName)jt.next();
                String name = defName + " Service: " + serviceQName.toString();
                strList.add(name);
            }                
        }
        String[] strings = new String[strList.size()];
        return strList.toArray(strings);
    }
    
    @ManagedAttribute(description = "The celtix bus WSDL provider the Bindings",
                      persistPolicy = "OnUpdate")
    public String[] getBindings() {       
        List<String> strList = new ArrayList<String>();
        Set<Definition> defSet = new HashSet<Definition>(definitionsMap.values());
        for (Iterator<Definition> it = defSet.iterator();
            it.hasNext();) {
            Definition definition = it.next();
            String defName = "Definition: " + definition.getQName().toString();
            for (Iterator jt = definition.getBindings().values().iterator();
                jt.hasNext();) {
                Binding binding = (Binding)jt.next();
                String name = defName + " Binding: " + binding.getQName().toString();
                strList.add(name);
            }                
        }
        String[] strings = new String[strList.size()];
        return strList.toArray(strings);
    }
    
    @ManagedAttribute(description = "The celtix bus WSDL provider the PortTypes",
                      persistPolicy = "OnUpdate")
    public String[] getPortTypes() {
        List<String> strList = new ArrayList<String>();
        Set<Definition> defSet = new HashSet<Definition>(definitionsMap.values());        
        for (Iterator<Definition> it = defSet.iterator();
            it.hasNext();) {
            Definition definition = it.next();
            String defName = "Definition: " + definition.getQName().toString();
            for (Iterator jt = definition.getPortTypes().values().iterator();
                jt.hasNext();) {
                PortType port = (PortType)jt.next();               
                String name = defName + " PortType: " + port.getQName().toString();
                strList.add(name);
            }                
        }
        String[] strings = new String[strList.size()];
        return strList.toArray(strings);
    }
    
    // get the address 
    
    @ManagedOperation(currencyTimeLimit = 30, 
                      description = "The celtix bus WSDL defined service and port provider operation")
    public String[] getOperation(String def, String pt) {
        List<String> strList = new ArrayList<String>();
        Definition definition = null;
        PortType port = null;
        Set<Definition> defSet = new HashSet<Definition>(definitionsMap.values());
        for (Iterator<Definition> it = defSet.iterator();
            it.hasNext();) {
            definition = it.next();
            if (def.compareTo(definition.getQName().getLocalPart()) == 0) {
                for (Iterator jt = definition.getPortTypes().values().iterator();
                        jt.hasNext();) {                    
                    port = (PortType)jt.next();
                    if (pt.compareTo(port.getQName().getLocalPart()) == 0) {
                        break;
                    }
                }
                break;
            }
        }
        if (definition != null && port != null) {
            for (Iterator it = port.getOperations().iterator(); it.hasNext();) {
                Operation opt = (Operation)it.next();
                String name = "Operation: " + opt.getName();
                strList.add(name);
            }                
        }
        String[] strings = new String[strList.size()];
        return strList.toArray(strings);
    }
    
       
    public String getInstrumentationName() {
        return INSTRUMENTED_NAME;
    }

    public Object getComponent() {
        return wsdlManager;
    }
        

    public String getUniqueInstrumentationName() {
        return objectName;
    }

}
