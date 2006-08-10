package org.objectweb.celtix.bus;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.extension.ExtensionManager;
import org.objectweb.celtix.extension.ExtensionManagerImpl;
import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.phase.Phase;


public class CeltixBus implements Bus {
    
    public static final String BUS_PROPERTY_NAME = "bus";
    
    private static final String BUS_EXTENSION_RESOURCE = "META-INF/bus-extensions.xml";
    
    
    private List<Phase> inPhases;
    private List<Phase> outPhases;
    private List<Interceptor> inInterceptors;
    private List<Interceptor> outInterceptors;
    private List<Interceptor> faultInterceptors;
    private Map<Class, Object> extensions;
    private ExtensionManager extensionManager;
    
    public CeltixBus() {
        this(new HashMap<Class, Object>());
    }
    
    public CeltixBus(Map<Class, Object> e) {
        
        extensions = e;
        
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(BUS_PROPERTY_NAME, this);
   
        extensionManager = new ExtensionManagerImpl(BUS_EXTENSION_RESOURCE, 
                                                    Thread.currentThread().getContextClassLoader(),
                                                    properties);
        
        createPhases();
                
    }
    
    public List<Interceptor> getFaultInterceptors() {
        return faultInterceptors;
    }

    public List<Interceptor> getInInterceptors() {
        return inInterceptors;
    }    

    public List<Interceptor> getOutInterceptors() {
        return outInterceptors;
    }
      
    public <T> T getExtension(Class<T> extensionType) {
        Object obj = extensions.get(extensionType);
        if (null != obj) {
            return extensionType.cast(obj);
        }
        return extensionManager.getExtension(extensionType);
    }

    public <T> void setExtension(T extension, Class<T> extensionType) {
        extensions.put(extensionType, extension);
    }
    
    // TODO: define PhaseManager interface and register PhaseManagerImpl as an extension
    
    public List<Phase> getInPhases() {
        return inPhases;
    }

    public List<Phase> getOutPhases() {
        return outPhases;
    }
    
    // TODO:
    public Configuration getConfiguration() {
        return null;
    }

    final void createPhases() {
        PhaseFactory pf = new PhaseFactory(this);
        inPhases = pf.createInPhases();
        outPhases = pf.createOutPhases();
    }

}
