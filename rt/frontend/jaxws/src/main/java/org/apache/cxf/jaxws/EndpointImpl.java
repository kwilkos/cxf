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

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;

import org.apache.cxf.Bus;
import org.apache.cxf.common.injection.ResourceInjector;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.jaxws.context.WebContextResourceResolver;
import org.apache.cxf.jaxws.handler.AnnotationHandlerChainBuilder;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.resource.DefaultResourceManager;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.resource.ResourceResolver;
import org.apache.cxf.service.Service;

public class EndpointImpl extends javax.xml.ws.Endpoint {
    private static final Logger LOG = LogUtils.getL7dLogger(EndpointImpl.class);

    protected boolean doInit;

    private Bus bus;
    private Object implementor;
    private Server server;
    private Service service;
    private JaxWsImplementorInfo implInfo;
    private JaxWsServiceFactoryBean serviceFactory;
    private Map<String, Object> properties;
    
    public EndpointImpl(Bus b, Object implementor, JaxWsServiceFactoryBean serviceFactory) {
        this.bus = b;
        this.serviceFactory = serviceFactory;
        this.implInfo = serviceFactory.getJaxWsImplementorInfo();
        this.service = serviceFactory.getService();
        this.implementor = implementor;
        
        if (this.service == null) {
            service = serviceFactory.create();
        }
        
        doInit = true;
    }
    
    @SuppressWarnings("unchecked")
    public EndpointImpl(Bus b, Object i, String uri, URL wsdl) {
        bus = b;
        implementor = i;
        // build up the Service model
        implInfo = new JaxWsImplementorInfo(implementor.getClass());
        serviceFactory = new JaxWsServiceFactoryBean(implInfo);
        serviceFactory.setBus(bus);
        if (null != wsdl) {
            serviceFactory.setWsdlURL(wsdl);
        }
        service = serviceFactory.create();
        
        configureObject(service);
        
        service.put(Message.SCHEMA_VALIDATION_ENABLED, service.getEnableSchemaValidationForAllPort());
        
        service.setInvoker(new JAXWSMethodInvoker(i));
        
        doInit = true; 
    }
    
    
    public EndpointImpl(Bus b, Object i, String uri) {
        this(b, i, uri, (URL)null);
    }
   
   

    public EndpointImpl(Bus bus, Object implementor) {
        this(bus, implementor, (String) null);
    }

    public Binding getBinding() {
        return ((JaxWsEndpointImpl) getEndpoint()).getJaxwsBinding();
    }

    public void setExecutor(Executor executor) {
        service.setExecutor(executor);
    }

    public Executor getExecutor() {
        return service.getExecutor();
    }

    @Override
    public Object getImplementor() {
        return implementor;
    }

    @Override
    public List<Source> getMetadata() {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub

    }

    @Override
    public void publish(String address) {
        doPublish(address);
    }

    public void setMetadata(List<Source> arg0) {
        // TODO Auto-generated method stub

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
            resourceManager.addResourceResolver(new WebContextResourceResolver());
            ResourceInjector injector = new ResourceInjector(resourceManager);
            injector.inject(instance);
        }
    }

    protected void doPublish(String address) {

        JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
        svrFactory.setBus(bus);
        svrFactory.setAddress(address);
        svrFactory.setServiceFactory(serviceFactory);
        svrFactory.setStart(false);
        svrFactory.setServiceBean(implementor);
        configureObject(svrFactory);
        
        server = svrFactory.create();

        init();
        
        org.apache.cxf.endpoint.Endpoint endpoint = getEndpoint();
        
        if (properties != null) {
            endpoint.putAll(properties);
        }
        
        configureObject(endpoint);
        
        if (endpoint.getEnableSchemaValidation()) {
            endpoint.put(Message.SCHEMA_VALIDATION_ENABLED, endpoint.getEnableSchemaValidation());
        }
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
                configureHandlers();
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
    private void configureHandlers() {
        LOG.fine("loading handler chain for endpoint");
        AnnotationHandlerChainBuilder builder = new AnnotationHandlerChainBuilder();

        List<Handler> chain = null;

        if (null == chain || chain.size() == 0) {
            chain = builder.buildHandlerChainFromClass(implementor.getClass());
        }
        getBinding().setHandlerChain(chain);
    }
}