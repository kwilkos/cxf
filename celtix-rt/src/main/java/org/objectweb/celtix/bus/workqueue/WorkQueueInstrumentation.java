package org.objectweb.celtix.bus.workqueue;


import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.workqueue.WorkQueueManager.ThreadingModel;

public class WorkQueueInstrumentation implements Instrumentation {    
    private static int instanceNumber;
    private static String iName = "WorkQueue";
    private String objectName;
    private WorkQueueManagerImpl wqManager;
    
    
    public WorkQueueInstrumentation(WorkQueueManagerImpl wq) {
        wqManager = wq;        
        objectName = iName + instanceNumber;       
        instanceNumber++;
    }
    
    public int getInstanceNumber() {        
        return 0;
    } 
        
    public void shutdown(boolean processRemainingWorkItems) {
        wqManager.shutdown(processRemainingWorkItems); 
    }

    public ThreadingModel getThreadingModel() {        
        return wqManager.getThreadingModel();
    }

    public void setThreadingModel(ThreadingModel model) {
        wqManager.setThreadingModel(model);        
    }
   
    public Object getComponent() {        
        return wqManager;
    }

    public String getInstrumentationName() {        
        return iName;
    }

    public String getUniqueInstrumentationName() {       
        return objectName;
    }   

}
