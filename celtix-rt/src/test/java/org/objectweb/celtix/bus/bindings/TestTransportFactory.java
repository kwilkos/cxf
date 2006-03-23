package org.objectweb.celtix.bus.bindings;

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.ResponseCallback;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

class TestTransportFactory implements TransportFactory {

    public ClientTransport createClientTransport(EndpointReferenceType address) 
        throws WSDLException, IOException {
        return new TestClientTransport();
    }

    public ServerTransport createServerTransport(EndpointReferenceType address) throws WSDLException,
        IOException {
        return new TestServerTransport();
    }

    public ServerTransport createTransientServerTransport(EndpointReferenceType address)
        throws WSDLException, IOException {
        return null;
    }

    public void init(Bus b) {
    }
    
    /**
     * @param callback used to report (potentially asynchronous) responses.
     */
    public synchronized void setResponseCallback(ResponseCallback callback) {
    }

    public void shutdown() {
       //do nothing  
    }
}
