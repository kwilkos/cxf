package org.objectweb.celtix.bus.management.jmx.model;


import org.objectweb.celtix.bus.management.jmx.JMXManagedComponent;
import org.objectweb.celtix.bus.transports.http.HTTPClientTransportInstrumentation;

public class HTTPClientTransportComponent 
    extends JMXManagedComponent 
    implements HTTPClientTransportComponentMBean {

    HTTPClientTransportInstrumentation hcTransportInstrumentation; 
    
    public HTTPClientTransportComponent(HTTPClientTransportInstrumentation hcti) {
        hcTransportInstrumentation = hcti;        
        objectName = getObjectName(hcti.getUniqueInstrumentationName());
        
    }
    
    public String getUrl() {        
        return hcTransportInstrumentation.getUrl();
    }

    public String getProxyUserName() {        
        return hcTransportInstrumentation.getProxyAuthPolicy().getUserName();
    }

    public String getUserName() {        
        return hcTransportInstrumentation.getAuthPolicy().getUserName();
    }

    public String getProxyAuthorizationType() {
        return hcTransportInstrumentation.getProxyAuthPolicy().getAuthorizationType();
    }

}
