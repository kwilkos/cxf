package org.objectweb.celtix.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;

public class EndpointRegistry {

    private static Logger logger = Logger.getLogger(EndpointRegistry.class.getName());
    private Bus bus;
    private List<EndpointImpl> endpoints;

    EndpointRegistry(Bus b) {
        bus = b;
        endpoints = new ArrayList<EndpointImpl>();
    }

    public void registerEndpoint(EndpointImpl ep) {
        assert ep.getBus() == bus;
        if (endpoints.contains(ep)) {
            logger.warning("Endpoint is already registered");
        } else {
            endpoints.add(ep);
        }
    }

    public void unregisterEndpoint(Endpoint ep) {
        if (ep.isPublished()) {
            logger.warning("Can't unregister active endpoint");
        }
        endpoints.remove(ep);
    }
    
    public void shutdown() {
        for (Endpoint ep : endpoints) {
            if (ep.isPublished()) {
                ep.stop();
            }
        }
        endpoints.clear();
    }
}
