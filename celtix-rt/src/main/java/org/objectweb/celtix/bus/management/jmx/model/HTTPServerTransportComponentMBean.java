package org.objectweb.celtix.bus.management.jmx.model;

public interface HTTPServerTransportComponentMBean {
    
    String getUrl();
    // get the policy object information 
    String getContentEncoding();
    Boolean getHonorKeepAlive();
    Long getReceiveTimeout();

}
