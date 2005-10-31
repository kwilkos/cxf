package org.objectweb.celtix;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.bus.busimpl.BusFactory;
import org.objectweb.celtix.buslifecycle.BusLifeCycleManager;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.handlers.HandlerFactoryManager;
import org.objectweb.celtix.plugins.PluginManager;
import org.objectweb.celtix.transports.TransportFactoryManager;
import org.objectweb.celtix.workqueue.WorkQueueManager;
import org.objectweb.celtix.wsdl.WSDLManager;

/**
 * The Bus class provides access to configuration, factories and managers
 * for use by an application.
 */
public abstract class Bus {
    
    public static final String BUS_CLASS_PROPERTY = "org.objectweb.celtix.BusClass";

    private static ThreadLocal<Bus> current = new ThreadLocal<Bus>();
    
    /**
     * Returns a newly created and fully initialised <code>Bus</code>.
     * 
     * @return Bus the newly created <code>Bus</code>.
     * @throws BusException If there is an error initializing <code>Bus</code>.
     */
    public static synchronized Bus init() throws BusException {
        return init(new String[0]);
    }

    /**
     * Returns a newly created and fully initialised <code>Bus</code>.
     * 
     * @param args any args, such as domain name, bus class, and other configuration
     * options that can be used to initialize this <code>Bus</code>.
     * @return Bus the newly created <code>Bus</code>.
     * @throws BusException If there is an error initializing <code>Bus</code>.
     */
    public static synchronized Bus init(String[] args) throws BusException {
        return init(args, new HashMap<String, Object>());
    }
    
    /**
     * Returns a newly created and fully initialised <code>Bus</code>.
     * 
     * @param args any args, such as domain name, bus class, and other configuration
     * options that can be used to initialize this <code>Bus</code>.
     * @param properties any properties, such as bus identifier, bus class, and other configuration
     * options that can be used to identify and initialize this <code>Bus</code>. 
     * The properties are superceded by the settings in the <code>args</code> parameter,
     * and they in turn supercede system properties.
     * @return Bus the newly created <code>Bus</code>.
     * @throws BusException If there is an error initializing <code>Bus</code>.
     */
    public static synchronized Bus init(String[] args, Map<String, Object> properties) throws BusException {
        return init(args, properties, null);
    }
    
    /**
     * Returns a newly created and fully initialised <code>Bus</code>.
     * 
     * @param args any args, such as domain name, bus class, and other configuration
     * options that can be used to initialize this <code>Bus</code>.
     * @param properties any properties, such as domain name, bus class, and other configuration
     * options that can be used to initialize this <code>Bus</code>. 
     * The properties are superceded by the settings in the <code>args</code> parameter,
     * and they in turn supercede system properties.
     * @param classLoader an optional classloader to use when instantiating a <code>Bus</code>
     * needs to be instantiated (defaults to the current thread's context classloader).
     * @return Bus the newly created <code>Bus</code>.
     * @throws BusException If there is an error initializing <code>Bus</code>.
     */
    public static synchronized Bus init(String[] args, 
        Map<String, Object> properties, 
        ClassLoader classLoader) throws BusException {
        
        // delegate to the factory 
        BusFactory bf = BusFactory.getInstance();
        return bf.getBus(args, properties, classLoader);
    }
    
    /** 
    * Returns the current <code>Bus</code> on this thread.
    * 
    * @return the current <code>Bus</code> on this thread.
    */
    public static Bus getCurrent() {        
        return current.get();
    }
   
    /** 
    * Sets the current <code>Bus</code>
    * This method has been added to make the association between a <code>Service</code>
    * and a <code>Bus</code> - as a runtime environment for the <code>Service</code> 
    * more explicit. Recurring to the last <code>Bus</code> created in the process
    * or the current <code>Bus</code> on a thread is necessary as the 
    * JAX-WS <code>ServiceFactory</code> must be implemented as a Singleton.
    * By exposing these APIs the dependency
    * becomes more obvious and - more importantly - can be controlled by the application
    * developer.
    * 
    * @param bus The <code>Bus</code> designated to be the current one on this thread.
    */
    public static void setCurrent(Bus bus) {
        current.set(bus);
    }
    
    /**
     * Shuts down the <code>Bus</code>.
     * 
     * @param wait If <code>true</code>, waits for the <code>Bus</code>
     * to shutdown before returning, otherwise returns immediately.
     * @throws BusException
     */
    public abstract void shutdown(boolean wait) throws BusException;

    /** 
     * Returns the <code>Configuration</code> of this <code>Bus</code>.
     * 
     * @return Configuration the configuration of this <code>bus</code>.
     */
    public abstract Configuration getConfiguration();

    /** 
     * Returns the <code>HandlerFactoryManager</code> of this <code>Bus</code>.
     * 
     * @return HandlerFactoryManager the handler factory manager of this
     * <code>Bus</code>.
     */
    public abstract HandlerFactoryManager getHandlerFactoryManager();

    /** 
     * Returns the <code>TransportFactoryManager</code> of this <code>Bus</code>.
     * 
     * @return TransportRegistry the servant registry of this <code>Bus</code>.
     */
    public abstract TransportFactoryManager getTransportFactoryManager();

    /** 
     * Returns the <code>BindingManager</code> of this <code>Bus</code>.
     * 
     * @return BindingManager the binding manager of this <code>Bus</code>.
     */
    public abstract BindingManager getBindingManager();

    /** 
     * Returns the <code>ClientRegistry</code> of this <code>Bus</code>.
     * 
     * @return WSDLManager the wsdl manager of this <code>Bus</code>.
     */
    public abstract WSDLManager getWSDLManager();

    /** 
     * Returns the <code>PluginManager</code> of this <code>Bus</code>.
     * 
     * @return PluginManager the plugin manager of this <code>Bus</code>.
     */
    public abstract PluginManager getPluginManager();

    /** 
     * Returns the <code>BusLifeCycleManager</code> of this <code>Bus</code>.
     * 
     * @return BusLifeCycleManager of this <code>Bus</code>.
     */
    public abstract BusLifeCycleManager getLifeCycleManager();

    /** 
     * Returns the <code>WorkQueueManager</code> of this <code>Bus</code>.
     * 
     * @return WorkQueueManager of this <code>Bus</code>.
     */
    public abstract WorkQueueManager getWorkQueueManager();
    
    /**
     * Starts processing bus events, and returns only after the <code>Bus</code> has been shut down
     * (from another thread).
     *
     */
    public abstract void run();

    public abstract void initialize(String[] args,
            Map<String, Object> properties) throws BusException;

}
