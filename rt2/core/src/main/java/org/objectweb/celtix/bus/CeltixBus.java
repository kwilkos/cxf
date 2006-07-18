package org.objectweb.celtix.bus;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactoryManager;
import org.objectweb.celtix.bindings.BindingFactoryManagerImpl;
import org.objectweb.celtix.bus.resource.ResourceManagerImpl;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.interceptors.InterceptorProvider;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.resource.ResourceManager;


public class CeltixBus extends Bus implements InterceptorProvider {

    private Configuration configuration;
    private Map<String, Object> properties;
    private BindingFactoryManager bindingFactoryManager;
    // private TransportFactoryManager transportFactoryManager;
    // private WSDLManager wsdlManager;
    // private CeltixBusLifeCycleManager lifeCycleManager;
    // private WorkQueueManager workQueueManager;
    private ResourceManager resourceManager;
    private String busID;
    private List<Phase> inPhases;
    private List<Phase> outPhases;
    private List<Interceptor> inInterceptors;
    private List<Interceptor> outInterceptors;
    private List<Interceptor> faultInterceptors;
    


    /**
     * Used by the <code>BusFactory</code> to initialize a new bus.
     *
     * @param args the command line configuration of this <code>Bus</code>.
     */
    public void initialize(String[] args, Map<String, Object> p)
        throws BusException {

        properties = p;
        
        configuration = null;
        // configuration = new BusConfigurationBuilder().build(args, properties);
        busID = (String)configuration.getId();
        
        bindingFactoryManager = (BindingFactoryManager)properties.get(BindingFactoryManager.class.getName());
        if (null == bindingFactoryManager) {
            bindingFactoryManager = new BindingFactoryManagerImpl(this);
            properties.put(BindingFactoryManager.class.getName(), bindingFactoryManager);
        }
        
        
        
        /*
        if (properties.get(CELTIX_TRANSPORTFACTORYMANAGER) != null) {
            transportFactoryManager = (TransportFactoryManager)properties.get(CELTIX_TRANSPORTFACTORYMANAGER);
        } else {
            transportFactoryManager = new TransportFactoryManagerImpl(this);
        }
        */
        
        /*
        if (properties.get(CELTIX_WSDLMANAGER) != null) {
            wsdlManager = (WSDLManager)properties.get(CELTIX_WSDLMANAGER);
        } else {
            wsdlManager = new WSDLManagerImpl(this);
        }
        */
        
        
        
        // lifeCycleManager = new CeltixBusLifeCycleManager();
        
        // workQueueManager = new WorkQueueManagerImpl(this);
        
        resourceManager = new ResourceManagerImpl(this);   
        
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
        
        bindingFactoryManager.shutdown();
        
        // lifeCycleManager.postShutdown();
        
        super.removeByID(getBusID());
    }

    public void run() {
        // workQueueManager.run();
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


    public WSDLManager getWSDLManager() {
        return wsdlManager;
    }

    public BusLifeCycleManager getLifeCycleManager() {
        return lifeCycleManager;
    }

    public WorkQueueManager getWorkQueueManager() {
        return workQueueManager;
    }

    */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }


   
    public String getBusID() {
        return busID;
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
}
