package org.objectweb.celtix.bus;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactoryManager;
import org.objectweb.celtix.bindings.BindingFactoryManagerImpl;
import org.objectweb.celtix.bus.resource.ResourceManagerImpl;
import org.objectweb.celtix.buslifecycle.BusLifeCycleManager;
import org.objectweb.celtix.buslifecycle.CeltixBusLifeCycleManager;
import org.objectweb.celtix.common.injection.ResourceInjector;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationException;
import org.objectweb.celtix.configuration.impl.ConfigurationBuilderImpl;
import org.objectweb.celtix.event.EventProcessor;
import org.objectweb.celtix.event.EventProcessorImpl;
import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.management.InstrumentationManager;
import org.objectweb.celtix.management.InstrumentationManagerImpl;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.resource.PropertiesResolver;
import org.objectweb.celtix.resource.ResourceManager;
import org.objectweb.celtix.wsdl.WSDLManager;
import org.objectweb.celtix.wsdl11.WSDLManagerImpl;


public class CeltixBus extends Bus {

    public static final String BUS_PROPERTY_NAME = "bus";
    public static final String CONFIGURATIONBUILDER_PROPERTY_NAME = "configurationBuilder";
    public static final String BINDINGFACTORYMANAGER_PROPERTY_NAME = "bindingFactoryManager";
    public static final String TRANSPORTFACTORYMANAGER_PROPERTY_NAME = "transportFactoryManager";
    public static final String WSDL11MANAGER_PROPERTY_NAME = "wsdl11Manager";
    public static final String INSTRUMENTATIONMANAGER_PROPERTY_NAME = "instrumentationManager";
    public static final String EVENTPROCESSOR_PROPERTY_NAME = "eventProcessor";
    public static final String LIFECYCLEMANAGER_PROPERTY_NAME = "lifeCycleManager";
    public static final String WORKQUEUEMANAGER_PROPERTY_NAME = "workQueueManager";
    public static final String RESOURCEMANAGER_PROPERTY_NAME = "resourceManager";

    
    
    private ConfigurationBuilder configurationBuilder;
    private Configuration configuration;
    private Map<String, Object> properties = new HashMap<String, Object>();
    private BindingFactoryManager bindingFactoryManager;
    // private TransportFactoryManager transportFactoryManager;
    private WSDLManager wsdl11Manager;
    private InstrumentationManager instrumentationManager;
    private EventProcessor eventProcessor;
    private BusLifeCycleManager lifeCycleManager;
    // private WorkQueueManager workQueueManager;
    private ResourceManager resourceManager;

    private List<Phase> inPhases;
    private List<Phase> outPhases;
    private List<Interceptor> inInterceptors;
    private List<Interceptor> outInterceptors;
    private List<Interceptor> faultInterceptors;
    


    /**
     * Used by the <code>BusFactory</code> to initialize a new bus.
     *
     * @param p properties for this <code>Bus</code>.
     */
    public void initialize(Map<String, Object> p)
        throws BusException {

        if (null != p) {
            properties.putAll(p);
        }
       
         
        configurationBuilder = (ConfigurationBuilder)properties.get(CONFIGURATIONBUILDER_PROPERTY_NAME);
        if (null == configurationBuilder) {            
            configurationBuilder = new ConfigurationBuilderImpl();
            properties.put(CONFIGURATIONBUILDER_PROPERTY_NAME, configurationBuilder);
        }
        
        Collection<Object> newPropertyValues = new ArrayList<Object>();
        try { 
            configuration = new BusConfigurationBuilder().build(configurationBuilder, properties);
        } catch (ConfigurationException ex) {
            // TODO: bus configuration metadata
        }
        
        bindingFactoryManager = (BindingFactoryManager)properties.get(BINDINGFACTORYMANAGER_PROPERTY_NAME);
        if (null == bindingFactoryManager) {            
            bindingFactoryManager = new BindingFactoryManagerImpl();
            properties.put(BINDINGFACTORYMANAGER_PROPERTY_NAME, bindingFactoryManager);
            newPropertyValues.add(bindingFactoryManager);
        }
        
            
        /*
        if (properties.get(CELTIX_TRANSPORTFACTORYMANAGER) != null) {
            transportFactoryManager = (TransportFactoryManager)properties.get(CELTIX_TRANSPORTFACTORYMANAGER);
        } else {
            transportFactoryManager = new TransportFactoryManagerImpl(this);
        }
        */
        

        wsdl11Manager = (WSDLManager)properties.get(WSDL11MANAGER_PROPERTY_NAME);
        if (null == wsdl11Manager) {            
            wsdl11Manager = new WSDLManagerImpl();
            properties.put(BINDINGFACTORYMANAGER_PROPERTY_NAME, wsdl11Manager);
            newPropertyValues.add(wsdl11Manager);
        }

        
        instrumentationManager = (InstrumentationManager)properties.get(INSTRUMENTATIONMANAGER_PROPERTY_NAME);
        if (null == instrumentationManager) {            
            instrumentationManager = new InstrumentationManagerImpl();
            properties.put(INSTRUMENTATIONMANAGER_PROPERTY_NAME, instrumentationManager);
            newPropertyValues.add(instrumentationManager);
        }
        
        eventProcessor = (EventProcessor)properties.get(EVENTPROCESSOR_PROPERTY_NAME);
        if (null == instrumentationManager) {            
            eventProcessor = new EventProcessorImpl();
            properties.put(EVENTPROCESSOR_PROPERTY_NAME, EVENTPROCESSOR_PROPERTY_NAME);
            newPropertyValues.add(eventProcessor);
        }
        
        lifeCycleManager = (BusLifeCycleManager)properties.get(LIFECYCLEMANAGER_PROPERTY_NAME);
        if (null == lifeCycleManager) {            
            lifeCycleManager = new CeltixBusLifeCycleManager();
            properties.put(LIFECYCLEMANAGER_PROPERTY_NAME, lifeCycleManager);
            newPropertyValues.add(lifeCycleManager);
        }
        
        // workQueueManager = new WorkQueueManagerImpl(this);
        
        resourceManager = new ResourceManagerImpl();
        
        injectResources(newPropertyValues, false); 
        
        createPhases();
        
        createInterceptors();
               
        Bus.setCurrent(this);

        // lifeCycleManager.initComplete();
    }

