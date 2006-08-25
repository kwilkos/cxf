package org.objectweb.celtix.bus.jaxws;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.logging.LogUtils;

public class EndpointRegistry {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointRegistry.class);
    private Bus bus;
    private List<EndpointImpl> endpoints;

    public EndpointRegistry(Bus b) {
        bus = b;
        endpoints = new Vector<EndpointImpl>();
    }

    public void registerEndpoint(EndpointImpl ep) {
        assert ep.getBus() == bus;
        if (endpoints.contains(ep)) {
            LOG.warning("ENDPOINT_ALREADY_REGISTERED_MSG");
        } else {
            endpoints.add(ep);
        }
    }

    public void unregisterEndpoint(Endpoint ep) {
        if (ep.isPublished()) {
            LOG.warning("ENDPOINT_ACTIVE_MSG");
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
