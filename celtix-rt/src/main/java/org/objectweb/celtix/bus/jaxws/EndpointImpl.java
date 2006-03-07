package org.objectweb.celtix.bus.jaxws;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.wsdl.WSDLException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingType;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.bus.handlers.AnnotationHandlerChainBuilder;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerChainType;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.injection.ResourceInjector;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public final class EndpointImpl extends javax.xml.ws.Endpoint
    implements ServerBindingEndpointCallback {

    public static final String ENDPOINT_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/bus/jaxws/endpoint-config";
    
    private static final Logger LOG = LogUtils.getL7dLogger(EndpointImpl.class);

    private final Bus bus;
    private final Object implementor;
    private final String bindingURI;
    
    private Configuration configuration;
    private EndpointReferenceType reference;
    private ServerBinding serverBinding;
    private boolean published;
    private List<Source> metadata;
    private Executor executor;
    private JAXBContext context;
    private Map<String, Object> properties;
    
    private boolean doInit;
    
    //Implemetor (SEI) specific members
    private List<Class<?>> seiClass;
    
    //Implementor (Provider) specific members
    private ServiceMode serviceMode;
    private WebServiceProvider wsProvider;
    private Class<?> dataClass;
    
    public EndpointImpl(Bus b, Object impl, String bindingId) {
        this(b, impl, bindingId, EndpointReferenceUtils.getEndpointReference(b.getWSDLManager(), impl));
    }
    
    public EndpointImpl(Bus b, Object impl, String bindingId, EndpointReferenceType ref) {
        bus = b;
        implementor = impl;
        reference = ref;

        if (null == bindingId) {
            BindingType bType = implementor.getClass().getAnnotation(BindingType.class);
            if (bType != null) {
                bindingId = bType.value();
            }
        }

        if (null == bindingId) {
            // Use SOAP1.1/HTTP Binding as default. JAX-WS Spec 5.2.1
            bindingId = SOAPBinding.SOAP11HTTP_BINDING; 
        }
        
        bindingURI = bindingId;
        if (Provider.class.isAssignableFrom(impl.getClass())) {
            //Provider Implementor
            wsProvider = implementor.getClass().getAnnotation(WebServiceProvider.class);
            if (wsProvider == null) {
                throw new WebServiceException(
                           "Provider based implementor must carry a WebServiceProvider annotation");
            }
            serviceMode = implementor.getClass().getAnnotation(ServiceMode.class);
        } else {
            //SEI Implementor
            try {           
                injectResources();
            } catch (Exception ex) {
                if (ex instanceof WebServiceException) { 
                    throw (WebServiceException)ex; 
                }
                throw new WebServiceException("Creation of Endpoint failed", ex);
            }
            
            try {
                context = JAXBEncoderDecoder.createJAXBContextForClass(impl.getClass());
            } catch (JAXBException ex1) {
                ex1.printStackTrace();
                context = null;
            }
        }
        
        doInit = true;
    }


    private void init() {
        try {
            if (properties != null) {
                QName val = (QName) properties.get(Endpoint.WSDL_SERVICE);
                if (null != val) {
                    EndpointReferenceUtils.setServiceName(reference, val);
                }
                
                val = (QName) properties.get(Endpoint.WSDL_PORT);
                if (null != val) {
                    EndpointReferenceUtils.setPortName(reference, val.toString());
                }
            }
            
            configuration = createConfiguration();
            serverBinding = createServerBinding(bindingURI);
            configureHandlers();
            configureSystemHandlers();
        } catch (Exception ex) {
            if (ex instanceof WebServiceException) { 
                throw (WebServiceException)ex; 
            }
            throw new WebServiceException("Creation of Endpoint failed", ex);
        }
        
        doInit = false;
    }
    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.Endpoint#getBinding()
     */
    public Binding getBinding() {
        //Initialise the Endpoint so HandlerChain is set up on Binding.
        if (doInit) {
            init();
        }
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
            throw new IllegalArgumentException(new BusException(new Message("BINDING_INCOMPATIBLE_CONTEXT_EXC"
                                                                            , LOG)));
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
        doPublish(address);
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
        if (doInit) {
            init();
        }

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
        ServerBinding bindingImpl = factory.createServerBinding(reference, this, this);
        assert null != bindingImpl;
        return bindingImpl;

    }

    String getAddressFromContext(Object ctx) {
        return null;
    }

    boolean isContextBindingCompatible(Object ctx) {
        return true;
    }

    void doPublish(String address) {
        if (doInit) {
            init();
        }

        EndpointReferenceUtils.setAddress(reference, address);
        try {
            serverBinding.activate();
            published = true;
        } catch (WSDLException ex) {
            LOG.log(Level.SEVERE, "SERVER_BINDING_ACTIVATION_FAILURE_MSG", ex);
            throw new WebServiceException(ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "SERVER_BINDING_ACTIVATION_FAILURE_MSG", ex);
            throw new WebServiceException(ex);
        }
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> arg0) {
        properties = arg0;
        doInit = true;
    }


    /** 
     * inject resources into servant.  The resources are injected
     * according to @Resource annotations.  See JSR 250 for more
     * information.
     */
    private void injectResources() { 
        ResourceInjector injector = new ResourceInjector(bus.getResourceManager());
        injector.inject(implementor);
    }


    /** 
     * Obtain handler chain from configuration first. If none is specified, 
     * default to the chain configured in the code, i.e. in annotations.
     *
     */
    private void configureHandlers() { 
        
        LOG.fine("loading handler chain for endpoint"); 
        AnnotationHandlerChainBuilder builder = new AnnotationHandlerChainBuilder();
        HandlerChainType hc = (HandlerChainType)configuration.getObject("handlerChain");
        List<Handler> chain = builder.buildHandlerChainFromConfiguration(hc);
        if (null == chain || chain.size() == 0) {
            chain = builder.buildHandlerChainFor(implementor.getClass()); 
        }
        serverBinding.getBinding().setHandlerChain(chain);
    }
    
    private void configureSystemHandlers() {
        serverBinding.configureSystemHandlers(configuration); 
    }

    public DataBindingCallback createDataBindingCallback(ObjectMessageContext objContext,
                                                         DataBindingCallback.Mode mode) {
        if (mode == DataBindingCallback.Mode.PARTS) {
            return new JAXBDataBindingCallback(objContext.getMethod(),
                                           mode,
                                           context);
        }
        
        if (dataClass == null) {
            dataClass = EndpointUtils.getProviderParameterType(this);
        }

        return new DynamicDataBindingCallback(dataClass, mode);
    }

    public Method getMethod(Endpoint endpoint, QName operationName) {
        
        if (wsProvider == null) {
            return EndpointUtils.getMethod(endpoint, operationName); 
        }

        Method invokeMethod = null;
        if (operationName.getLocalPart().equals("invoke")) {

            try {
                if (dataClass == null) {
                    dataClass = EndpointUtils.getProviderParameterType(endpoint);
                }
                invokeMethod = implementor.getClass().getMethod("invoke", dataClass);
            } catch (NoSuchMethodException ex) {
                //TODO
            }
        }
        return invokeMethod;
    }

    public DataBindingCallback.Mode getServiceMode() {
        DataBindingCallback.Mode mode = DataBindingCallback.Mode.PARTS;
        
        if (wsProvider != null) {
            mode = serviceMode != null 
                    ? DataBindingCallback.Mode.fromServiceMode(serviceMode.value())
                    : DataBindingCallback.Mode.PAYLOAD;
        }
        return mode; 
    }
    
    public WebServiceProvider getWebServiceProvider() {
        return wsProvider;
    } 
 
    public synchronized List<Class<?>> getWebServiceAnnotatedClass() {
        if (null == seiClass) {
            seiClass = EndpointUtils.getWebServiceAnnotatedClass(implementor.getClass());
        }
        return seiClass;
    }
    
    private Configuration createConfiguration() {
        
        Configuration busCfg = bus.getConfiguration();
        assert null != busCfg;
        Configuration cfg = null;
        String id = EndpointReferenceUtils.getServiceName(reference).toString();
        ConfigurationBuilder cb = ConfigurationBuilderFactory.getBuilder(null);
        cfg = cb.getConfiguration(ENDPOINT_CONFIGURATION_URI, id, busCfg);
        if (null == cfg) {
            cfg = cb.buildConfiguration(ENDPOINT_CONFIGURATION_URI, id, busCfg);
        }
        return cfg;
    }
}
