package org.objectweb.celtix.bus.management.jmx.model;

public interface WorkQueueComponentMBean {

    void shutdown(boolean processRemainingWorkItems);

    String getThreadingModel();

    void setThreadingModel(String model);
   
}
