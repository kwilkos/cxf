package org.objectweb.celtix.bus.workqueue;


import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedOperation;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.workqueue.WorkQueueManager.ThreadingModel;

@ManagedResource(objectName = "WorkQueue", 
                 description = "The Celtix bus internal thread pool for manangement ", 
                 log = true,
                 logFile = "jmx.log", currencyTimeLimit = 15, persistPolicy = "OnUpdate", persistPeriod = 200,
                 persistLocation = "./persist", persistName = "WorkQueue.jmx")
                 
public class WorkQueueInstrumentation implements Instrumentation {    
    private static final String INSTRUMENTED_NAME = "WorkQueue";
    
    private static int instanceNumber;
    
    private String objectName;
    private WorkQueueManagerImpl wqManager;
    
    public WorkQueueInstrumentation(WorkQueueManagerImpl wq) {
        wqManager = wq;        
        objectName = INSTRUMENTED_NAME + instanceNumber;       
        instanceNumber++;
    }
    
    public static void resetInstanceNumber() {
        instanceNumber = 0;
    }
    
    @ManagedOperation(currencyTimeLimit = 30)
    public void shutdown(boolean processRemainingWorkItems) {
        wqManager.shutdown(processRemainingWorkItems); 
    }
    
    @ManagedAttribute(description = "The thread pool work model",                      
                      defaultValue = "SINGLE_THREADED",
                      persistPolicy = "OnUpdate")
                      
    public String getThreadingModel() {
        
        return wqManager.getThreadingModel().toString();
    }

    public void setThreadingModel(String model) {
        if (model.compareTo("SINGLE_THREADED") == 0) {
            wqManager.setThreadingModel(ThreadingModel.SINGLE_THREADED);
        }
        if (model.compareTo("MULTI_THREADED") == 0) {
            wqManager.setThreadingModel(ThreadingModel.MULTI_THREADED);
        }             
    }
   
    public Object getComponent() {        
        return wqManager;
    }

    public String getInstrumentationName() {        
        return INSTRUMENTED_NAME;
    }

    public String getUniqueInstrumentationName() {       
        return objectName;
    }   

}
