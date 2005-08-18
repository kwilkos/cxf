package org.objectweb.celtix.bus;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusMessage;
import org.objectweb.celtix.configuration.Configuration;

public class EndpointImpl implements javax.xml.ws.Endpoint {

    private static Logger logger = Logger.getLogger(EndpointImpl.class.getName());

    Bus bus;
    Configuration configuration;
    Object implementor;
    Binding binding;
    boolean published;
    List<Handler> handlers;
    List<Source> metadata;
    
    EndpointImpl(Bus b, Object impl, URI bindingId) {
        bus = b;
        implementor = impl;
        initializeBinding(bindingId);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.Endpoint#getBinding()
     */
    public Binding getBinding() {
        return binding;
    }

    /*
     * (non-Javadoc)
     * Not sure if this is meant to return the effective handler chain - or the default handler chain
     * previously assigned with javax.xml.ws.Endpoint#setHandlerChain()
     * 
     * @see javax.xml.ws.Endpoint#getHandlerChain()
     */
    public List<Handler> getHandlerChain() {
        if (null != handlers) {
            return handlers;
        }
        // determine handler chain based on WSDL and other configuration
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.Endpoint#getImplementor()
     */
    public Object getImplementor() {
        return implementor;
    }

    /*
     * (non-Javadoc)
     * Not sure if this is meant to return the effective metadata - or the default metadata
     * previously assigned with javax.xml.ws.Endpoint#setHandlerChain()
     * 
     * @see javax.xml.ws.Endpoint#getMetadata()
     */
    public List<Source> getMetadata() {
        return metadata;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.Endpoint#isPublished()
     */
    public boolean isPublished() {
        return published;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.Endpoint#publish(java.lang.Object)
     */
    public void publish(Object serverContext) {
        if (isPublished()) {
            logger.warning("Endpoint is already published");
        }
        if (!isContextBindingCompatible(serverContext)) {
            throw new IllegalArgumentException(new BusMessage("BINDING_INCOMPATIBLE_CONTEXT").toString());
        }

        // apply all changes to configuration and metadata and (re-)activate

        String address = getAddressFromContext(serverContext);
        doPublish(address);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.Endpoint#publish(java.lang.String)
     */
    public void publish(String address) {
        if (isPublished()) {
            logger.warning("Endpoint is already published");
        }

        if (!isAddressBindingCompatible(address)) {
            throw new IllegalArgumentException(new BusMessage("BINDING_INCOMPATIBLE_ADDRESS").toString());
        }

        doPublish(address);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.Endpoint#setHandlerChain(java.util.List)
     */
    public void setHandlerChain(List<Handler> h) {
        handlers = h;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.Endpoint#setMetadata(java.util.List)
     */
    public void setMetadata(List<Source> m) {
        metadata = m;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.Endpoint#stop()
     */
    public void stop() {
        if (!isPublished()) {
            logger.warning("Endpoint is not active.");
        }
        published = false;
    }
    
    public Bus getBus() {
        return bus;
    }

    void initializeBinding(URI bindingId) {

    }

    String getAddressFromContext(Object context) {
        return null;
    }

    boolean isAddressBindingCompatible(String address) {
        return true;
    }

    boolean isContextBindingCompatible(Object context) {
        return true;
    }
    
    void doPublish(String address) {
        // initialise ...
        published = true;
    }
    
    /*
    boolean isProvider() {
        return this instanceof javax.xml.ws.server.Provider;
    }
    */

}
