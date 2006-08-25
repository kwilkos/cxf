package org.objectweb.celtix.management;

import java.util.List;

/** 
 *  InstrumentationManager interface for the instrumentations query, register 
 *  and unregister
 */
public interface InstrumentationManager {
    
    /**
     * regist the instrumentation instance to the instrumentation manager      
     */
    void regist(Instrumentation instrumentation);

    /**
     * unregist the instrumentation instance from the instrumentation manager  
     */
    void unregist(Object component);

    /**
     * get all instrumentation from the instrumentation manager
     * @retrun the instrumentation list 
     */
    List<Instrumentation> getAllInstrumentation();

    /**
     * provide a clean up method for instrumentation manager to stop
     */
    void shutdown();

}
