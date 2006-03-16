package org.objectweb.celtix.bus.wsdl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;


import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.management.Instrumentation;

@ManagedResource(objectName = "WSDLManager", 
                 description = "The Celtix bus wsdl model component ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class WSDLManagerInstrumentation implements Instrumentation {
    private static final String INSTRUMENTED_NAME = "WSDLManager";
    private static int instanceNumber;
    private String objectName;
    private WSDLManagerImpl wsdlManager;
    private WeakHashMap<Object, Definition> definitionsMap;
    private Definition definition;
    

    public WSDLManagerInstrumentation(WSDLManagerImpl wsdl) {
        wsdlManager = wsdl;
        objectName = INSTRUMENTED_NAME + instanceNumber;
        definitionsMap = wsdlManager.definitionsMap;        
    }
    
    public static void resetInstanceNumber() {
        instanceNumber = 0;
    }
    // get the wsdl model information 
    
    @ManagedAttribute(description = "The celtix bus provider the Service",
                      persistPolicy = "OnUpdate")
    public String[] getServices() {
        List<String> strList = new ArrayList<String>();
        for (Iterator<Definition> it = definitionsMap.values().iterator();
            it.hasNext();) {
            definition = it.next();
            String defName = definition.getQName().toString();          
            for (Iterator jt = definition.getServices().keySet().iterator();
                jt.hasNext();) {
                QName serviceQName = (QName)jt.next();
                String serviceName = defName + ":" + serviceQName.toString();
                strList.add(serviceName);
            }                
        }
        String[] strings = new String[strList.size()];
        return strList.toArray(strings);
    }
    
    @ManagedAttribute(description = "The celtix bus provider the Bindings",
                      persistPolicy = "OnUpdate")
    public String[] getBindings() {       
        List<String> strList = new ArrayList<String>(); 
        for (Iterator<Definition> it = definitionsMap.values().iterator();
            it.hasNext();) {
            definition = it.next();
            String defName = definition.getQName().toString();
            for (Iterator jt = definition.getBindings().values().iterator();
                jt.hasNext();) {
                Binding binding = (Binding)jt.next();
                String serviceName = defName + ":" + binding.getQName().toString();
                strList.add(serviceName);
            }                
        }
        String[] strings = new String[strList.size()];
        return strList.toArray(strings);
    }
    
    @ManagedAttribute(description = "The celtix bus provider the PortTypes",
                      persistPolicy = "OnUpdate")
    public String[] getPortTypes() {
        List<String> strList = new ArrayList<String>();
        for (Iterator<Definition> it = definitionsMap.values().iterator();
            it.hasNext();) {
            definition = it.next();
            String defName = definition.getQName().toString();
            for (Iterator jt = definition.getPortTypes().values().iterator();
                jt.hasNext();) {
                PortType port = (PortType)jt.next();
                String serviceName = defName + ":" + port.getQName().toString();
                strList.add(serviceName);
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
