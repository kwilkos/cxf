package org.apache.cxf.management;

import java.util.List;

import javax.management.MBeanServer;

/** 
 *  InstrumentationManager interface for the instrumentations query, register 
 *  and unregister
 */
public interface InstrumentationManager {
    
    /**
     * register the instrumentation instance to the instrumentation manager      
     */
    void register(Instrumentation instrumentation);

    /**
     * unregister the instrumentation instance from the instrumentation manager  
     */
    void unregister(Object component);

    /**
     * get all instrumentation from the instrumentation manager
     * @return the instrumentation list 
     */
    List<Instrumentation> getAllInstrumentation();

    /**
     * provide a clean up method for instrumentation manager to stop
     */
    void shutdown();
    
    /**
     * get the MBeanServer which will host the cxf runtime component MBeans
     * NOTE: if the configuration is not set the JMXEnabled to be true, this method
     * will return null
     * @return the MBeanServer 
     */
    MBeanServer getMBeanServer();

}
