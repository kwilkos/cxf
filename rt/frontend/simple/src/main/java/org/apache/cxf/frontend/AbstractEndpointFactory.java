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
package org.apache.cxf.frontend;

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.soap.SoapBindingInfoFactoryBean;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.AbstractBindingInfoFactoryBean;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.wsdl11.WSDLEndpointFactory;

public abstract class AbstractEndpointFactory {

    private Bus bus;
    private String address;
    private String transportId;
    private AbstractBindingInfoFactoryBean bindingFactory;
    private Class serviceClass;
    private DestinationFactory destinationFactory;
    private ReflectionServiceFactoryBean serviceFactory;
    private QName endpointName;
    private Map<String, Object> properties;
    
    protected Endpoint createEndpoint() throws BusException, EndpointException {
        Service service = serviceFactory.getService();
        
        if (service == null) {
            serviceFactory.setServiceClass(getServiceClass());
            serviceFactory.setBus(getBus());
            service = serviceFactory.create();
        }
        
        if (endpointName == null) {
            endpointName = serviceFactory.getEndpointName();
        }

        EndpointInfo ei = service.getServiceInfo().getEndpoint(endpointName);
        Endpoint ep = null;
        if (ei == null) {
            ei = createEndpointInfo();
        } else if (getAddress() != null) {
            ei.setAddress(getAddress()); 
        }                        
        
        setProps(ei);
        
        ep = service.getEndpoints().get(ei.getName());
        if (ep == null) {
            ep = serviceFactory.createEndpoint(ei);
        }
        service.getEndpoints().put(ep.getEndpointInfo().getName(), ep);
        return ep;
    }

    private void setProps(EndpointInfo ei) {
        if (properties == null) {
            return;
        }
        
        for (Map.Entry<String, Object> e : properties.entrySet()) {
            ei.setProperty(e.getKey(), e.getValue());
        }
    }

    protected EndpointInfo createEndpointInfo() throws BusException {
        if (transportId == null) {
            if (getAddress() != null) {
                DestinationFactoryManager dfm = getBus().getExtension(DestinationFactoryManager.class);
                DestinationFactory df = dfm.getDestinationFactoryForUri(getAddress());
                if (df != null) {
                    transportId = df.getTransportIds().get(0);
                }
            }
            
            if (transportId == null) {
                // TODO: we shouldn't have to do this, but the DF is null because the
                // LocalTransport doesn't return for the http:// uris
                // People also seem to be supplying a null JMS getAddress(), which is worrying
                transportId = "http://schemas.xmlsoap.org/wsdl/soap/http";
            }
        }
        
        // SOAP nonsense
        if (getBindingFactory() instanceof SoapBindingInfoFactoryBean) {
            ((SoapBindingInfoFactoryBean) getBindingFactory()).setTransportURI(transportId);
            transportId = "http://schemas.xmlsoap.org/wsdl/soap/";
        }
        
        setTransportId(transportId);
        
        DestinationFactoryManager dfm = getBus().getExtension(DestinationFactoryManager.class);
        destinationFactory = dfm.getDestinationFactory(transportId);
        
        // Get the Service from the ServiceFactory if specified
        Service service = serviceFactory.getService();
        getBindingFactory().setServiceFactory(serviceFactory);
        BindingInfo bindingInfo = getBindingFactory().create();
        service.getServiceInfo().addBinding(bindingInfo);
        
        EndpointInfo ei = new EndpointInfo(service.getServiceInfo(), transportId);
        ei.setName(endpointName);
        ei.setAddress(getAddress());
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
        if (bus == null) {
            bus = BusFactory.newInstance().getDefaultBus();
        }
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public String getTransportId() {
        return transportId;
    }

    public void setTransportId(String transportId) {
        this.transportId = transportId;
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


    public DestinationFactory getDestinationFactory() {
        return destinationFactory;
    }


    public void setDestinationFactory(DestinationFactory destinationFactory) {
        this.destinationFactory = destinationFactory;
    }


    public ReflectionServiceFactoryBean getServiceFactory() {
        return serviceFactory;
    }


    public void setServiceFactory(ReflectionServiceFactoryBean serviceFactory) {
        this.serviceFactory = serviceFactory;
    }


    public QName getEndpointName() {
        return endpointName;
    }


    public void setEndpointName(QName endpointName) {
        this.endpointName = endpointName;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}