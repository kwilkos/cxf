package org.objectweb.celtix.bus.workqueue;


import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedOperation;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.workqueue.WorkQueueManager.ThreadingModel;

@ManagedResource(componentName = "WorkQueue", 
                 description = "The Celtix bus internal thread pool for manangement ", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate", persistPeriod = 200)
                 
public class WorkQueueInstrumentation implements Instrumentation {    
    private static final String INSTRUMENTED_NAME = "Bus.WorkQueue";
    
    private String objectName;
    private WorkQueueManagerImpl wqManager;
    private AutomaticWorkQueueImpl aWorkQueue;
    
    public WorkQueueInstrumentation(WorkQueueManagerImpl wq) {
        wqManager = wq;        
        objectName = ",name=WorkQueue";
        if (wqManager.autoQueue != null 
            && AutomaticWorkQueueImpl.class.isAssignableFrom(wqManager.autoQueue.getClass())) {
            aWorkQueue = (AutomaticWorkQueueImpl) wqManager.autoQueue;
        }
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
   
    @ManagedAttribute(description = "The WorkQueueMaxSize",
                      persistPolicy = "OnUpdate")
    public long getWorkQueueMaxSize() {
        return aWorkQueue.getMaxSize();
    }
   
    @ManagedAttribute(description = "The WorkQueue Current size",
                      persistPolicy = "OnUpdate")
    public long getWorkQueueSize() {
        return aWorkQueue.getSize();
    }

    @ManagedAttribute(description = "The WorkQueue has nothing to do",
                      persistPolicy = "OnUpdate")
    public boolean isEmpty() {
        return aWorkQueue.isEmpty();
    }

    @ManagedAttribute(description = "The WorkQueue is very busy")
    public boolean isFull() {
        return aWorkQueue.isFull();
    }

    @ManagedAttribute(description = "The WorkQueue HighWaterMark",
                      persistPolicy = "OnUpdate")
    public int getHighWaterMark() {
        return aWorkQueue.getHighWaterMark();
    }
    public void setHighWaterMark(int hwm) {
        aWorkQueue.setHighWaterMark(hwm);
    }

    @ManagedAttribute(description = "The WorkQueue LowWaterMark",
                      persistPolicy = "OnUpdate")
    public int getLowWaterMark() {
        return aWorkQueue.getLowWaterMark();
    }

    public void setLowWaterMark(int lwm) {
        aWorkQueue.setLowWaterMark(lwm);
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
