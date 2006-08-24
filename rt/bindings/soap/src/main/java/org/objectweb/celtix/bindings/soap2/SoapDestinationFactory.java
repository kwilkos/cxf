package org.apache.cxf.bindings.soap2;

import java.io.IOException;

import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;

import org.apache.cxf.BusException;
import org.apache.cxf.bindings.soap2.model.SoapBindingInfo;
import org.apache.cxf.messaging.Destination;
import org.apache.cxf.messaging.DestinationFactory;
import org.apache.cxf.messaging.DestinationFactoryManager;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class SoapDestinationFactory implements DestinationFactory {
    private DestinationFactoryManager destinationFactoryManager;
    
    public SoapDestinationFactory(DestinationFactoryManager destinationFactoyrManager) {
        super();
        this.destinationFactoryManager = destinationFactoyrManager;
    }

    public Destination getDestination(EndpointInfo ei) throws IOException {
        EndpointReferenceType epr = new EndpointReferenceType();
        AttributedURIType uri = new AttributedURIType();
        
        // TODO: make non wsdl4j specific
        SOAPAddress add = ei.getExtensor(SOAPAddress.class);
        uri.setValue(add.getLocationURI());
        epr.setAddress(uri);
        
        SoapBindingInfo binding = (SoapBindingInfo) ei.getBinding();
        DestinationFactory destinationFactory;
        try {
            destinationFactory = destinationFactoryManager.getDestinationFactory(binding.getTransportURI());
            
            return destinationFactory.getDestination(ei);
        } catch (BusException e) {
            throw new RuntimeException("Could not find destination factory for transport "
                                       + binding.getTransportURI());
        }
    }

    public Destination getDestination(EndpointReferenceType reference) throws WSDLException, IOException {
        // TODO How do we get actual destination factory??
        throw new UnsupportedOperationException();
    }

    public DestinationFactoryManager getDestinationFactoryManager() {
        return destinationFactoryManager;
    }

    public void setDestinationFactoryManager(DestinationFactoryManager destinationFactoryManager) {
        this.destinationFactoryManager = destinationFactoryManager;
    }
}
