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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.spi.ServiceDelegate;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.databinding.source.SourceDataBinding;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.binding.soap.JaxWsSoapBindingConfiguration;
import org.apache.cxf.jaxws.handler.HandlerResolverImpl;
import org.apache.cxf.jaxws.handler.PortInfoImpl;
import org.apache.cxf.jaxws.support.DummyImpl;
import org.apache.cxf.jaxws.support.JaxWsClientEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.AbstractServiceFactoryBean;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.workqueue.OneShotAsyncExecutor;
import org.apache.cxf.wsdl11.WSDLServiceFactory;

public class ServiceImpl extends ServiceDelegate {

    private static final Logger LOG = LogUtils.getL7dLogger(ServiceImpl.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();

    private Bus bus;
    private URL wsdlURL;

    private HandlerResolver handlerResolver;
    private final Collection<QName> ports = new HashSet<QName>();
    private Map<QName, PortInfoImpl> portInfos = new HashMap<QName, PortInfoImpl>();
    private Executor executor;
    private QName serviceName;
    private Class<?> clazz;

    public ServiceImpl(Bus b, URL url, QName name, Class<?> cls) {
        bus = b;
        wsdlURL = url;
        this.serviceName = name;
        clazz = cls;
        
        handlerResolver = new HandlerResolverImpl(bus, name, clazz);
        if (url != null) {
            try {
                initializePorts();
            } catch (ServiceConstructionException e) {
                throw new WebServiceException(e);
            }
        }
    }
    
    private void initializePorts() {        
        WSDLServiceFactory sf = new WSDLServiceFactory(bus, wsdlURL, serviceName);
        Service service = sf.create();
        for (ServiceInfo si : service.getServiceInfos()) { 
            for (EndpointInfo ei : si.getEndpoints()) {
                this.ports.add(ei.getName());
                addPort(ei.getName(), ei.getTransportId(), ei.getAddress());
            }
        }
    }

    public final void addPort(QName portName, String bindingId, String address) {
        PortInfoImpl portInfo = new PortInfoImpl(bindingId, portName, serviceName);
        portInfo.setAddress(address);
        portInfos.put(portName, portInfo);
    }

    private Endpoint getJaxwsEndpoint(QName portName, AbstractServiceFactoryBean sf) {
        Service service = sf.getService();
        EndpointInfo ei = null;
        if (portName == null) {
            ei = service.getServiceInfos().get(0).getEndpoints().iterator().next();
        } else {
            ei = service.getEndpointInfo(portName);
            if (ei == null) {
                PortInfoImpl portInfo = getPortInfo(portName);
                if (null != portInfo) {
                    try {
                        ei = createEndpointInfo(sf, portName, portInfo);
                    } catch (BusException e) {
                        throw new WebServiceException(e);
                    }
                }
            }
        }

        if (ei == null) {
            Message msg = new Message("INVALID_PORT", BUNDLE, portName);
            throw new WebServiceException(msg.toString());
        }

        try {
            return new JaxWsClientEndpointImpl(bus, service, ei, this);
        } catch (EndpointException e) {
            throw new WebServiceException(e);
        }
    }

    private AbstractServiceFactoryBean createDispatchService(DataBinding db) {
        AbstractServiceFactoryBean serviceFactory;

        Service dispatchService = null;        
        
        if (null != wsdlURL) {
            WSDLServiceFactory sf = new WSDLServiceFactory(bus, wsdlURL, serviceName);
            dispatchService = sf.create();            
            dispatchService.setDataBinding(db);
            serviceFactory = sf;
        } else {
            ReflectionServiceFactoryBean sf = new JaxWsServiceFactoryBean();
            sf.setBus(bus);
            sf.setServiceName(serviceName);
            // maybe we can find another way to create service which have no SEI
            sf.setServiceClass(DummyImpl.class);
            sf.setDataBinding(db);
            dispatchService = sf.create();
            serviceFactory = sf;
        }    
        configureObject(dispatchService);
        return serviceFactory;
    }

    public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Mode mode) {
        AbstractServiceFactoryBean sf = null;
        try {
            sf = createDispatchService(new SourceDataBinding());
        } catch (ServiceConstructionException e) {
            throw new WebServiceException(e);
        }
        Endpoint endpoint = getJaxwsEndpoint(portName, sf);
        Dispatch<T> disp = new DispatchImpl<T>(bus, mode, type, getExecutor(), endpoint);

        configureObject(disp);

        return disp;
    }

    public Dispatch<Object> createDispatch(QName portName, JAXBContext context, Mode mode) {
        AbstractServiceFactoryBean sf = null;
        try {
            sf = createDispatchService(new JAXBDataBinding(context));
        } catch (ServiceConstructionException e) {
            throw new WebServiceException(e);
        }
        Endpoint endpoint = getJaxwsEndpoint(portName, sf);
        Dispatch<Object> disp = new DispatchImpl<Object>(bus, mode, context, Object.class, getExecutor(),
                                                         endpoint);

        configureObject(disp);

        return disp;
    }

    public Executor getExecutor() {
        return executor;
    }

    public HandlerResolver getHandlerResolver() {
        return handlerResolver;
    }

    public <T> T getPort(Class<T> type) {
        try {
            return createPort(null, type);
        } catch (ServiceConstructionException e) {
            throw new WebServiceException(e);
        }  
    }

    public <T> T getPort(QName portName, Class<T> type) {
        if (portName == null) {
            throw new WebServiceException(BUNDLE.getString("PORT_NAME_NULL_EXC"));
        }
        try {
            return createPort(portName, type);
        } catch (ServiceConstructionException e) {
            throw new WebServiceException(e);
        }  
    }

