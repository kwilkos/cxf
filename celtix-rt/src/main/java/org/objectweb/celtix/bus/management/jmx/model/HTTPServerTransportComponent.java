package org.objectweb.celtix.bus.management.jmx.model;


import org.objectweb.celtix.bus.management.jmx.JMXManagedComponent;
import org.objectweb.celtix.bus.transports.http.HTTPServerTransportInstrumentation;


public class HTTPServerTransportComponent 
    extends JMXManagedComponent 
    implements HTTPServerTransportComponentMBean {

    private HTTPServerTransportInstrumentation hsTransportInstrumentation;
    
    public HTTPServerTransportComponent(HTTPServerTransportInstrumentation hsti) {
        hsTransportInstrumentation = hsti;
        objectName = getObjectName(hsti.getUniqueInstrumentationName());
        
    }
    
    public String getUrl() {       
        return hsTransportInstrumentation.getUrl();
    }

    public String getContentEncoding() {        
        return hsTransportInstrumentation.getHTTPServerPolicy().getContentEncoding();
    }

    public Boolean getHonorKeepAlive() {       
        return hsTransportInstrumentation.getHTTPServerPolicy().isHonorKeepAlive();
    }

    public Long getReceiveTimeout() {        
        return hsTransportInstrumentation.getHTTPServerPolicy().getReceiveTimeout();
    }

}
