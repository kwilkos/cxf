package org.objectweb.celtix.management;


/**
 * Basic interface for representing a Instrumented object.
 * 
 */
public interface Instrumentation {

    /**
     * get the Instrumentation Name, this name is base on class 
     * which implement instrumentation interface
     * @return the instrumentation name      
     */
    String getInstrumentationName();    
    
    /**
     * get the instrumentation managed component  
     * @return the Component object reference 
     */
    Object getComponent();
    
    /**
     * get the unique Instrumentation Name, this name is base on class instance
     * which implement instrumentation interface
     * @return the instrumentation name and instance number  
     */
    String getUniqueInstrumentationName();
       
}
