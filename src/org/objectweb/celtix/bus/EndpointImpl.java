package org.objectweb.celtix.bus;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.BindingManager;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class EndpointImpl implements javax.xml.ws.Endpoint {

    private static Logger logger = Logger.getLogger(EndpointImpl.class.getName());

    private final Bus bus;
    private final Configuration configuration;
    private Object implementor;
    private EndpointReferenceType reference;
    private ServerBinding serverBinding;
    private boolean published;
    private List<Handler> handlers;
    private List<Source> metadata;
    private Executor executor;

    EndpointImpl(Bus b, Object impl, URI bindingId) throws BusException {
        bus = b;
        implementor = impl;
        configuration = null; // new EndpointConfiguration(Bus, this);
        reference = EndpointReferenceUtils.getEndpointReference(getWsdlLocation(), getServiceName(),
                                                                getPortName().getLocalPart());
        serverBinding = createServerBinding(bindingId);
        executor = bus.getWorkQueueManager().getAutomaticWorkQueue();
        assert null != executor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.Endpoint#getBinding()
     */
    public Binding getBinding() {
        return serverBinding.getBinding();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.Endpoint#getHandlerChain()
     */
    public List<Handler> getHandlerChain() {
        // create a copy of handler chain
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
     * (non-Javadoc) Not sure if this is meant to return the effective metadata -
     * or the default metadata previously assigned with
     * javax.xml.ws.Endpoint#setHandlerChain()
     * 
     * @see javax.xml.ws.Endpoint#getMetadata()
     */
    public List<Source> getMetadata() {
        return metadata;
    }

    /**
     * documented but not yet implemented in RI
     */

    public Executor getExecutor() {
        return executor;
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
            throw new IllegalArgumentException(new BusException("BINDING_INCOMPATIBLE_CONTEXT"));
        }

        // apply all changes to configuration and metadata and (re-)activate

        String address = getAddressFromContext(serverContext);
        publish(address);
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
        if (!serverBinding.isCompatibleWithAddress(address)) {
            throw new IllegalArgumentException(new BusException("BINDING_INCOMPATIBLE_ADDRESS"));
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

    /**
     * documented but not yet implemented in RI
     */
    public void setExecutor(Executor ex) {
        executor = ex;
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
    
    public ServerBinding getServerBinding() {
        return serverBinding;
    }
    
    public EndpointReferenceType getEndpointReferenceType() {
        return reference;
    }

    javax.jws.WebService getWebServiceAnnotation() {
        return (WebService)implementor.getClass().getAnnotation(WebService.class);
    }

    QName getServiceName() {
        javax.jws.WebService wsAnnotation = getWebServiceAnnotation();
        return new QName(wsAnnotation.targetNamespace(), wsAnnotation.serviceName());
    }

    QName getPortName() {
        javax.jws.WebService wsAnnotation = getWebServiceAnnotation();
        return new QName(wsAnnotation.targetNamespace(), wsAnnotation.endpointInterface());
    }

    URL getWsdlLocation() {
        javax.jws.WebService wsAnnotation = getWebServiceAnnotation();
        URL url = null;

        try {
            url = new URL(wsAnnotation.wsdlLocation());
        } catch (java.net.MalformedURLException mue) {
            logger.severe("Could not create URL from annotated wsdl location:\n" + mue);
        }

        return url;
    }

    ServerBinding createServerBinding(URI bindingId) throws BusException {
        BindingManager bm = bus.getBindingManager();
        BindingFactory factory = bm.getBindingFactory(bindingId.toString());
        ServerBinding bindingImpl = factory.createServerBinding(reference, this);
        assert null != bindingImpl;
        return bindingImpl;

    }

    String getAddressFromContext(Object context) {
        return null;
    }

    boolean isContextBindingCompatible(Object context) {
        return true;
    }

    void doPublish(String address) {
        EndpointReferenceUtils.setAddress(reference, address);
        try {
            serverBinding.activate();
            published = true;
        } catch (WSDLException ex) {
            logger.severe("Failed to publish endpoint - server binding could not be activated:\n"
                          + ex.getMessage());
        } catch (IOException ex) {
            logger.severe("Failed to publish endpoint - server binding could not be activated:\n"
                          + ex.getMessage());
        }
    }

    /*
     * boolean isProvider() { return this instanceof
     * javax.xml.ws.server.Provider; }
     */

}
