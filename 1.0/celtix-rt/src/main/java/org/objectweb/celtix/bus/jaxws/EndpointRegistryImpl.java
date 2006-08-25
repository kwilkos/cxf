package org.objectweb.celtix.bus.jaxws;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.busimpl.ComponentCreatedEvent;
import org.objectweb.celtix.bus.busimpl.ComponentRemovedEvent;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jaxws.EndpointRegistry;

public class EndpointRegistryImpl implements EndpointRegistry {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointRegistryImpl.class);
    private final Bus bus;
    private final List<EndpointImpl> endpoints;    

    public EndpointRegistryImpl(Bus b) {
        bus = b;
        endpoints = new Vector<EndpointImpl>();
    }

    public void registerEndpoint(Endpoint ep) {  
        assert ep instanceof EndpointImpl;
        EndpointImpl epl = (EndpointImpl)ep;
        assert epl.getBus() == bus;
        if (endpoints.contains(epl)) {
            LOG.warning("ENDPOINT_ALREADY_REGISTERED_MSG");
        } else {
            endpoints.add(epl);
            if (bus != null) {               
                bus.sendEvent(new ComponentCreatedEvent(epl));
            }
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
                bus.sendEvent(new ComponentRemovedEvent((EndpointImpl)ep));
            }
        }
        endpoints.clear();
    }

    public List<EndpointImpl> getEndpoints() {
        return endpoints;
    }

}