    public Iterator<QName> getPorts() {
        return ports.iterator();
    }

    public QName getServiceName() {
        return serviceName;
    }

    public URL getWSDLDocumentLocation() {
        return wsdlURL;
    }

    public void setExecutor(Executor e) {
        this.executor = e;
    }

    public void setHandlerResolver(HandlerResolver hr) {
        handlerResolver = hr;
    }

    public Bus getBus() {
        return bus;
    }

    protected <T> T createPort(QName portName, Class<T> serviceEndpointInterface) {
        LOG.log(Level.FINE, "creating port for portName", portName);
        LOG.log(Level.FINE, "endpoint interface:", serviceEndpointInterface);

        JaxWsProxyFactoryBean proxyFac = new JaxWsProxyFactoryBean();
        JaxWsClientFactoryBean clientFac = (JaxWsClientFactoryBean) proxyFac.getClientFactoryBean();
        ReflectionServiceFactoryBean serviceFactory = proxyFac.getServiceFactory();
        
        proxyFac.setBus(bus);
        proxyFac.setServiceClass(serviceEndpointInterface);
        proxyFac.setServiceName(serviceName);

        if (wsdlURL != null) {
            proxyFac.setWsdlURL(wsdlURL.toString());
        }
        
        if (portName == null) {
            clientFac.create();
            portName = serviceFactory.getEndpointName();
        } else {
            serviceFactory.setEndpointName(portName);
        }
        
        PortInfoImpl portInfo = portInfos.get(portName);
        if (portInfo != null) {
            clientFac.setBindingId(portInfo.getBindingID());
            clientFac.setAddress(portInfo.getAddress());
        }
        configureObject(portName.toString() + ".jaxwsProxyFactory", proxyFac);
        
        Object obj = proxyFac.create();
        
        // Configure the Service
        Service service = serviceFactory.getService();
        service.setExecutor(new Executor() {
            public void execute(Runnable command) {
                Executor ex = getExecutor();
                if (ex == null) {
                    ex = OneShotAsyncExecutor.getInstance();
                } 
                ex.execute(command);
            }
        });
        configureObject(service);
                
        // Configure the JaxWsEndpoitnImpl
        JaxWsEndpointImpl jaxwsEndpoint = (JaxWsEndpointImpl) clientFac.getClient().getEndpoint();
        configureObject(jaxwsEndpoint);  
                                      
        List<Handler> hc = jaxwsEndpoint.getJaxwsBinding().getHandlerChain();
        
        hc.addAll(handlerResolver.getHandlerChain(portInfos.get(portName)));

        LOG.log(Level.FINE, "created proxy", obj);

        ports.add(portName);
        return serviceEndpointInterface.cast(obj);
    }

    private EndpointInfo createEndpointInfo(AbstractServiceFactoryBean serviceFactory, 
                                            QName portName,
                                            PortInfoImpl portInfo) throws BusException {
        EndpointInfo ei = null;
        String address = portInfo.getAddress();

        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
        DestinationFactory df = dfm.getDestinationFactoryForUri(portInfo.getAddress());

        String transportId = df.getTransportIds().get(0);
        String bindingID = portInfo.getBindingID();

        
        Object config = null;
        if (serviceFactory instanceof JaxWsServiceFactoryBean) {
            config = new JaxWsSoapBindingConfiguration((JaxWsServiceFactoryBean)serviceFactory);
        }
        BindingInfo bindingInfo = bus.getExtension(BindingFactoryManager.class).getBindingFactory(bindingID)
                .createBindingInfo(serviceFactory.getService(), bindingID, config);


        Service service = serviceFactory.getService();
        service.getServiceInfos().get(0).addBinding(bindingInfo);

        // TODO we may need to get the transportURI from Address
        ei = new EndpointInfo(service.getServiceInfos().get(0), transportId);
        ei.setName(portName);
        ei.setAddress(address);
        ei.setBinding(bindingInfo);

        service.getServiceInfos().get(0).addEndpoint(ei);
        return ei;
    }

    private void configureObject(Object instance) {
        configureObject(null, instance);
    }
    
    private void configureObject(String name, Object instance) {
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(name, instance);
        }
    }

    private PortInfoImpl getPortInfo(QName portName) {
        // TODO if the portName null ?
        return portInfos.get(portName);
    }

    
    
    //  TODO JAX-WS 2.1
    /*
    public <T> Dispatch<T> createDispatch(QName portName,
                                          Class<T> type,
                                          Mode mode,
                                          WebServiceFeature... features) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public <T> Dispatch<T> createDispatch(EndpointReference endpointReference,
                                          Class<T> type,
                                          Mode mode,
                                          WebServiceFeature... features) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Dispatch<Object> createDispatch(QName portName,
                                           JAXBContext context,
                                           Mode mode,
                                           WebServiceFeature... features) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Dispatch<Object> createDispatch(EndpointReference endpointReference,
                                           JAXBContext context,
                                           Mode mode,
                                           WebServiceFeature... features) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public <T> T getPort(Class<T> serviceEndpointInterface,
                         WebServiceFeature... features) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public <T> T getPort(QName portName,
                         Class<T> serviceEndpointInterface,
                         WebServiceFeature... features) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public <T> T getPort(EndpointReference endpointReference,
                         Class<T> serviceEndpointInterface,
                         WebServiceFeature... features) {
        // TODO
        throw new UnsupportedOperationException();
    }
    */
}
