package org.objectweb.celtix;

import java.util.Map;

import org.objectweb.celtix.configuration.Configuration;

public abstract class Bus {
    
    private static ThreadLocal<Bus> current;

    /**
     * Returns a newly created and fully initialised <code>Bus</code>
     * or an existing bus if one with the same id already exists.
     * 
     * @param args any args, such as bus identifier, bus class, and other configuration
     * options that can be used to identify and initialize this <code>Bus</code>.
     * @return Bus the newly created or existing <code<Bus</code>.
     * @throws BusException If there is an error initializing <code>Bus</code>.
     */
    public static synchronized Bus init(String[] args) throws BusException {
        return init(args, null);
    }
    
    /**
     * Returns a newly created and fully initialised <code>Bus</code>
     * or an existing bus if one with the same id already exists.
     * 
     * @param args any args, such as bus identifier, bus class, and other configuration
     * options that can be used to identify and initialize this <code>Bus</code>.
     * @param properties any properties, such as bus identifier, bus class, and other configuration
     * options that can be used to identify and initialize this <code>Bus</code>. 
     * The properties are superceded by the settings in the <code>args</code> parameter,
     * and they in turn supercede system properties.
     * @return Bus the newly created or existing <code<Bus</code>.
     * @throws BusException If there is an error initializing <code>Bus</code>.
     */
    public static synchronized Bus init(String[] args, Map<String, Object> properties) throws BusException {
        return init(args, null, null);
    }
    
    /**
     * Returns a newly created and fully initialised <code>Bus</code>
     * or an existing bus if one with the same id already exists.
     * 
     * @param args any args, such as bus identifier, bus class, and other configuration
     * options that can be used to identify and initialize this <code>Bus</code>.
     * @param properties any properties, such as bus identifier, bus class, and other configuration
     * options that can be used to identify and initialize this <code>Bus</code>. 
     * The properties are superceded by the settings in the <code>args</code> parameter,
     * and they in turn supercede system properties.
     * @param classLoader an optional classloader to use when instantiating a <code>Bus</code>
     * needs to be instantiated (defaults to the current thread's context classloader).
     * @return Bus the newly created or existing <code<Bus</code>.
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

    public abstract void shutdown(boolean wait) throws BusException;
    public abstract Configuration getConfiguration();
    public abstract Object getHandlerRegistry();
    public abstract Object getTransportRegistry();
    public abstract Object getServantRegistry();
    public abstract Object getClientRegistry();
    
    protected abstract void initialize(String id, 
            String[] args,
            Map<String, Object> properties) throws BusException;


}
