package org.objectweb.celtix.transports.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.ConduitInitiator;
import org.objectweb.celtix.messaging.Destination;
import org.objectweb.celtix.messaging.DestinationFactory;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

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
