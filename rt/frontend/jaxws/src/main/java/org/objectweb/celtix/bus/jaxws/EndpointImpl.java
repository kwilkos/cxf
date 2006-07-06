package org.objectweb.celtix.bus.jaxws;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.soap.SOAPBinding.Style;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingType;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServicePermission;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.bindings.ServerDataBindingCallback;
import org.objectweb.celtix.bus.handlers.AnnotationHandlerChainBuilder;
import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerChainType;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.injection.ResourceInjector;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.configuration.ConfigurationBuilder;
import org.objectweb.celtix.configuration.ConfigurationBuilderFactory;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.endpoints.ContextInspector;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.celtix.jaxb.ServerDynamicDataBindingCallback;
import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.management.InstrumentationFactory;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class EndpointImpl extends javax.xml.ws.Endpoint
    implements ServerBindingEndpointCallback, InstrumentationFactory {

    public static final String ENDPOINT_CONFIGURATION_URI = 
        "http://celtix.objectweb.org/bus/jaxws/endpoint-config";
    private static final String ENABLE_HANDLER_INIT = "enableHandlerInit";
    private static final Logger LOG = LogUtils.getL7dLogger(EndpointImpl.class);
    
    protected EndpointReferenceType reference;
    
    protected boolean published;
    
    protected final Bus bus;
    protected final Object implementor;
    protected Class<?> implementorClass;
    protected final String bindingURI;
    
    protected Configuration configuration;
    protected List<Source> metadata;
    protected Executor executor;
    protected JAXBContext context;
    protected Schema schema;
    protected Map<String, Object> properties;
    protected ServerBinding serverBinding;
    protected String address; 
    
    protected boolean doInit;
    protected boolean initialised;
    
    //Implemetor (SEI) specific members
    protected List<Class<?>> seiClass;
    
    //Implementor (Provider) specific members
    protected ServiceMode serviceMode;
    protected WebServiceProvider wsProvider;
    protected Class<?> dataClass;
    
    protected Map<QName, ServerDataBindingCallback> callbackMap
        = new ConcurrentHashMap<QName, ServerDataBindingCallback>();
    
    
    public EndpointImpl(Bus b, Object impl, String bindingId) {
        this(b, impl, bindingId, EndpointReferenceUtils.getEndpointReference(b.getWSDLManager(), impl));
    }
    
    public EndpointImpl(Bus b, Object impl, String bindingId, EndpointReferenceType ref) {
        this(b, impl, impl.getClass(), bindingId, ref);
    }
    
    
    public EndpointImpl(Bus b, Object obj, Class<?> implClass, String bindingId, EndpointReferenceType ref) {

        bus = b;
        implementor = obj;
        implementorClass = implClass;
        reference = ref;
        bindingURI = bindingId;

        if (Provider.class.isAssignableFrom(implementorClass)) {
            //Provider Implementor
            wsProvider = implementorClass.getAnnotation(WebServiceProvider.class);
            if (wsProvider == null) {
                throw new WebServiceException(
                           "Provider based implementor must carry a WebServiceProvider annotation");
            }
            serviceMode = implementorClass.getAnnotation(ServiceMode.class);
        } else {
            //SEI Implementor
            try {
                context = JAXBEncoderDecoder.createJAXBContextForClass(implementorClass);
            } catch (JAXBException ex1) {
                ex1.printStackTrace();
                context = null;
            }
        }

        if (bus != null) {
            //NOTE  EndpointRegistry need to check the Registry instrumentation is created
            bus.getEndpointRegistry().registerEndpoint(this);
        }

        doInit = true;
    }
    
    private synchronized void init() {
        if (doInit) {
            try {
                injectResources();
                initProvider();
                initProperties();
                initMetaData();
    
                configuration = createConfiguration();
                if (null  != configuration) {
                    serverBinding = createServerBinding(bindingURI);
                    configureHandlers();
                    configureSystemHandlers();
                    configureSchemaValidation();
                }
                
                initOpMap();
            } catch (Exception ex) {
                if (ex instanceof WebServiceException) { 
                    throw (WebServiceException)ex; 
                }
                throw new WebServiceException("Creation of Endpoint failed", ex);
            }
        }
        doInit = false;
    }
    
    private void initOpMap() throws WSDLException {
        Definition def = EndpointReferenceUtils.getWSDLDefinition(bus.getWSDLManager(), reference);
        if (def == null) {
            return;
        }
        Port port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), reference);
        List ops = port.getBinding().getBindingOperations();
        Iterator opIt = ops.iterator();
        while (opIt.hasNext()) {
            BindingOperation op = (BindingOperation)opIt.next();
            BindingInput bindingInput = op.getBindingInput();
            List elements = bindingInput.getExtensibilityElements();
            QName qn = new QName(def.getTargetNamespace(), op.getName());
            for (Iterator i = elements.iterator(); i.hasNext();) {
                Object element = i.next();
                if (SOAPBody.class.isInstance(element)) {
                    SOAPBody body = (SOAPBody)element;
                    if (body.getNamespaceURI() != null) {
                        qn = new QName(body.getNamespaceURI(), op.getName());
                    }
                }
            }
            
            ServerDataBindingCallback cb = getDataBindingCallback(qn, null,
                                                                  DataBindingCallback.Mode.PARTS);
            callbackMap.put(qn, cb);
            if (!"".equals(cb.getRequestWrapperQName().getLocalPart())) {
                callbackMap.put(cb.getRequestWrapperQName(), cb);
            }
        }
    }
    
    private void initProperties() {
        if (null != properties) {
            QName serviceName = (QName)properties.get(Endpoint.WSDL_SERVICE);
            QName portName = (QName)properties.get(Endpoint.WSDL_PORT);            
            if (null != serviceName && null != portName) {
                EndpointReferenceUtils.setServiceAndPortName(reference, serviceName, 
                                                                       portName.getLocalPart());
            }
        }
    }
    
    private void initMetaData() {
        if (null != metadata) {
            EndpointReferenceUtils.setMetadata(reference, metadata);
        }
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
    
    public void releaseImplementor(Object impl) {
        //no-op for normal cases
    }

    /**
     * @return Returns the Implementor Class.
     */
    public Class getImplementorClass() {
        return implementorClass;
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
        if (doInit) {
            init();
            initialised = true;
        }
        if (isPublished()) {
            LOG.warning("ENDPOINT_ALREADY_PUBLISHED_MSG");
        }
        // apply all changes to configuration and metadata and (re-)activate
        try {
            address = getAddressFromContext(serverContext);
            if (!isContextBindingCompatible(address)) {
                throw new IllegalArgumentException(
                    new BusException(new Message("BINDING_INCOMPATIBLE_CONTEXT_EXC", LOG)));
            }
            publish(address);
        } catch (Exception ex) {
            throw new WebServiceException(ex);
        }   
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.ws.Endpoint#publish(java.lang.String)
     */
    public void publish(String addr) {
        
        address = addr;
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new WebServicePermission("endpointPublish"));    
        }
        if (doInit && !initialised) {
            init();
        }
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
        if (isPublished()) {
            throw new IllegalStateException("Endpoint has already been published"); 
        }
        metadata = m;
        doInit = true;
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
        try {
            serverBinding.deactivate();
        } catch (IOException ex) {
            throw new WebServiceException(ex);
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

    private String getBindingIdFromWSDL() {
        Port port = null;
        try {
            port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), reference);
        } catch (WSDLException we) {
            return null;
        }
        return ((ExtensibilityElement)port.getExtensibilityElements().get(0)).
            getElementType().getNamespaceURI();
    }

    ServerBinding createServerBinding(String bindingId) throws BusException, WSDLException, IOException {
        if (null == bindingId) {
            BindingType bType = implementorClass.getAnnotation(BindingType.class);
            if (bType != null) {
                bindingId = bType.value();
            }
        }
        
        String bindingIdFromWSDL = null;
        if (bindingId == null) {
            bindingIdFromWSDL = getBindingIdFromWSDL();
        }
        
        if (null == bindingId && null != bindingIdFromWSDL) {
            bindingId = bindingIdFromWSDL;
        }
        
        if (null == bindingId) {
            // Use SOAP1.1/HTTP Binding as default. JAX-WS Spec 5.2.1
            bindingId = SOAPBinding.SOAP11HTTP_BINDING; 
        }
        
        BindingFactory factory = bus.getBindingManager().getBindingFactory(bindingId);
        if (null == factory) {
            throw new BusException(new Message("BINDING_FACTORY_MISSING_EXC", LOG, bindingId));
        }
        ServerBinding bindingImpl = factory.createServerBinding(reference, this);
        assert null != bindingImpl;
        return bindingImpl;

    }

    String getAddressFromContext(Object ctx) throws Exception {
        List<String> strs = configuration.getStringList("serverContextInspectors");
        Iterator iter = strs.iterator();
        String addr = null;
        while (iter.hasNext()) {
            String className = (String)iter.next();
            
            try {
                LOG.log(Level.FINE, "loading context inspector", className);

                Class<? extends ContextInspector> inspectorClass = 
                    Class.forName(className, true, 
                                  getContextInspectorClassLoader()).asSubclass(ContextInspector.class);

                ContextInspector inspector = inspectorClass.newInstance();
                addr = inspector.getAddress(ctx);
                if (addr != null) {
                    return addr;
                }
            } catch (ClassNotFoundException e) {
                throw new WebServiceException(
                    new Message("CONTEXT_INSPECTOR_INSTANTIATION_EXC", LOG).toString(), e);
            } catch (InstantiationException e) {
                throw new WebServiceException(
                    new Message("CONTEXT_INSPECTOR_INSTANTIATION_EXC", LOG).toString(), e);
            } catch (IllegalAccessException e) {
                throw new WebServiceException(
                    new Message("CONTEXT_INSPECTOR_INSTANTIATION_EXC", LOG).toString(), e);
            }
        }
        return addr;
    }

    protected boolean isContextBindingCompatible(String addr) {
        return serverBinding.isBindingCompatible(addr);    
    }

    void doPublish(String addr) {

        EndpointReferenceUtils.setAddress(reference, addr);      
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

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> arg0) {
        properties = arg0;
        doInit = true;
    }


    /**
     * inject resources into servant.  The resources are injected
     * according to @Resource annotations.  See JSR 250 for more
     * information.
     */
    protected void injectResources(Object instance) {
        if (instance != null) {
            ResourceInjector injector = new ResourceInjector(bus.getResourceManager());
            injector.inject(instance);
        }
    }

    /**
     * inject resources into servant.  The resources are injected
     * according to @Resource annotations.  See JSR 250 for more
     * information.
     */
    protected void injectResources() {
        injectResources(implementor);
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
        builder.setHandlerClassLoader(implementorClass.getClassLoader());
            
        List<Handler> chain = builder.buildHandlerChainFromConfiguration(hc);
        builder.setHandlerInitEnabled(configuration.getBoolean(ENABLE_HANDLER_INIT));
        
        if (null == chain || chain.size() == 0) {
            chain = builder.buildHandlerChainFor(implementorClass);
        }
        serverBinding.getBinding().setHandlerChain(chain);
    }

    private void configureSystemHandlers() {
        serverBinding.configureSystemHandlers(configuration);
    }

    private void configureSchemaValidation() {
        Boolean enableSchemaValidation = configuration.getObject(Boolean.class,
            "enableSchemaValidation");

        if (enableSchemaValidation != null && enableSchemaValidation.booleanValue()) {
            LOG.fine("endpoint schema validation enabled"); 
            schema = EndpointReferenceUtils.getSchema(bus.getWSDLManager(), reference);
        }
    }

    public DataBindingCallback getFaultDataBindingCallback(ObjectMessageContext objContext) {
        return new JAXBDataBindingCallback(null,
                                         DataBindingCallback.Mode.PARTS,
                                         context,
                                         schema,
                                         this);        
    }

    @SuppressWarnings("unchecked")
    public ServerDataBindingCallback getDataBindingCallback(QName operationName,
                                                            ObjectMessageContext objContext,
                                                            DataBindingCallback.Mode mode) {
        if (mode == DataBindingCallback.Mode.PARTS) {
            ServerDataBindingCallback cb = callbackMap.get(operationName);
            if (null == cb) {
                cb = new JAXBDataBindingCallback(getMethod(operationName),
                                                 DataBindingCallback.Mode.PARTS,
                                                 context,
                                                 schema,
                                                 this);
            }
            
            return cb;
        }
        
        if (dataClass == null) {
            dataClass = EndpointUtils.getProviderParameterType(this);
        }

        return new ServerDynamicDataBindingCallback(dataClass, mode, (Provider<?>) implementor);
    }

    public Method getMethod(QName operationName) {

        if (wsProvider == null) {
            return EndpointUtils.getMethod(this, operationName); 
        }

        Method invokeMethod = null;
        if (operationName.getLocalPart().equals("invoke")) {

            try {
                if (dataClass == null) {
                    dataClass = EndpointUtils.getProviderParameterType(this);
                }
                invokeMethod = implementorClass.getMethod("invoke", dataClass);
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
            seiClass = EndpointUtils.getWebServiceAnnotatedClass(implementorClass);
        }
        return seiClass;
    }

    private Configuration createConfiguration() {

        Configuration busCfg = bus.getConfiguration();
        if (null == busCfg) {
            return null;
        }

        Configuration cfg = null;
        String id = EndpointReferenceUtils.getServiceName(reference).toString();
        ConfigurationBuilder cb = ConfigurationBuilderFactory.getBuilder(null);
        cfg = cb.getConfiguration(ENDPOINT_CONFIGURATION_URI, id, busCfg);
        if (null == cfg) {
            cfg = cb.buildConfiguration(ENDPOINT_CONFIGURATION_URI, id, busCfg);
        }
        return cfg;
    }
    
    private ClassLoader getContextInspectorClassLoader() {
        return getClass().getClassLoader();
    }    
        
    void start() {
        if (published) {
            return;
        } else {
            try {
                serverBinding.activate();
                published = true;
            } catch (IOException e) {
                //e.printStackTrace();
            } catch (WSDLException e) {
                //e.printStackTrace(); 
            }
        }
    }


    public Map<QName, ? extends DataBindingCallback> getOperations() {
        return callbackMap;
    }

    public Style getStyle() {
        javax.jws.soap.SOAPBinding bind = implementorClass
            .getAnnotation(javax.jws.soap.SOAPBinding.class);
        if (bind != null) {
            return bind.style();
        }
        return javax.jws.soap.SOAPBinding.Style.DOCUMENT;
    }
    
    private void initProvider() {
        // if no wsdl is specified, lets fake service port name
        if (wsProvider != null 
            && (null == wsProvider.wsdlLocation() || "".equals(wsProvider.wsdlLocation()))) {
            
            String ns = wsProvider.targetNamespace(); 
            if (null == ns || "".equals(ns)) {
                ns = "http://localhost/" + implementorClass.getPackage().getName().replace(".", "/");
            }
            
            try {
                URL addrURL = new URL(address);
                String path = addrURL.getPath();
                if (path.contains("/")) {
                    int index = path.indexOf("/");
                    QName serviceName = new QName(ns, path.substring(0, index));
                    String portName = path.substring(index);
                    EndpointReferenceUtils.setServiceAndPortName(reference, serviceName, portName);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public Instrumentation createInstrumentation() {
        return new EndpointInstrumentation(this);
    }
}
