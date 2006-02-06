package org.objectweb.celtix;


import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.buslifecycle.BusLifeCycleManager;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.management.InstrumentationManager;
import org.objectweb.celtix.plugins.PluginManager;
import org.objectweb.celtix.resource.ResourceManager;
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
    private static Bus defaultBus; 
    
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
    * Returns the current <code>Bus</code> on this thread.  If no bus
    * has been initialised on this thread, return the default bus.
    * 
    * @return the current <code>Bus</code> on this thread.
    */
    public static Bus getCurrent() {        
        Bus ret = current.get();
        if (ret == null) { 
            ret = getDefaultBus(); 
        } 
        return ret; 
    }


    /** 
    * Sets the current <code>Bus</code>.  If a bus is explicitly
    * initialised on a thread, this is the current bus.  If no thread
    * has been initialised (implicitly or explicitly), setting the
    * current bus will set the default bus for all threads
    *
    * @param bus the current bus
    */
    public static void setCurrent(Bus bus) {
        current.set(bus);
        setDefaultBus(bus);
    }
    
    /**
     * Sends the event specified to the <code>Bus</code>.
     * @param event The <code>BusEvent</code> to send.
     * @throws BusException If there is an error sending event to <code>Bus</code>.
     */
    public abstract void sendEvent(BusEvent event) throws BusException;

    /**
     * Adds an event listener to the current <code>Bus</code>.
     * @param l The <code>BusEvenetListener</code> to be added.
     * @param filter A <code>BusEventFilter</code> to be applied to the listener.
     * @throws BusException If there is an error adding listener.
     */
    public abstract void addListener(BusEventListener l, BusEventFilter filter)
        throws BusException;

    /**
     * Removes the specified event listener from the <code>Bus</code>.
     * @param l The <code>BusEventListener</code> to be removed.
     * @throws BusException If there is an error removing the listener.
     */
    public abstract void removeListener(BusEventListener l) throws BusException;

    /**
     * Provides access to <code>BusEventCache</code> associated with the <code>Bus</code>.
     * @return BusEventCache The <code>BusEventCache</code> object.
     * @see BusEventCache
     */
    public abstract BusEventCache getEventCache();

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
     * Returns the <code>ResourceManager</code> of this <code>Bus</code>.
     * 
     * @return ResourceManager of this <code>Bus</code>.
     */
    public abstract ResourceManager getResourceManager();
    
    /**
     * Returns the <code> InstrumenatationManager </code> of this <code>Bus</code>
     * 
     * @return InstrumentationManager of this <code>Bus</code>
     */
    public abstract InstrumentationManager getInstrumentationManager();

    /**
     * Starts processing bus events, and returns only after the <code>Bus</code> has been shut down
     * (from another thread).
     *
     */
    public abstract void run();

    public abstract void initialize(String[] args,
            Map<String, Object> properties) throws BusException;


 
    static void clearDefault() { 
        defaultBus = null; 
    } 


    /**
     * Clear current for all threads.  For use in unit testing
     */
    static void clearCurrent() { 
        current.remove(); 
    } 
    /**
     * Initialise a default bus.
     */
    private static synchronized Bus getDefaultBus() { 
        try { 
            if (defaultBus == null) { 
                defaultBus = Bus.init(); 
            } 
            return defaultBus;
        } catch (BusException ex) { 
            throw new WebServiceException("unable to initialize default bus", ex);
        } 
    } 

    /** 
     * Set the default bus for all threads.  If no bus has been
     * already initialised, this bus will be used as the default bus
     * that do not explicitly initialise a bus.
     */
    private static synchronized void setDefaultBus(Bus bus) { 
        if (defaultBus == null) { 
            defaultBus = bus;
        } 
    } 

}
