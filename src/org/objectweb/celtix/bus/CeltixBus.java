package org.objectweb.celtix.bus;

import java.util.Map;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.handlers.HandlerFactoryManager;
import org.objectweb.celtix.transports.TransportFactoryManager;
import org.objectweb.celtix.wsdl.WSDLManager;

public class CeltixBus extends Bus {
    
    private Configuration configuration;
    private Object bindingManager;
    private Object clientRegistry;
    private HandlerFactoryManager handlerFactoryManager;
    private Object servantRegistry;
    private TransportFactoryManager transportFactoryManager;
    private WSDLManagerImpl wsdlManager;
    // maybe a plugin manager too ...
    
    /**
     * Protected constructor used by the <code>BusManager</code> to create a new bus.
     * 
     * @param busArgs the command line configuration of this <code>Bus</code>.
     */
    protected void initialize(String id, String[] args, Map<String, Object> properties)
        throws BusException {
        
        // the real thing ...
        
        configuration = new BusConfiguration(id, args, properties);
        
        // (the bus) configuration should be completely intialized by now
        
        wsdlManager = new WSDLManagerImpl(this);
        handlerFactoryManager = new HandlerFactoryManagerImpl(this);
        transportFactoryManager = new TransportFactoryManagerImpl(this);
        
        // create and initialise the remaining objects:
        
        // bindingManager = new BindingManager(this);
        // clientRegistry = new ClientRegistry(this);
        
        // servantRegistry = new ServantRegistry(this);
        
                
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
        
        // shutdown in inverse order of construction
        
        // transportRegistry.shutdown(wait);
        // servantRegistry.shutdown(wait);
        // handlerRegistry.shutdown(wait);
        // clientRegistry.shutdown(wait);
        // bindingManager.shutdown(wait);        
        // configuration.shutdown();

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
     * Returns the <code>HandlerRegistry</code> of this <code>Bus</code>.
     * 
     * @return HandlerRegistry the servant registry of this <code>Bus</code>.
     */
    public HandlerFactoryManager getHandlerFactoryManager() {
        return handlerFactoryManager;
    }
    
    /** 
     * Returns the <code>TransportRegistry</code> of this <code>Bus</code>.
     * 
     * @return TransportRegistry the servant registry of this <code>Bus</code>.
     */
    public TransportFactoryManager getTransportFactoryManager() {
        return transportFactoryManager;
    }
    
    /** 
     * Returns the <code>ServantRegistry</code> of this <code>Bus</code>.
     * 
     * @return ServantRegistry the servant registry of this <code>Bus</code>.
     */
    public Object getServantRegistry() {
        return null;
    }
    
    /** 
     * Returns the <code>ClientRegistry</code> of this <code>Bus</code>.
     * 
     * @return ClientRegistry the client registry of this <code>Bus</code>.
     */
    public Object getClientRegistry() {
        return null;
    }
    
    
    public WSDLManager getWSDLManager() {
        return wsdlManager;
    }

    
    /**
     * 
     */
    public String toString() {
        return null;
    }
}
