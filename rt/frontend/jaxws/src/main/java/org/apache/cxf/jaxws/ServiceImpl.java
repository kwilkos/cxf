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
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;

import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.spi.ServiceDelegate;

import org.apache.cxf.Bus;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.handler.HandlerResolverImpl;
import org.apache.cxf.jaxws.support.JaxwsEndpointImpl;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl11.WSDLServiceFactory;

public class ServiceImpl extends ServiceDelegate {

    private static final Logger LOG = LogUtils.getL7dLogger(ServiceImpl.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();

    private Bus bus;
    private URL wsdlURL;

    private Service service;
    private HandlerResolver handlerResolver;
    private final Collection<QName> ports = new HashSet<QName>();

    public ServiceImpl(Bus b, URL url, QName name, Class<?> cls) {
        bus = b;
        wsdlURL = url;

        if (url == null) {
            ServiceInfo info = new ServiceInfo();
            service = new org.apache.cxf.service.ServiceImpl(info);
        } else {
            WSDLServiceFactory sf = new WSDLServiceFactory(bus, url, name);
            service = sf.create();
        }
        handlerResolver = new HandlerResolverImpl(bus, name);

        try {
            JAXBDataBinding dataBinding = new JAXBDataBinding(cls, service);
            service.setDataReaderFactory(dataBinding.getDataReaderFactory());
            service.setDataWriterFactory(dataBinding.getDataWriterFactory());
        } catch (JAXBException e) {
            throw new WebServiceException(new Message("FAILED_TO_INITIALIZE_JAXBCONTEXT",
                                                      LOG,
                                                      cls.getName()).toString(), e);
        }
    }

    public void addPort(QName portName, String bindingId, String address) {
        throw new WebServiceException(new Message("UNSUPPORTED_API_EXC", LOG, "addPort").toString());
    }

    private Endpoint getJaxwsEndpoint(QName portName) {
        ServiceInfo si = service.getServiceInfo();
        EndpointInfo ei = null;
        if (portName == null) {
            ei = si.getEndpoints().iterator().next();
        } else {
            ei = si.getEndpoint(portName);
        }
        
        try {
            return new JaxwsEndpointImpl(bus, service, ei);
        } catch (EndpointException e) {
            throw new WebServiceException(e);
        }        
    }
    
    public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Mode mode) {
        Dispatch<T> disp = new DispatchImpl<T>(bus, 
            mode, 
            type, 
            getExecutor(), 
            getJaxwsEndpoint(portName));

        return disp;
    }

    public Dispatch<Object> createDispatch(QName portName, JAXBContext context, Mode mode) {

        Dispatch<Object> disp = new DispatchImpl<Object>(bus, 
            mode, 
            context, 
            Object.class, 
            getExecutor(),
            getJaxwsEndpoint(portName));

        return disp;
    }

    public Executor getExecutor() {
        return service.getExecutor();
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
        return service.getName();
    }

    public URL getWSDLDocumentLocation() {
        return wsdlURL;
    }

    public void setExecutor(Executor e) {
        service.setExecutor(e);
    }

    public void setHandlerResolver(HandlerResolver hr) {
        handlerResolver = hr;
    }

    public Bus getBus() {
        return bus;
    }

    public Service getService() {
        return service;
    }

    protected <T> T createPort(QName portName, Class<T> serviceEndpointInterface) {

        LOG.log(Level.FINE, "creating port for portName", portName);
        LOG.log(Level.FINE, "endpoint interface:", serviceEndpointInterface);

        QName pn = portName;
        ServiceInfo si = service.getServiceInfo();
        EndpointInfo ei = null;
        if (portName == null) {
            if (1 == si.getEndpoints().size()) {
                ei = si.getEndpoints().iterator().next();
                pn = new QName(service.getName().getNamespaceURI(), ei.getName().getLocalPart());
            }
        } else {
            ei = si.getEndpoint(portName);
        }
        if (null == pn) {
            throw new WebServiceException(BUNDLE.getString("COULD_NOT_DETERMINE_PORT"));
        }

        JaxwsEndpointImpl jaxwsEndpoint;
        try {
            jaxwsEndpoint = new JaxwsEndpointImpl(bus, service, ei);
        } catch (EndpointException e) {
            throw new WebServiceException(e);
        }
        
        Client client = new ClientImpl(bus, jaxwsEndpoint);

        InvocationHandler ih = new EndpointInvocationHandler(client, jaxwsEndpoint.getJaxwsBinding());

        // configuration stuff 
        // createHandlerChainForBinding(serviceEndpointInterface, portName, endpointHandler.getBinding());

        Object obj = Proxy
            .newProxyInstance(serviceEndpointInterface.getClassLoader(),
                              new Class[] {serviceEndpointInterface, BindingProvider.class}, ih);

        LOG.log(Level.FINE, "created proxy", obj);

        ports.add(pn);
        return serviceEndpointInterface.cast(obj);
    }

}
