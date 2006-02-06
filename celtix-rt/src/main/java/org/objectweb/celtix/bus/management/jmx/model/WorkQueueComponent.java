package org.objectweb.celtix.bus.management.jmx.model;


import org.objectweb.celtix.bus.management.jmx.JMXManagedComponent;
import org.objectweb.celtix.bus.workqueue.WorkQueueInstrumentation;
import org.objectweb.celtix.workqueue.WorkQueueManager.ThreadingModel;

public class WorkQueueComponent 
    extends JMXManagedComponent 
    implements WorkQueueComponentMBean {
       
    WorkQueueInstrumentation wqInstrumentation;
    
    public WorkQueueComponent(WorkQueueInstrumentation wqi) {
        wqInstrumentation = wqi;
        objectName = getObjectName(wqi.getUniqueInstrumentationName());      
    }
   
       
    public void shutdown(boolean processRemainingWorkItems) {
        wqInstrumentation.shutdown(processRemainingWorkItems);
        
    }

    public String getThreadingModel() {
        return wqInstrumentation.getThreadingModel().toString();
        
    }

    public void setThreadingModel(String model) {
        if (model.compareTo("SINGLE_THREADED") == 0) {
            wqInstrumentation.setThreadingModel(ThreadingModel.SINGLE_THREADED);
        }
        if (model.compareTo("MULTI_THREADED") == 0) {
            wqInstrumentation.setThreadingModel(ThreadingModel.MULTI_THREADED);
        }
    }  

}
