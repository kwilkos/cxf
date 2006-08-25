package org.objectweb.celtix.bus.busimpl;


import java.util.Map;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusEvent;
import org.objectweb.celtix.BusEventCache;
import org.objectweb.celtix.BusEventFilter;
import org.objectweb.celtix.BusEventListener;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.bus.bindings.BindingManagerImpl;
import org.objectweb.celtix.bus.configuration.CeltixConfigurationBuilder;
import org.objectweb.celtix.bus.configuration.ConfigurationEvent;
import org.objectweb.celtix.bus.configuration.ConfigurationEventFilter;
import org.objectweb.celtix.bus.resource.ResourceManagerImpl;
import org.objectweb.celtix.bus.transports.TransportFactoryManagerImpl;
import org.objectweb.celtix.bus.workqueue.WorkQueueManagerImpl;
import org.objectweb.celtix.bus.wsdl.WSDLManagerImpl;
import org.objectweb.celtix.buslifecycle.BusLifeCycleManager;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.jaxws.EndpointRegistry;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.management.InstrumentationFactory;
import org.objectweb.celtix.management.InstrumentationManager;
import org.objectweb.celtix.plugins.PluginManager;
import org.objectweb.celtix.resource.ResourceManager;
import org.objectweb.celtix.transports.TransportFactoryManager;
import org.objectweb.celtix.workqueue.WorkQueueManager;
import org.objectweb.celtix.wsdl.WSDLManager;


public class CeltixBus extends Bus implements BusEventListener, InstrumentationFactory {

    public static final String CELTIX_WSDLMANAGER = "celtix.WSDLManager";
    public static final String CELTIX_TRANSPORTFACTORYMANAGER = "celtix.TRANSPORTFACTORYMANAGER";

    private ConfigurationBuilder configurationBuilder;
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
    private InstrumentationManager instrumentationManager;
    private BusEventCache eventCache;
    private BusEventProcessor eventProcessor;
    private String busID;


    /**
     * Used by the <code>BusFactory</code> to initialize a new bus.
     *
     * @param args the command line configuration of this <code>Bus</code>.
     */
    public void initialize(String[] args, Map<String, Object> properties)
        throws BusException {


        lifeCycleManager = new CeltixBusLifeCycleManager();

        // register a event cache for all bus events
        eventCache = new BusEventCacheImpl(this);
        //TODO: shall we add BusEventProcessor to a celtix bus registry?
        eventProcessor = new BusEventProcessor(this, eventCache);

        configurationBuilder = new CeltixConfigurationBuilder();       
        configuration = new BusConfigurationBuilder().build(configurationBuilder, args, properties);
        
        //Register bus on bus configuration to receive ConfigurationEvent
        ConfigurationEventFilter configurationEventFilter = new ConfigurationEventFilter();
        addListener((BusEventListener)this, configurationEventFilter);

        busID = configuration.getId().toString();

        try {
            //REVISIT - dynamic discovery of objects, generic registry instead of fields
            Object o = Class.forName("org.objectweb.celtix.management.InstrumentationManagerImpl")
                .getConstructor(Bus.class).newInstance(this);
            instrumentationManager = (InstrumentationManager)o;
        } catch (Exception e) {
            //ignore for now
        }


        if (properties.get(CELTIX_WSDLMANAGER) != null) {
            wsdlManager = (WSDLManager)properties.get(CELTIX_WSDLMANAGER);
        } else {
            wsdlManager = new WSDLManagerImpl(this);
        }

        if (properties.get(CELTIX_TRANSPORTFACTORYMANAGER) != null) {
            transportFactoryManager = (TransportFactoryManager)properties.get(CELTIX_TRANSPORTFACTORYMANAGER);
        } else {
            transportFactoryManager = new TransportFactoryManagerImpl(this);
        }

        bindingManager = new BindingManagerImpl(this);
        workQueueManager = new WorkQueueManagerImpl(this);
        resourceManager = new ResourceManagerImpl(this);

        try {
            //REVISIT - dynamic discovery of objects, generic registry instead of fields
            Object o = Class.forName("org.objectweb.celtix.bus.jaxws.EndpointRegistryImpl")
                .getConstructor(Bus.class).newInstance(this);
            endpointRegistry = (EndpointRegistry)o;
        } catch (Exception e) {
            //ignore for now
        }
        
        
        
        Bus.setCurrent(this);

        lifeCycleManager.initComplete();
        //send bus component created event
        this.sendEvent(new BusEvent(this, BusEvent.COMPONENT_CREATED_EVENT));
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
    /*public static synchronized Bus init(String[] args)
        throws BusException {
        return null;
    }*/

    /**
     * Shuts down the <code>Bus</code>.
     *
     * @param wait If <code>true</code>, waits for the <code>Bus</code>
     * to shutdown before returning, otherwise returns immediately.
     * @throws BusException
     */
    public void shutdown(boolean wait) throws BusException {
        //System.out.println("===Shutdown the bus===");
        lifeCycleManager.preShutdown();

        // shutdown in inverse order of construction

        endpointRegistry.shutdown();

        transportFactoryManager.shutdown();
        bindingManager.shutdown();
        wsdlManager.shutdown();
        this.sendEvent(new BusEvent(this, BusEvent.COMPONENT_REMOVED_EVENT));
        // handlerRegistry.shutdown(wait);
        // clientRegistry.shutdown(wait);
        // bindingManager.shutdown(wait);
        // configuration.shutdown();

        workQueueManager.shutdown(wait);
        instrumentationManager.shutdown();
        lifeCycleManager.postShutdown();
        super.removeByID(getBusID());
    }

    /* (non-Javadoc)
     * @see org.objectweb.celtix.Bus#run()
     */
    @Override
    public void run() {
        workQueueManager.run();
    }
    
    public ConfigurationBuilder getConfigurationBuilder() {
        return configurationBuilder;
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
    public EndpointRegistry getEndpointRegistry() {
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

    @Override
    public InstrumentationManager getInstrumentationManager() {
        return instrumentationManager;
    }

    @Override
    public void sendEvent(BusEvent event) {
        eventProcessor.processEvent(event);
    }

    @Override
    public void addListener(BusEventListener l, BusEventFilter filter) throws BusException {
        eventProcessor.addListener(l, filter);
    }

    @Override
    public void removeListener(BusEventListener l) throws BusException {
        eventProcessor.removeListener(l);
    }

    @Override
    public BusEventCache getEventCache() {
        return eventCache;
    }

    @Override
    public String getBusID() {
        return busID;
    }

    public boolean isServicesMonitoring() {
        return configuration.getBoolean("servicesMonitoring");
    }

    public void setServicesMonitoring(boolean pServicesMonitoring) {
        configuration.setBoolean("servicesMonitoring", pServicesMonitoring);
    }

    // The notification between runtime components and corresponding
    // configurations to support dynamic configuration
    // REVISIT!!!
    
    public void processEvent(BusEvent e) throws BusException {
        if (e.getID().equals(ConfigurationEvent.RECONFIGURED)) {
            String configName = (String)e.getSource();
            /*
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("CeltixBus processEvent " + configName + ": reconfigured.");
            }
            */
            reConfigure(configName);
        }
    }

    private void reConfigure(String configName) {
        /*
        if ("servicesMonitoring".equals(configName)) {
            servicesMonitoring = configuration.getBoolean("servicesMonitoring");
        }
        */

    }

    public Instrumentation createInstrumentation() {
        return new CeltixBusInstrumentation(this);
    }
}
