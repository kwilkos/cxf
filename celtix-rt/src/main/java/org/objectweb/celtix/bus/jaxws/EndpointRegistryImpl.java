package org.objectweb.celtix.bus.jaxws;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.busimpl.ComponentCreatedEvent;
import org.objectweb.celtix.bus.busimpl.ComponentRemovedEvent;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jaxws.EndpointRegistry;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class EndpointRegistryImpl implements EndpointRegistry {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointRegistryImpl.class);
    private final Bus bus;
    private final Map<EndpointReferenceType, EndpointImpl> endpoints;    

    public EndpointRegistryImpl(Bus b) {
        bus = b;
        endpoints = new Hashtable<EndpointReferenceType, EndpointImpl>();
    }

    public void registerEndpoint(Endpoint ep) {  
        assert ep instanceof EndpointImpl;
        EndpointImpl epl = (EndpointImpl) ep;
        EndpointReferenceType epr = epl.getEndpointReferenceType();
        assert epl.getBus() == bus;
        if (endpoints.containsKey(epr)) {
            LOG.warning("ENDPOINT_ALREADY_REGISTERED_MSG");
        } else {
            endpoints.put(epr, epl);
            if (bus != null) {               
                bus.sendEvent(new ComponentCreatedEvent(epl));
            }
        }
    }

    public void unregisterEndpoint(Endpoint ep) {
        
        assert ep instanceof EndpointImpl;
        EndpointImpl epl = (EndpointImpl) ep;
        EndpointReferenceType epr = epl.getEndpointReferenceType();
        if (ep.isPublished()) {
            LOG.warning("ENDPOINT_ACTIVE_MSG");
        }
        endpoints.remove(epr);
    }
    
    public void shutdown() {
        for (Endpoint ep : endpoints.values()) {
            if (ep.isPublished()) {
                ep.stop();
                bus.sendEvent(new ComponentRemovedEvent((EndpointImpl)ep));
            }
        }
        endpoints.clear();
    }

    public Collection<EndpointImpl> getEndpoints() {
        return endpoints.values();
    }
    
    public Endpoint getEndpoint(EndpointReferenceType epr) {
        return endpoints.get(epr);
    }


}
