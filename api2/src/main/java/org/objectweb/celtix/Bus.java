package org.objectweb.celtix;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.bindings.BindingFactoryManager;
// import org.objectweb.celtix.buslifecycle.BusLifeCycleManager;
import org.objectweb.celtix.configuration.Configuration;
// import org.objectweb.celtix.jaxws.EndpointRegistry;
// import org.objectweb.celtix.plugins.PluginManager;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.resource.ResourceManager;
// import org.objectweb.celtix.transports.TransportFactoryManager;
// import org.objectweb.celtix.workqueue.WorkQueueManager;
// import org.objectweb.celtix.wsdl.WSDLManager;

public abstract class Bus {
    
    public static final String BUS_CLASS_PROPERTY = "org.objectweb.celtix.BusClass";

    private static ThreadLocal<Bus> current = new ThreadLocal<Bus>();
    private static Map<String, WeakReference<Bus>> nameMap = 
        new ConcurrentHashMap<String, WeakReference<Bus>>();
    private static Bus defaultBus; 
    
    /**
     * Returns a newly created and fully initialised <code>Bus</code>.
     * 
     * @return Bus the newly created <code>Bus</code>.
     * @throws BusException If there is an error initializing <code>Bus</code>.
     */
    public static synchronized Bus init() throws BusException {
        return init(new HashMap<String, Object>());
    }
    
    /**
     * Returns a newly created and fully initialised <code>Bus</code>.
     * 
     * @param properties any properties, such as bus identifier, bus class, and other configuration
     * options that can be used to identify and initialize this <code>Bus</code>. 
     * These properties supercede corresponding system properties.
     * @return Bus the newly created <code>Bus</code>.
     * @throws BusException If there is an error initializing <code>Bus</code>.
     */
    public static synchronized Bus init(Map<String, Object> properties) throws BusException {
        return init(properties, null);
    }
    
    /**
     * Returns a newly created and fully initialised <code>Bus</code>.
     * 
     * @param properties any properties, such as domain name, bus class, and other configuration
     * options that can be used to initialize this <code>Bus</code>. 
     * These properties supercede corresponding system properties.
     * @param classLoader an optional classloader to use when instantiating a <code>Bus</code>
     * needs to be instantiated (defaults to the current thread's context classloader).
     * @return Bus the newly created <code>Bus</code>.
     * @throws BusException If there is an error initializing <code>Bus</code>.
     */
    public static synchronized Bus init(
        Map<String, Object> properties, 
        ClassLoader classLoader) throws BusException {
        
        // delegate to the factory 
        BusFactory bf = BusFactory.getInstance();
        Bus b = bf.getBus(properties, classLoader);
        nameMap.put(b.getBusID(), new WeakReference<Bus>(b));
        return b;
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
     * Returns the LAST Bus that was created with the given ID.  If 
     * multiple buses are created with the same ID, only the last is
     * saved for access later.
     * 
     * The Bus objects are only held via a WeakReference.   Thus, if
     * something else doesn't hold onto it, it will be garbage collected 
     * and this method will return null.
     * 
     * @param id
     * @return The last bus by the given ID.
     */
    public static Bus getByID(String id) {
        WeakReference<Bus> bus = nameMap.get(id);
        if (bus != null) {
            if (bus.get() == null) {
                nameMap.remove(id);
            }
            return bus.get();
        }
        return null;
    }
    
    protected void removeByID(String id) {
        if (nameMap.containsKey(id)) {
            nameMap.remove(id);
        }
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
     * Returns the configuration of this <code>Bus</code>.
     * 
     * @return Configuration the configuration of this <code>bus</code>.
     */
    public abstract Configuration getConfiguration();

    /** 
     * Returns the <code>TransportFactoryManager</code> of this <code>Bus</code>.
     * 
     * @return TransportRegistry the servant registry of this <code>Bus</code>.
     */
    // public abstract TransportFactoryManager getTransportFactoryManager();

    /** 
     * Returns the BindingFactoryManager of this <code>Bus</code>.
     * 
     * @return the BindingFactoryManager of this <code>Bus</code>.
     */
    public abstract BindingFactoryManager getBindingManager();    
    
    /**
     * Returns the list of in phases for this bus.
     * @return the list of in phases for this bus
     */
    public abstract List<Phase> getInPhases(); 
    
    /**
     * Returns the list of out phases for this bus.
     * @return the list of out phases for this bus
     */
    public abstract List<Phase> getOutPhases();

    /** 
     * Returns the <code>ClientRegistry</code> of this <code>Bus</code>.
     * 
     * @return WSDLManager the wsdl manager of this <code>Bus</code>.
     */
    // public abstract WSDLManager getWSDLManager();

    /** 
     * Returns the <code>BusLifeCycleManager</code> of this <code>Bus</code>.
     * 
     * @return BusLifeCycleManager of this <code>Bus</code>.
     */
    // public abstract BusLifeCycleManager getLifeCycleManager();

    /** 
     * Returns the <code>WorkQueueManager</code> of this <code>Bus</code>.
     * 
     * @return WorkQueueManager of this <code>Bus</code>.
     */
    // public abstract WorkQueueManager getWorkQueueManager();
    

    /** 
     * Returns the <code>ResourceManager</code> of this <code>Bus</code>.
     * 
     * @return ResourceManager of this <code>Bus</code>.
     */
    public abstract ResourceManager getResourceManager();
    
    /**
     * Returns the BusID  of this <code>Bus</code>
     * 
     * @return String BusID of this <code>Bus</code>
     */
    public abstract String getBusID();
    

    /**
     * Starts processing bus events, and returns only after the <code>Bus</code> has been shut down
     * (from another thread).
     *
     */
    public abstract void run();
    

    public abstract void initialize(
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
