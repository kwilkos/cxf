package org.objectweb.celtix.bus.transports.http;

import java.io.IOException;

import javax.wsdl.WSDLException;
import javax.xml.bind.JAXBException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.transports.http.configuration.HTTPClientPolicy;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;
import org.objectweb.celtix.wsdl.JAXBExtensionHelper;

public class HTTPTransportFactory implements TransportFactory {
    Bus bus;
    
    public void init(Bus b) {
        bus = b;
        try {
            JAXBExtensionHelper.addExtensions(bus.getWSDLManager().getExtenstionRegistry(),
                                              javax.wsdl.Port.class,
                                              HTTPClientPolicy.class);
            JAXBExtensionHelper.addExtensions(bus.getWSDLManager().getExtenstionRegistry(),
                                              javax.wsdl.Port.class,
                                              HTTPServerPolicy.class);
        } catch (JAXBException e) {
            //ignore, we can continue without the extension registered
        }
    }

    public ServerTransport createServerTransport(EndpointReferenceType address) 
        throws WSDLException, IOException {
        return new HTTPServerTransport(bus, address);
    }

    public ServerTransport createTransientServerTransport(EndpointReferenceType address)
        throws WSDLException {
        
        // TODO Auto-generated method stub
        return null;
    }

    public ClientTransport createClientTransport(EndpointReferenceType address)
        throws WSDLException, IOException {
        
        return new HTTPClientTransport(bus, address);
    }

}
