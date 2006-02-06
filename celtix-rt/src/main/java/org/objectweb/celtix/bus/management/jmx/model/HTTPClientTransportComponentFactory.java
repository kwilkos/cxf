package org.objectweb.celtix.bus.management.jmx.model;

import org.objectweb.celtix.bus.management.jmx.JMXManagedComponent;
import org.objectweb.celtix.bus.management.jmx.JMXManagedComponentFactory;
import org.objectweb.celtix.bus.transports.http.HTTPClientTransportInstrumentation;
import org.objectweb.celtix.management.Instrumentation;

public class HTTPClientTransportComponentFactory implements JMXManagedComponentFactory {

    public JMXManagedComponent createManagedComponent(Instrumentation i) {
        HTTPClientTransportInstrumentation hcti = (HTTPClientTransportInstrumentation)i;        
        return new HTTPClientTransportComponent(hcti);
    }    

}
