package org.apache.cxf.management;


/**
 * Basic interface for create a Instrumentation instance.
 * 
 */
public interface InstrumentationFactory {

    /**
     * create the Instrumentation instance 
     * which implement instrumentation interface
     * @return the instrumentation instance
     */
    Instrumentation createInstrumentation();    
}
