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
package org.apache.cxf.service.factory;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.soap.SoapBindingInfoFactoryBean;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.ChainInitiationObserver;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.wsdl11.WSDLEndpointFactory;

public class ServerFactoryBean {
    private ReflectionServiceFactoryBean serviceFactory;
    private Service service;
    private DestinationFactory destinationFactory;
    private Server server;
    private Bus bus;
    private String address;
    private String transportId; // where should this come from???
    private AbstractBindingInfoFactoryBean bindingFactory;
    private QName endpointName;
    private boolean start = true;
    private Class serviceClass;
    
    public ServerFactoryBean() {
        super();
        bindingFactory = new SoapBindingInfoFactoryBean();
    }
    
    public Server create() {
        try {
            service = serviceFactory.getService();
            
            if (service == null) {
                serviceFactory.setServiceClass(serviceClass);
                serviceFactory.setBus(bus);
                service = serviceFactory.create();
            }
            
            if (endpointName == null) {
                endpointName = serviceFactory.getEndpointName();
            }

            EndpointInfo ei = service.getServiceInfo().getEndpoint(endpointName);
            Endpoint ep = null;
            if (ei == null) {
                ei = createEndpoint();
            } else if (address != null) {
                ei.setAddress(address); 
            }                        
            
            ep = service.getEndpoints().get(ei.getName());
            if (ep == null) {
                ep = serviceFactory.createEndpoint(ei);
            }
            service.getEndpoints().put(ep.getEndpointInfo().getName(), ep);
            server = new ServerImpl(bus, ep, new ChainInitiationObserver(ep, bus));
            
            if (start) {
                server.start();
            }
        } catch (EndpointException e) {
            throw new ServiceConstructionException(e);
        } catch (BusException e) {
            throw new ServiceConstructionException(e);
        } catch (IOException e) {
            throw new ServiceConstructionException(e);
        }
        
        return server;
    }

    private EndpointInfo createEndpoint() throws BusException {
        if (transportId == null) {
            if (address != null) {
                DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
                DestinationFactory df = dfm.getDestinationFactoryForUri(address);
                if (df != null) {
                    transportId = df.getTransportIds().get(0);
                }
            }
            
            if (transportId == null) {
                // TODO: we shouldn't have to do this, but the DF is null because the
                // LocalTransport doesn't return for the http:// uris
                // People also seem to be supplying a null JMS address, which is worrying
                transportId = "http://schemas.xmlsoap.org/wsdl/soap/http";
            }
        }
        
        // SOAP nonsense
        if (bindingFactory instanceof SoapBindingInfoFactoryBean) {
            ((SoapBindingInfoFactoryBean) bindingFactory).setTransportURI(transportId);
            transportId = "http://schemas.xmlsoap.org/wsdl/soap/";
        }
        
        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
        destinationFactory = dfm.getDestinationFactory(transportId);
        
        // Get the Service from the ServiceFactory if specified        
        bindingFactory.setServiceFactory(serviceFactory);
        BindingInfo bindingInfo = bindingFactory.create();
        service.getServiceInfo().addBinding(bindingInfo);
        
        EndpointInfo ei = new EndpointInfo(service.getServiceInfo(), transportId);
        ei.setName(endpointName);
        ei.setAddress(address);
        ei.setBinding(bindingInfo);
        
        if (destinationFactory instanceof WSDLEndpointFactory) {
            WSDLEndpointFactory we = (WSDLEndpointFactory) destinationFactory;
            
            we.createPortExtensors(ei, service);
        } else {
            // ?
        }
        service.getServiceInfo().addEndpoint(ei);
        return ei;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public DestinationFactory getDestinationFactory() {
        return destinationFactory;
    }

    public void setDestinationFactory(DestinationFactory destinationFactory) {
        this.destinationFactory = destinationFactory;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public ReflectionServiceFactoryBean getServiceFactory() {
        return serviceFactory;
    }

    public void setServiceFactory(ReflectionServiceFactoryBean serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    public String getTransportId() {
        return transportId;
    }

    public void setTransportId(String transportId) {
        this.transportId = transportId;
    }

    public QName getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(QName endpointName) {
        this.endpointName = endpointName;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public AbstractBindingInfoFactoryBean getBindingFactory() {
        return bindingFactory;
    }

    public void setBindingFactory(AbstractBindingInfoFactoryBean bindingFactory) {
        this.bindingFactory = bindingFactory;
    }

    public Class getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class serviceClass) {
        this.serviceClass = serviceClass;
    }    
}
