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

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.interceptor.AbstractBasicInterceptorProvider;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.ws.AbstractWSFeature;
import org.apache.cxf.wsdl11.WSDLEndpointFactory;

public abstract class AbstractEndpointFactory extends AbstractBasicInterceptorProvider {
    private static final Logger LOG = LogUtils.getL7dLogger(AbstractEndpointFactory.class);

    private Bus bus;
    private String address;
    private String transportId;
    private String bindingId;
    private Class serviceClass;
    private DestinationFactory destinationFactory;
    private ReflectionServiceFactoryBean serviceFactory;
    private QName endpointName;
    private Map<String, Object> properties;
    private List<AbstractWSFeature> features;
    private Object bindingConfig;

    protected Endpoint createEndpoint() throws BusException, EndpointException {
        Service service = serviceFactory.getService();
        
        if (service == null) {
            Class cls = getServiceClass();

            serviceFactory.setServiceClass(cls);
            serviceFactory.setBus(getBus());
            service = serviceFactory.create();
        }
        
        if (endpointName == null) {
            endpointName = serviceFactory.getEndpointName();
        }

        EndpointInfo ei = service.getServiceInfo().getEndpoint(endpointName);
        if (ei != null
            && transportId != null
            && !ei.getTransportId().equals(transportId)) {
            ei = null;
        }
        
        Endpoint ep = null;
        if (ei == null) {
            ei = createEndpointInfo();
        } else if (getAddress() != null) {
            ei.setAddress(getAddress()); 
        }                        

        ep = service.getEndpoints().get(ei.getName());
        
        if (ep == null) {
            ep = serviceFactory.createEndpoint(ei);
        }
        
        if (properties != null) {
            ep.putAll(properties);
        }
        
        service.getEndpoints().put(ep.getEndpointInfo().getName(), ep);
        
        if (getInInterceptors() != null) {
            ep.getInInterceptors().addAll(getInInterceptors());
        }
        if (getOutInterceptors() != null) {
            ep.getOutInterceptors().addAll(getOutInterceptors());
        }
        if (getInFaultInterceptors() != null) {
            ep.getInFaultInterceptors().addAll(getInFaultInterceptors());
        }
        if (getOutFaultInterceptors() != null) {
            ep.getOutFaultInterceptors().addAll(getOutFaultInterceptors());
        }
        return ep;
    }

    protected EndpointInfo createEndpointInfo() throws BusException {
        if (transportId == null) {
            if (getAddress() != null) {
                DestinationFactory df = getDestinationFactory();
                if (df == null) {
                    DestinationFactoryManager dfm = getBus().getExtension(DestinationFactoryManager.class);
                    df = dfm.getDestinationFactoryForUri(getAddress());
                }
                
                if (df != null) {
                    transportId = df.getTransportIds().get(0);
                }
            }
            
            if (transportId == null) {
                // TODO: we shouldn't have to do this, but the DF is null because the
                // LocalTransport doesn't return for the http:// uris
                // People also seem to be supplying a null JMS getAddress(), which is worrying
                transportId = "http://schemas.xmlsoap.org/soap/http";
            }
        }
        
        // Get the Service from the ServiceFactory if specified
        Service service = serviceFactory.getService();


        // SOAP nonsense
        BindingInfo bindingInfo = createBindingInfo();
        if (bindingInfo instanceof SoapBindingInfo) {
            ((SoapBindingInfo) bindingInfo).setTransportURI(transportId);
            transportId = "http://schemas.xmlsoap.org/wsdl/soap/";
        }
        
        service.getServiceInfo().addBinding(bindingInfo);

        setTransportId(transportId);
        
        if (destinationFactory == null) {
            DestinationFactoryManager dfm = getBus().getExtension(DestinationFactoryManager.class);
            destinationFactory = dfm.getDestinationFactory(transportId);
        }
        
        EndpointInfo ei = new EndpointInfo(service.getServiceInfo(), transportId);
        int count = 1;
        while (service.getServiceInfo().getEndpoint(endpointName) != null) {
            endpointName = new QName(endpointName.getNamespaceURI(), 
                                     endpointName.getLocalPart() + count);
            count++;
        }
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

    protected BindingInfo createBindingInfo() {
        BindingFactoryManager mgr = bus.getExtension(BindingFactoryManager.class);
        String binding = bindingId;
        if (binding == null) {
            // default to soap binding
            binding = "http://schemas.xmlsoap.org/soap/";
        }
        try {
            return mgr.getBindingFactory(binding).createBindingInfo(serviceFactory.getService(),
                                                                    binding, bindingConfig);
        } catch (BusException ex) {
            throw new ServiceConstructionException(
                   new Message("COULD.NOT.RESOLVE.BINDING", LOG, bindingId), ex);
        }
    }

    public String getAddress() {
        if (address != null && address.indexOf("://") == -1) {
            String a2 = "http://localhost";
            if (!address.startsWith("/")) {
                a2 += "/";
            }
            
            a2 += address;
            return a2;
        }
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Bus getBus() {
        if (bus == null) {
            bus = BusFactory.getDefaultBus();
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
    public void setBindingId(String bind) {
        bindingId = bind;
    }
    public String getBindingId() {
        return bindingId;
    }

    public void setBindingConfig(Object obj) {
        bindingConfig = obj;
    }
    public Object getBindingConfig() {
        return bindingConfig;
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

    public List<AbstractWSFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<AbstractWSFeature> features) {
        this.features = features;
    }
    
}