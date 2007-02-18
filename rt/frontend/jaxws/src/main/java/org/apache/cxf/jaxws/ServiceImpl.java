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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
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

import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.spi.ServiceDelegate;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.xml.XMLBindingInfoFactoryBean;
import org.apache.cxf.binding.xml.XMLConstants;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.databinding.source.SourceDataBinding;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.binding.soap.JaxWsSoapBindingInfoFactoryBean;
import org.apache.cxf.jaxws.handler.HandlerResolverImpl;
import org.apache.cxf.jaxws.handler.PortInfoImpl;
import org.apache.cxf.jaxws.support.DummyImpl;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.AbstractBindingInfoFactoryBean;
import org.apache.cxf.service.factory.AbstractServiceFactoryBean;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
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
        
    }

    public void addPort(QName portName, String bindingId, String address) {
        PortInfoImpl portInfo = new PortInfoImpl(bindingId, portName, serviceName);
        portInfo.setAddress(address);
        portInfos.put(portName, portInfo);
    }

    private Endpoint getJaxwsEndpoint(QName portName, AbstractServiceFactoryBean sf) {
        Service service = sf.getService();
        ServiceInfo si = service.getServiceInfo();
        EndpointInfo ei = null;
        if (portName == null) {
            ei = si.getEndpoints().iterator().next();
        } else {
            PortInfoImpl portInfo = getPortInfo(portName);
            if (null != portInfo) {
                try {
                    ei = createEndpointInfo(sf, portName, portInfo);
                } catch (BusException e) {
                    throw new WebServiceException(e);
                }
            } else {
                ei = si.getEndpoint(portName);
            }
        }

        try {
            return new JaxWsEndpointImpl(bus, service, ei);
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
        AbstractServiceFactoryBean sf = createDispatchService(new SourceDataBinding());
        Endpoint endpoint = getJaxwsEndpoint(portName, sf);

        Dispatch<T> disp = new DispatchImpl<T>(bus, mode, type, getExecutor(), endpoint);

        configureObject(disp);

        return disp;
    }

    public Dispatch<Object> createDispatch(QName portName, JAXBContext context, Mode mode) {

        AbstractServiceFactoryBean sf = createDispatchService(new JAXBDataBinding(context));
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
        return createPort(null, type);
    }

    public <T> T getPort(QName portName, Class<T> type) {
        if (portName == null) {
            throw new WebServiceException(BUNDLE.getString("PORT_NAME_NULL_EXC"));
        }
        return createPort(portName, type);
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

        ReflectionServiceFactoryBean serviceFactory = new JaxWsServiceFactoryBean();
        serviceFactory.setBus(bus);
        serviceFactory.setServiceName(serviceName);
        serviceFactory.setServiceClass(serviceEndpointInterface);

        if (wsdlURL != null) {
            serviceFactory.setWsdlURL(wsdlURL);
        }

        Service service = serviceFactory.create();
        configureObject(service);
        service.put(Message.SCHEMA_VALIDATION_ENABLED, service.getEnableSchemaValidationForAllPort());

        QName pn = portName;
        ServiceInfo si = service.getServiceInfo();

        EndpointInfo ei = null;
        if (portName == null) {
            if (1 <= si.getEndpoints().size()) {
                Iterator it = si.getEndpoints().iterator();
                if (1 == si.getEndpoints().size()) {
                    ei = (EndpointInfo) it.next();
                    pn = new QName(service.getName().getNamespaceURI(), ei.getName().getLocalPart());
                } else {
                    WebService webService = (WebService) serviceEndpointInterface
                        .getAnnotation(WebService.class);
                    String name = webService.name();
                    String nameSpace = webService.targetNamespace();
                    QName portTypeName = new QName(nameSpace, name);
                    EndpointInfo epi = null;
                    while (it.hasNext()) {
                        epi = (EndpointInfo) it.next();
                        //InterfaceInfo interfaceInfo = epi.getBinding().getInterface();
                        if (epi.getBinding().getInterface().getName().equals(portTypeName)) {
                            pn = new QName(service.getName().getNamespaceURI(), epi.getName().getLocalPart());
                            ei = epi;
                            break;
                        }
                    }
                }
            }
        } else {
            // first check the endpointInfo from portInfos
            PortInfoImpl portInfo = portInfos.get(portName);
            if (null != portInfo) {
                try {
                    ei = createEndpointInfo(serviceFactory, portName, portInfo);
                } catch (BusException e) {
                    throw new WebServiceException(e);
                }
            } else {
                ei = si.getEndpoint(portName);
            }
        }
        if (null == pn) {
            throw new WebServiceException(BUNDLE.getString("COULD_NOT_DETERMINE_PORT"));
        }

        JaxWsEndpointImpl jaxwsEndpoint;
        try {
            jaxwsEndpoint = new JaxWsEndpointImpl(bus, service, ei);
        } catch (EndpointException e) {
            throw new WebServiceException(e);
        }
        configureObject(jaxwsEndpoint);        
        
        if (jaxwsEndpoint.getEnableSchemaValidation()) {
            jaxwsEndpoint.put(Message.SCHEMA_VALIDATION_ENABLED, jaxwsEndpoint.getEnableSchemaValidation());
        }

        Client client = new ClientImpl(bus, jaxwsEndpoint);

        InvocationHandler ih = new JaxWsClientProxy(client, jaxwsEndpoint.getJaxwsBinding());
        
        List<Handler> hc = ((JaxWsClientProxy)ih).getBinding().getHandlerChain();
        hc.addAll(handlerResolver.getHandlerChain(portInfos.get(pn)));

        // configuration stuff
        // createHandlerChainForBinding(serviceEndpointInterface, portName,
        // endpointHandler.getBinding());

        Object obj = Proxy
            .newProxyInstance(serviceEndpointInterface.getClassLoader(),
                              new Class[] {serviceEndpointInterface, BindingProvider.class}, ih);

        LOG.log(Level.FINE, "created proxy", obj);

        ports.add(pn);
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

        // TODO: Replace with discovery mechanism, now it just the hardcode        
        AbstractBindingInfoFactoryBean bindingFactory = null;
        if (bindingID.equals(XMLConstants.NS_XML_FORMAT) 
            || bindingID.equals(HTTPBinding.HTTP_BINDING)) {
            bindingFactory = new XMLBindingInfoFactoryBean();
        } else { // we set the default binding to be soap binding
            JaxWsSoapBindingInfoFactoryBean soapBindingFactory = new JaxWsSoapBindingInfoFactoryBean();
            soapBindingFactory.setTransportURI(transportId);
            bindingFactory = soapBindingFactory;
        } 

        bindingFactory.setServiceFactory(serviceFactory);

        BindingInfo bindingInfo = bindingFactory.create();
        Service service = serviceFactory.getService();
        service.getServiceInfo().addBinding(bindingInfo);

        // TODO we may need to get the transportURI from Address
        ei = new EndpointInfo(service.getServiceInfo(), transportId);
        ei.setName(portName);
        ei.setAddress(address);
        ei.setBinding(bindingInfo);

        service.getServiceInfo().addEndpoint(ei);
        return ei;
    }

    private void configureObject(Object instance) {
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(instance);
        }
    }

    private PortInfoImpl getPortInfo(QName portName) {
        // TODO if the portName null ?
        return portInfos.get(portName);
    }
}
