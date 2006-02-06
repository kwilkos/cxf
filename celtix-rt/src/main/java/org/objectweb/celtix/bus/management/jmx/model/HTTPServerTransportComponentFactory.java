package org.objectweb.celtix.bus.management.jmx.model;

import org.objectweb.celtix.bus.management.jmx.JMXManagedComponent;
import org.objectweb.celtix.bus.management.jmx.JMXManagedComponentFactory;
import org.objectweb.celtix.bus.transports.http.HTTPServerTransportInstrumentation;
import org.objectweb.celtix.management.Instrumentation;

public class HTTPServerTransportComponentFactory 
    implements JMXManagedComponentFactory {

    public JMXManagedComponent createManagedComponent(Instrumentation i) {
        HTTPServerTransportInstrumentation hsti = (HTTPServerTransportInstrumentation) i;
        return new HTTPServerTransportComponent(hsti);
    }
    

}
