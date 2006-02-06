package org.objectweb.celtix.management;

import java.util.List;

/** 
 *  InstrumentationManager interface for the instrumentations query, register 
 *  and unregister
 */
public interface InstrumentationManager {
   
    void regist(Instrumentation instrumentation);

    void unregist(Object component);

    List<Instrumentation> getAllInstrumentation();

}
