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

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.WebServicePermission;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.configuration.Configurable;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
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

    private static final WebServicePermission PUBLISH_PERMISSION =
        new WebServicePermission("publishEndpoint");
    
    private Bus bus;
    private Object implementor;
    private Server server;
    private JaxWsServerFactoryBean serverFactory;
    private JaxWsServiceFactoryBean serviceFactory;
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
        this(BusFactory.getThreadDefaultBus(), implementor);
    }
   
    public EndpointImpl(Bus b, Object implementor, 
                        JaxWsServerFactoryBean sf) {
        this.bus = b;
        this.serverFactory = sf;
        this.implementor = implementor;
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
        wsdlLocation = wsdl == null ? null : new String(wsdl);
        serverFactory = new JaxWsServerFactoryBean();
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
    
    public JaxWsServiceFactoryBean getServiceFactory() {
        return serviceFactory;
    }

    
    @Override
    public Object getImplementor() {
        return implementor;
    }

    /**
     * Gets the class of the implementor.
     * @return the class of the implementor object
     */
    public Class getImplementorClass() {
        return implementor.getClass();
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

    public void setServiceFactory(JaxWsServiceFactoryBean sf) {
        serviceFactory = sf;
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
        
        ServerImpl serv = getServer(addr);
        if (addr != null) {
            serv.getEndpoint().getEndpointInfo().setAddress(addr);
        }
        serv.start();
    }
    
    public ServerImpl getServer() {
        return getServer(null);
    }
    public synchronized ServerImpl getServer(String addr) {
        if (server == null) {
            checkProperties();

            // Initialize the endpointName so we can do configureObject
            if (endpointName == null) {
                JaxWsImplementorInfo implInfo = new JaxWsImplementorInfo(getImplementorClass());
                endpointName = implInfo.getEndpointName();
            }
            
            if (serviceFactory != null) {
                serverFactory.setServiceFactory(serviceFactory);
            }

            /*if (serviceName != null) {
                serverFactory.getServiceFactory().setServiceName(serviceName);
            }*/

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
            
            if (getWsdlLocation() == null) {
                //hold onto the wsdl location so cache won't clear till we go away
                setWsdlLocation(serverFactory.getWsdlURL());
            }
        }
        return (ServerImpl) server;
    }
    
    org.apache.cxf.endpoint.Endpoint getEndpoint() {
        return ((ServerImpl)getServer(null)).getEndpoint();
    }
    
    private void configureObject(Object instance) {
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(instance);
        }
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
        if (features == null) {
            features = new ArrayList<AbstractFeature>();
        }
        return features;
    }

    public void setFeatures(List<AbstractFeature> features) {
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
