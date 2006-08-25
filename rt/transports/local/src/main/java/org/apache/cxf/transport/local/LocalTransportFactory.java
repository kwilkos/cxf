package org.apache.cxf.transport.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class LocalTransportFactory implements DestinationFactory, ConduitInitiator {
   
    public static final String TRANSPORT_ID = "http://cxf.apache.org/local-transport";
    
    private static final Logger LOG = Logger.getLogger(LocalTransportFactory.class.getName());
    
    private Map<String, Destination> destinations = new HashMap<String, Destination>();

    
    public Destination getDestination(EndpointInfo ei) throws IOException {
        return getDestination(createReference(ei));
    }

    public Destination getDestination(EndpointReferenceType reference) throws IOException {
        Destination d = destinations.get(reference.getAddress().getValue());
        if (d == null) {
            d = createDestination(reference);
            destinations.put(reference.getAddress().getValue(), d);
        }
        return d;
    }

    private Destination createDestination(EndpointReferenceType reference) {
        LOG.info("Creating destination for address " + reference.getAddress().getValue());
        return new LocalDestination(this, reference);
    }

    void remove(LocalDestination destination) {
        destinations.remove(destination);
    }

    public Conduit getConduit(EndpointInfo ei) throws IOException {
        return new LocalConduit((LocalDestination)getDestination(createReference(ei)));
    }

    public Conduit getConduit(EndpointInfo ei, EndpointReferenceType target) throws IOException {
        return new LocalConduit((LocalDestination)getDestination(target));
    }

    EndpointReferenceType createReference(EndpointInfo ei) {
        EndpointReferenceType epr = new EndpointReferenceType();
        AttributedURIType address = new AttributedURIType();
        address.setValue(ei.getAddress());
        epr.setAddress(address);
        return epr;
    }

}
