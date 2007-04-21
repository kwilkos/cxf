/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.jaxws;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServicePermission;
import javax.xml.ws.handler.Handler;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.common.injection.ResourceInjector;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.Configurable;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.jaxws.context.WebServiceContextResourceResolver;
import org.apache.cxf.jaxws.handler.AnnotationHandlerChainBuilder;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.resource.DefaultResourceManager;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.resource.ResourceResolver;
import org.apache.cxf.service.Service;

public class EndpointImpl extends javax.xml.ws.Endpoint 
    implements InterceptorProvider, Configurable {
    /**
     * This property controls whether the 'publishEndpoint' permission is checked 
     * using only the AccessController (i.e. when SecurityManager is not installed).
     * By default this check is not done as the system property is not set.
     */
    public static final String CHECK_PUBLISH_ENDPOINT_PERMISSON_PROPERTY =
        "org.apache.cxf.jaxws.checkPublishEndpointPermission";

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointImpl.class);

    private static final WebServicePermission PUBLISH_PERMISSION =
        new WebServicePermission("publishEndpoint");
    
    protected boolean doInit;
    private Bus bus;
    private Object implementor;
    private Server server;
    private JaxWsServerFactoryBean serverFactory;
    private Service service;
    private Map<String, Object> properties;
    private List<Source> metadata;
    
    private Executor executor;
    private String bindingUri;
    private String wsdlLocation;
    private String address;
    private QName endpointName;
    private QName serviceName;
    
    private List<AbstractFeature> features;
    private List<Interceptor> in = new CopyOnWriteArrayList<Interceptor>();
    private List<Interceptor> out = new CopyOnWriteArrayList<Interceptor>();
    private List<Interceptor> outFault  = new CopyOnWriteArrayList<Interceptor>();
    private List<Interceptor> inFault  = new CopyOnWriteArrayList<Interceptor>();

    public EndpointImpl(Object implementor) {
        this(BusFactory.getDefaultBus(), implementor);
    }
   
    public EndpointImpl(Bus b, Object implementor, 
                        JaxWsServerFactoryBean sf) {
        this.bus = b;
        this.serverFactory = sf;
        this.implementor = implementor;
        
        doInit = true;
    }
    
    /**
     * 
     * @param b
     * @param i The implementor object.
     * @param bindingUri The URI of the Binding being used. Optional.
     * @param wsdl The URL of the WSDL for the service, if different than the URL specified on the
     * WebService annotation. Optional.
     */
    public EndpointImpl(Bus b, Object i, String bindingUri, String wsdl) {
        bus = b;
        implementor = i;
        this.bindingUri = bindingUri;
        wsdlLocation = wsdl;
        serverFactory = new JaxWsServerFactoryBean();
        
        doInit = true; 
    }
    
    
    public EndpointImpl(Bus b, Object i, String bindingUri) {
        this(b, i, bindingUri, (String)null);
    }
   
    public EndpointImpl(Bus bus, Object implementor) {
        this(bus, implementor, (String) null);
    }

    public Binding getBinding() {
        return ((JaxWsEndpointImpl) getEndpoint()).getJaxwsBinding();
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Executor getExecutor() {
        return executor;
    }

    public Service getService() {
        return service;
    }

    
    @Override
    public Object getImplementor() {
        return implementor;
    }

    public List<Source> getMetadata() {
        return metadata;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public boolean isPublished() {
        return server != null;
    }

    @Override
    public void publish(Object arg0) {
        // Since this does not do anything now, just check the permission
        checkPublishPermission();
    }

    @Override
    public void publish(String addr) {
        doPublish(addr);
    }

    public void setMetadata(List<Source> metadata) {
        this.metadata = metadata;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
        
        if (server != null) {
            server.getEndpoint().putAll(properties);
        }
    }

    @Override
    public void stop() {
        if (null != server) {
            server.stop();
        }
    }

    public ServerImpl getServer() {
        return (ServerImpl) server;
    }
    

    /**
     * inject resources into servant.  The resources are injected
     * according to @Resource annotations.  See JSR 250 for more
     * information.
     */
    /**
     * @param instance
     */
    protected void injectResources(Object instance) {
        if (instance != null) {
            ResourceManager resourceManager = bus.getExtension(ResourceManager.class);
            List<ResourceResolver> resolvers = resourceManager.getResourceResolvers();
            resourceManager = new DefaultResourceManager(resolvers); 
            resourceManager.addResourceResolver(new WebServiceContextResourceResolver());
            ResourceInjector injector = new ResourceInjector(resourceManager);
            injector.inject(instance);
        }
    }

    
    public String getBeanName() {
        return endpointName.toString() + ".jaxws-endpoint";
    }

    protected void checkProperties() {
        if (properties != null) {
            if (properties.containsKey("javax.xml.ws.wsdl.description")) {
                wsdlLocation = properties.get("javax.xml.ws.wsdl.description").toString();
            }
            if (properties.containsKey(javax.xml.ws.Endpoint.WSDL_PORT)) {
                endpointName = (QName)properties.get(javax.xml.ws.Endpoint.WSDL_PORT);
            }
            if (properties.containsKey(javax.xml.ws.Endpoint.WSDL_SERVICE)) {
                serviceName = (QName)properties.get(javax.xml.ws.Endpoint.WSDL_SERVICE);
            }
        }
    }
    
    protected void doPublish(String addr) {
        checkPublishPermission();
        checkProperties();

        // Initialize the endpointName so we can do configureObject
        if (endpointName == null) {
            JaxWsImplementorInfo implInfo = new JaxWsImplementorInfo(implementor.getClass());
            endpointName = implInfo.getEndpointName();
        }
        
        configureObject(this);
        
        // Set up the server factory
        serverFactory.setAddress(addr);
        serverFactory.setStart(false);
        serverFactory.setEndpointName(endpointName);
        serverFactory.setServiceBean(implementor);
        serverFactory.setBus(bus);
        serverFactory.setFeatures(features);
        
        // Be careful not to override any serverfactory settings as a user might
        // have supplied their own.
        if (getWsdlLocation() != null) {
            serverFactory.setWsdlURL(getWsdlLocation());
        }
        
        if (bindingUri != null) {
            serverFactory.setBindingId(bindingUri);
        }
        
        if (serviceName != null) {
            serverFactory.getServiceFactory().setServiceName(serviceName);
        }
        
        configureObject(serverFactory);
        
        server = serverFactory.create();
        
        init();
        
        org.apache.cxf.endpoint.Endpoint endpoint = getEndpoint();
        if (getInInterceptors() != null) {
            endpoint.getInInterceptors().addAll(getInInterceptors());
        }
        if (getOutInterceptors() != null) {
            endpoint.getOutInterceptors().addAll(getOutInterceptors());
        }
        if (getInFaultInterceptors() != null) {
            endpoint.getInFaultInterceptors().addAll(getInFaultInterceptors());
        }
        if (getOutFaultInterceptors() != null) {
            endpoint.getOutFaultInterceptors().addAll(getOutFaultInterceptors());
        }
        
        if (properties != null) {
            endpoint.putAll(properties);
        }
        
        configureObject(endpoint.getService());
        configureObject(endpoint);
        
        server.start();
    }
    
    org.apache.cxf.endpoint.Endpoint getEndpoint() {
        return ((ServerImpl)getServer()).getEndpoint();
    }
    
    private void configureObject(Object instance) {
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(instance);
        }
    }


    private synchronized void init() {
        if (doInit) {
            try {
                injectResources(implementor);
                buildHandlerChain();
            } catch (Exception ex) {
                if (ex instanceof WebServiceException) { 
                    throw (WebServiceException)ex; 
                }
                throw new WebServiceException("Creation of Endpoint failed", ex);
            }
        }
        doInit = false;
    }
    
    /**
     * Obtain handler chain from annotations.
     *
     */
    private void buildHandlerChain() {
        LOG.fine("loading handler chain for endpoint");
        AnnotationHandlerChainBuilder builder = new AnnotationHandlerChainBuilder();

        List<Handler> chain = builder.buildHandlerChainFromClass(implementor.getClass());
        for (Handler h : chain) {
            injectResources(h);
        }
        
        getBinding().setHandlerChain(chain);
    }
    
    protected void checkPublishPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(PUBLISH_PERMISSION);
        } else if (Boolean.getBoolean(CHECK_PUBLISH_ENDPOINT_PERMISSON_PROPERTY)) {
            AccessController.checkPermission(PUBLISH_PERMISSION);
        }
    }

    public void publish() {
        publish(getAddress());
    }
    
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public QName getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(QName endpointName) {
        this.endpointName = endpointName;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }

    public String getWsdlLocation() {
        return wsdlLocation;
    }

    public void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }

    public void setBindingUri(String binding) {
        this.bindingUri = binding;
    }
    public String getBindingUri() {
        return this.bindingUri;
    }

    public List<Interceptor> getOutFaultInterceptors() {
        return outFault;
    }

    public List<Interceptor> getInFaultInterceptors() {
        return inFault;
    }

    public List<Interceptor> getInInterceptors() {
        return in;
    }

    public List<Interceptor> getOutInterceptors() {
        return out;
    }

    public void setInInterceptors(List<Interceptor> interceptors) {
        in = interceptors;
    }

    public void setInFaultInterceptors(List<Interceptor> interceptors) {
        inFault = interceptors;
    }

    public void setOutInterceptors(List<Interceptor> interceptors) {
        out = interceptors;
    }

    public void setOutFaultInterceptors(List<Interceptor> interceptors) {
        outFault = interceptors;
    }

    public List<AbstractFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<AbstractFeature> features) {
        if (features == null) {
            features = new ArrayList<AbstractFeature>();
        }
        this.features = features;
    }
    
    /*
    //TODO JAX-WS 2.1
    public EndpointReference getEndpointReference(Element... referenceParameters) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz,
                                                                Element... referenceParameters) {
        // TODO
        throw new UnsupportedOperationException();
    }
    */
}