    /**
     * Shuts down the <code>Bus</code>.
     *
     * @param wait If <code>true</code>, waits for the <code>Bus</code>
     * to shutdown before returning, otherwise returns immediately.
     * @throws BusException
     */
    public void shutdown(boolean wait) throws BusException {
        
        // lifeCycleManager.preShutdown();
        
        // shutdown in inverse order of construction
        
        // workQueueManager.shutdown(wait);
        
        // wsdlManager.shutdown();

        // transportFactoryManager.shutdown();
        
        // lifeCycleManager.postShutdown();
        
        super.removeByID(getBusID());
    }

    public void run() {
        // workQueueManager.run();
    }

    public ConfigurationBuilder getConfigurationBuilder() {
        return configurationBuilder;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public BindingFactoryManager getBindingManager() {
        return bindingFactoryManager;
    }

    /*
    public TransportFactoryManager getTransportFactoryManager() {
        return transportFactoryManager;
    }
    */

    public WSDLManager getWSDL11Manager() {
        return wsdl11Manager;
    }
    
    public InstrumentationManager getInstrumentationManager() {
        return instrumentationManager;
    }
    
    public EventProcessor getEventProcessor() {
        return eventProcessor;
    }

    public BusLifeCycleManager getLifeCycleManager() {
        return lifeCycleManager;
    }
    
    /*
    public WorkQueueManager getWorkQueueManager() {
        return workQueueManager;
    }

    */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }


   
    public String getBusID() {
        if (null != configuration) {
            return (String)configuration.getId();
        }
        return BusConfigurationBuilder.DEFAULT_BUS_ID;
    }
    
    public List<Phase> getInPhases() {
        return inPhases;
    }
    
    public List<Phase> getOutPhases() {
        return outPhases;
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
    
    /**
     * Inject resources into all bus properties. 
     * The resource manager used by the injector includes a resolver that resolves the bus
     * properties themselves, thus allowing one property value to obtain a reference to 
     * another or the bus.
     * Alternatively, only inject the bus itself.
     * 
     */
    void injectResources(Collection<Object> objs, boolean all) { 
        
        Map<String, Object> injectedProperties = null;
        if (all) {
            injectedProperties = new HashMap<String, Object>(properties);      
        } else {
            injectedProperties = new HashMap<String, Object>();
        }
        injectedProperties.put(BUS_PROPERTY_NAME, this);
        
        ResourceManager rm = new ResourceManagerImpl();
        rm.addResourceResolver(new PropertiesResolver(injectedProperties));
        ResourceInjector injector = new ResourceInjector(rm);
        
        Iterator it = objs.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (this != o) {
                injector.inject(o);
            }
        }
    }

    void createPhases() {
        PhaseFactory pf = new PhaseFactory(this);
        inPhases = pf.createInPhases();       
        outPhases = pf.createOutPhases();
    }
    
    void createInterceptors() {
        inInterceptors = new ArrayList<Interceptor>();
        outInterceptors = new ArrayList<Interceptor>();
        faultInterceptors = new ArrayList<Interceptor>();
        
        // TODO: initialise from configuration
    }
    
    protected Map<String, Object> getProperties() {
        return properties;
    }
}
