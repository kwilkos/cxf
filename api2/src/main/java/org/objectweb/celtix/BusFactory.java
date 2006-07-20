package org.objectweb.celtix;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Manages the <code>Bus</code> instances in a process.
 */
public final class BusFactory {

    private static final String DEFAULT_BUS_CLASSNAME = "org.objectweb.celtix.bus.busimpl.CeltixBus";
    private static BusFactory theInstance;
    
    private BusFactory() {
    }
    
    public static BusFactory getInstance() {
        synchronized (BusFactory.class) {
            if (null == theInstance) {
                theInstance = new BusFactory();
            }
        }
        return theInstance;
    }
    
    public Bus getBus(Map<String, Object> properties, 
                      ClassLoader classLoader) throws BusException {
        
        // check properties to determine bus class 
        
        String busClass = getBusClass(properties, classLoader);
        
        // create the bus
       
        return createBus(busClass, selectClassLoader(classLoader), properties);
    }
    
    private ClassLoader selectClassLoader(ClassLoader classLoader) { 
        ClassLoader ret = classLoader;
        if (null == classLoader) { 
            ret = BusFactory.class.getClassLoader();
        }
        return ret;
    } 

    private static Bus createBus(String className,
                                 ClassLoader classLoader,
                                 Map<String, Object> properties) throws BusException {

        Class<? extends Bus> busClass;
        try {
            busClass = Class.forName(className, true, classLoader).asSubclass(Bus.class);
            Bus bus = busClass.newInstance();
            bus.initialize(properties);
            return bus;
        } catch (Exception ex) {
            throw new BusException(ex);
        }
    }
    
    String getBusClass(Map<String, Object> properties, ClassLoader classLoader)
        throws BusException {
        
        String busClass = null;
    
        // first check properties    
        busClass = (String)properties.get(Bus.BUS_CLASS_PROPERTY);
        if (isValidBusClass(busClass)) {
            return busClass;
        }
        
        // next check system properties
        busClass = System.getProperty(Bus.BUS_CLASS_PROPERTY);
        if (isValidBusClass(busClass)) {
            return busClass;
        }
    
        try {
            // next, check for the services stuff in the jar file
            String serviceId = "META-INF/services/" + Bus.BUS_CLASS_PROPERTY;
            InputStream is = null;
        
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
        
            if (classLoader == null) {
                is = ClassLoader.getSystemResourceAsStream(serviceId);
            } else {
                is = classLoader.getResourceAsStream(serviceId);
            }
            if (is != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                busClass = rd.readLine();
                rd.close();
            }
            if (isValidBusClass(busClass)) {
                return busClass;
            }

            // otherwise use default  
            busClass = DEFAULT_BUS_CLASSNAME;
            return busClass;
        } catch (Exception ex) {
            throw new BusException(ex);
        }
    } 

    private boolean isValidBusClass(String busClassName) { 
        return busClassName != null && !"".equals(busClassName);
    }
 
}
