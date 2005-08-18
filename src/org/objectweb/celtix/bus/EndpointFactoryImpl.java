package org.objectweb.celtix.bus;

import java.net.URI;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;

public class EndpointFactoryImpl extends javax.xml.ws.EndpointFactory {

    /* (non-Javadoc)
     * @see javax.xml.ws.EndpointFactory#createEndpoint(java.net.URI, java.lang.Object)
     */
    @Override
    public Endpoint createEndpoint(URI bindingId, Object implementor) {     
        Endpoint ep = new EndpointImpl(Bus.getCurrent(), implementor, bindingId);
        return ep;
    }

    /* (non-Javadoc)
     * @see javax.xml.ws.EndpointFactory#publish(java.lang.String, java.lang.Object)
     */
    @Override
    public Endpoint publish(String address, Object implementor) {
        URI bindingId = getDefaultBindingId(address);
        Endpoint ep = new EndpointImpl(Bus.getCurrent(), implementor, bindingId);
        ep.publish(address);
        return null;
    }
    
    URI getDefaultBindingId(String address) {
        return null;
    }

}
