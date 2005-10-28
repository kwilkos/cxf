package org.objectweb.celtix.bus.jaxws;





import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.bus.context.WebServiceContextImpl;
import org.objectweb.celtix.bus.handlers.HandlerChainBuilder;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.injection.ResourceInjector;
import org.objectweb.celtix.common.injection.ResourceResolver;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class EndpointImpl extends javax.xml.ws.Endpoint {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointImpl.class);

    private final Bus bus;
    //private final Configuration configuration;
    
    private Object implementor;
    private EndpointReferenceType reference;
    private ServerBinding serverBinding;
    private boolean published;
    private List<Source> metadata;
    private Executor executor;


    public EndpointImpl(Bus b, Object impl, String bindingId)
        throws BusException, WSDLException, IOException {

        bus = b;
        implementor = impl;
        // configuration = new EndpointConfiguration(Bus, this);
        reference = EndpointReferenceUtils.getEndpointReference(bus.getWSDLManager(), implementor);
        if (null == bindingId) {
            try {
                Port port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), reference);
                ExtensibilityElement el = (ExtensibilityElement)port.getExtensibilityElements().get(0);
                bindingId = el.getElementType().getNamespaceURI();
            } catch (WSDLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        serverBinding = createServerBinding(bindingId);
        configureHandlers(); 
        injectResources();
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
            LOG.warning("ENDPOINT_ALREADY_PUBLISHED_MSG");
        }
        if (!isContextBindingCompatible(serverContext)) {
            throw new IllegalArgumentException(
                new BusException(new Message("BINDING_INCOMPATIBLE_CONTEXT_EXC", LOG)));
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
            LOG.warning("ENDPOINT_ALREADY_PUBLISHED_MSG");
        }
        if (!serverBinding.isCompatibleWithAddress(address)) {
            throw new IllegalArgumentException(
                new BusException(new Message("BINDING_INCOMPATIBLE_ADDRESS_EXC", LOG)));
        }
        doPublish(address);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.Endpoint#setHandlerChain(java.util.List)
     */
    public void setHandlerChain(List<Handler> h) {        
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
            LOG.warning("ENDPOINT_INACTIVE_MSG");
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
    ServerBinding createServerBinding(String bindingId) throws BusException, WSDLException, IOException {

        BindingFactory factory = bus.getBindingManager().getBindingFactory(bindingId);
        if (null == factory) {
            throw new BusException(new Message("BINDING_FACTORY_MISSING_EXC", LOG, bindingId));
        }
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
            LOG.log(Level.SEVERE, "SERVER_BINDING_ACTIVATION_FAILURE_MSG", ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "SERVER_BINDING_ACTIVATION_FAILURE_MSG", ex);
        }
    }

    @Override
    public Map<String, Object> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setProperties(Map<String, Object> arg0) {
        // TODO Auto-generated method stub
        
    }


    /** inject resources into servant.  The resources are injected
     * according to @Resource annotations.  See JSR 250 for more
     * information         AnnotationProcessor

     *
     */
    private void injectResources() { 

        // TODO ResourceResolver needs to be globally available and to
        // resolve more that just WebServiceContexts
        ResourceInjector injector = new ResourceInjector(new ResourceResolver() { 
                public Object resolve(String resourceName, Class<?> resourceType) {
                    return new WebServiceContextImpl();
                }
            });

        injector.inject(implementor);
    }


    private void configureHandlers() { 

        LOG.fine("loading handler chain for endpoint"); 
        HandlerChainBuilder builder = new HandlerChainBuilder();
        List<Handler> chain = builder.buildHandlerChainFor(implementor); 
        serverBinding.getBinding().setHandlerChain(chain); 
    } 
}
