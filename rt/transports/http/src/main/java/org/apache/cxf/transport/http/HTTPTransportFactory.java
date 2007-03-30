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

package org.apache.cxf.transport.http;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.wsdl.Port;
import javax.wsdl.extensions.http.HTTPAddress;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.security.SSLClientPolicy;
import org.apache.cxf.configuration.security.SSLServerPolicy;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.AbstractTransportFactory;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.https.HttpsURLConnectionFactory;
import org.apache.cxf.transport.https.JettySslConnectorFactory;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl11.WSDLEndpointFactory;
import org.mortbay.jetty.AbstractConnector;
//import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.xmlsoap.schemas.wsdl.http.AddressType;

public class HTTPTransportFactory extends AbstractTransportFactory implements ConduitInitiator,
    DestinationFactory, WSDLEndpointFactory {

    private static final Set<String> URI_PREFIXES = new HashSet<String>();
    static {
        URI_PREFIXES.add("http://");
        URI_PREFIXES.add("https://");
    }

    private Bus bus;
    private Collection<String> activationNamespaces;

    @Resource(name = "bus")
    public void setBus(Bus b) {
        bus = b;
    }

    public Bus getBus() {
        return bus;
    }

    @Resource(name = "activationNamespaces")
    public void setActivationNamespaces(Collection<String> ans) {
        activationNamespaces = ans;
    }

    @PostConstruct
    void registerWithBindingManager() {
        if (null == bus) {
            return;
        }
        ConduitInitiatorManager cim = bus.getExtension(ConduitInitiatorManager.class);

        //Note, activationNamespaces can be null
        if (null != cim && null != activationNamespaces) {
            for (String ns : activationNamespaces) {
                cim.registerConduitInitiator(ns, this);
            }
        }
        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
        if (null != dfm && null != activationNamespaces) {
            for (String ns : activationNamespaces) {
                dfm.registerDestinationFactory(ns, this);
            }
        }
    }

    public Conduit getConduit(EndpointInfo endpointInfo) throws IOException {
        return getConduit(endpointInfo, null);
    }

    public Conduit getConduit(EndpointInfo endpointInfo, EndpointReferenceType target) throws IOException {
        HTTPConduit conduit = target == null
            ? new HTTPConduit(bus, endpointInfo) : new HTTPConduit(bus, endpointInfo, target);
        configure(conduit);
        conduit.retrieveConnectionFactory();
        return conduit;
    }

    public Destination getDestination(EndpointInfo endpointInfo) throws IOException {
        JettyHTTPDestination destination = new JettyHTTPDestination(bus, this, endpointInfo);
        configure(destination);
        destination.retrieveEngine();        
        return destination;
    }

    public EndpointInfo createEndpointInfo(ServiceInfo serviceInfo, BindingInfo b, Port port) {
        List ees = port.getExtensibilityElements();
        for (Iterator itr = ees.iterator(); itr.hasNext();) {
            Object extensor = itr.next();

            if (extensor instanceof HTTPAddress) {
                HTTPAddress httpAdd = (HTTPAddress)extensor;

                EndpointInfo info = new EndpointInfo(serviceInfo, "http://schemas.xmlsoap.org/wsdl/http/");
                info.setAddress(httpAdd.getLocationURI());
                return info;
            } else if (extensor instanceof AddressType) {
                AddressType httpAdd = (AddressType)extensor;

                EndpointInfo info = new EndpointInfo(serviceInfo, "http://schemas.xmlsoap.org/wsdl/http/");
                info.setAddress(httpAdd.getLocation());
                return info;
            }
        }

        return null;
    }

    public void createPortExtensors(EndpointInfo ei, Service service) {
        // TODO
    }

    public Set<String> getUriPrefixes() {
        return URI_PREFIXES;
    }

    protected void configure(Object bean) {
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(bean);
        }
    }

    protected static URLConnectionFactory getConnectionFactory(SSLClientPolicy policy) {
        return policy == null
               ? new URLConnectionFactory() {
                       public URLConnection createConnection(Proxy proxy, URL u)
                           throws IOException {
                           return proxy != null 
                                  ? u.openConnection(proxy)
                                  : u.openConnection();
                       }
                   }
               : new HttpsURLConnectionFactory(policy);
    }
    
    protected static JettyConnectorFactory getConnectorFactory(SSLServerPolicy policy) {
        return policy == null
               ? new JettyConnectorFactory() {                     
                   public AbstractConnector createConnector(int port) {
                       SelectChannelConnector result = new SelectChannelConnector();
                       //SocketConnector result = new SocketConnector();
                       result.setPort(port);
                       return result;
                   }
               }
               : new JettySslConnectorFactory(policy);
    }
}