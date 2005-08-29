package org.objectweb.celtix.bus;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.BusMessage;
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
    private ServerBinding serverBinding;
    private boolean published;
    private List<Handler> handlers;
    private List<Source> metadata;

    EndpointImpl(Bus b, Object impl, URI bindingId) throws BusException {
        bus = b;
        implementor = impl;
        configuration = null; // new EndpointConfiguration(Bus, this);
        serverBinding = createServerBinding(bindingId);
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
        publish(address);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.Endpoint#publish(java.lang.String)
     */
    public void publish(String a) {
        if (isPublished()) {
            logger.warning("Endpoint is already published");
        }

        URL address = null;
        try {
            address = new URL(a);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
        if (!serverBinding.isCompatibleWithAddress(address)) {
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
        EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(getWsdlLocation(),
                                                                                getServiceName(),     
                                                                                getPortName().getLocalPart());
        BindingManager bm = bus.getBindingManager();
        logger.info("Getting binding factory for namespace URI " + bindingId.toString());
        BindingFactory factory = bm.getBindingFactory(bindingId.toString()); 
        ServerBinding bindingImpl = factory.createServerBinding(ref);
        assert null != bindingImpl;
        return bindingImpl;

    }

    String getAddressFromContext(Object context) {
        return null;
    }

    boolean isContextBindingCompatible(Object context) {
        return true;
    }

    void doPublish(URL address) {
        // initialise ...
        published = true;
    }

    /*
     * boolean isProvider() { return this instanceof
     * javax.xml.ws.server.Provider; }
     */

}
