package org.objectweb.celtix.bus.busimpl;


import java.util.Map;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.bus.bindings.BindingManagerImpl;
import org.objectweb.celtix.bus.jaxws.EndpointRegistry;
import org.objectweb.celtix.bus.resource.ResourceManagerImpl;
import org.objectweb.celtix.bus.transports.TransportFactoryManagerImpl;
import org.objectweb.celtix.bus.workqueue.WorkQueueManagerImpl;
import org.objectweb.celtix.bus.wsdl.WSDLManagerImpl;
import org.objectweb.celtix.buslifecycle.BusLifeCycleManager;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.plugins.PluginManager;
import org.objectweb.celtix.resource.ResourceManager;
import org.objectweb.celtix.transports.TransportFactoryManager;
import org.objectweb.celtix.workqueue.WorkQueueManager;
import org.objectweb.celtix.wsdl.WSDLManager;

public class CeltixBus extends Bus {
    
    private Configuration configuration;
    private BindingManager bindingManager;
    private Object clientRegistry;
    private EndpointRegistry endpointRegistry;
    private TransportFactoryManager transportFactoryManager;
    private WSDLManager wsdlManager;
    private PluginManager pluginManager;
    private CeltixBusLifeCycleManager lifeCycleManager;
    private WorkQueueManager workQueueManager;
    private ResourceManager resourceManager;
    /**
     * Used by the <code>BusFactory</code> to initialize a new bus.
     * 
     * @param args the command line configuration of this <code>Bus</code>.
     */
    public void initialize(String[] args, Map<String, Object> properties)
        throws BusException {

        lifeCycleManager = new CeltixBusLifeCycleManager();
        
        configuration = new BusConfiguration(args, properties);        
        wsdlManager = new WSDLManagerImpl(this);
        transportFactoryManager = new TransportFactoryManagerImpl(this);
        bindingManager = new BindingManagerImpl(this);
        workQueueManager = new WorkQueueManagerImpl(this);
        resourceManager = new ResourceManagerImpl(this);

        // create and initialise the remaining objects:
        // clientRegistry = new ClientRegistry(this);
        
        endpointRegistry = new EndpointRegistry(this);
        
        Bus.setCurrent(this); 
        
        lifeCycleManager.initComplete();
    }
    
    /**
     * Create and initialize a <code>Bus</code> object.
     * 
     * @param args Any args, such as domain name, configuration scope,
     * that may be needed to identify and initialize this <code>Bus</code>.
     * @return Bus If a <code>Bus</code> has already been created using the same args,
     * it will return the existing <code>Bus</code> object.  Otherwise,
     * it creates a new <code>Bus</code>.
     * @throws BusException If there is an error initializing <code>Bus</code>.
     */
    public static synchronized Bus init(String[] args) 
        throws BusException {
        return null;
    }
    
    /**
     * Shuts down the <code>Bus</code>.
     * 
     * @param wait If <code>true</code>, waits for the <code>Bus</code>
     * to shutdown before returning, otherwise returns immediately.
     * @throws BusException
     */
    public void shutdown(boolean wait) throws BusException {
        
        lifeCycleManager.preShutdown();
        
        // shutdown in inverse order of construction
        
        endpointRegistry.shutdown();
        
        // transportRegistry.shutdown(wait);
        // 
        // handlerRegistry.shutdown(wait);
        // clientRegistry.shutdown(wait);
        // bindingManager.shutdown(wait);        
        // configuration.shutdown();
        
        workQueueManager.shutdown(wait);

        lifeCycleManager.postShutdown();
    }   
    
    /* (non-Javadoc)
     * @see org.objectweb.celtix.Bus#run()
     */
    @Override
    public void run() {
        workQueueManager.run();
    }

    /** 
     * Returns the <code>Configuration</code> of this <code>Bus</code>.
     * 
     * @return Configuration the configuration of this <code>bus</code>.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /** 
     * Returns the <code>BindingManager</code> of this <code>Bus</code>.
     * 
     * @return BindingManager of this <code>Bus</code>.
     */
    public BindingManager getBindingManager() {
        return bindingManager;
    }
    
    /** 
     * Returns the <code>TransportFactoryManager</code> of this <code>Bus</code>.
     * 
     * @return TransportFactoryManager the transport factory manager of this <code>Bus</code>.
     */
    public TransportFactoryManager getTransportFactoryManager() {
        return transportFactoryManager;
    }
    
    /** 
     * Returns the <code>ClientRegistry</code> of this <code>Bus</code>.
     * 
     * @return ClientRegistry the client registry of this <code>Bus</code>.
     */
    public Object getClientRegistry() {
        return clientRegistry;
    }
    
    
    public WSDLManager getWSDLManager() {
        return wsdlManager;
    }
    
    /* (non-Javadoc)
     * @see org.objectweb.celtix.Bus#getPluginManager()
     */
    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }
    
    /** 
     * Returns the <code>EndpointRegistry</code> of this <code>Bus</code>.
     * 
     * @return EndpointRegistry the endpoint registry of this <code>Bus</code>.
     */
    Object getEndpointRegistry() {
        return endpointRegistry;
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.Bus#getLifeCycleManager()
     */
    @Override
    public BusLifeCycleManager getLifeCycleManager() {
        return lifeCycleManager;
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.Bus#getWorkQueueManager()
     */
    @Override
    public WorkQueueManager getWorkQueueManager() {
        return workQueueManager;
    }
    

    /* (non-Javadoc)
     * @see org.objectweb.celtix.Bus#getResourceManager()
     */
    @Override
    public ResourceManager getResourceManager() { 
        return resourceManager;
    } 

    
    
}
