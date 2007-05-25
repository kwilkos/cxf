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
package org.apache.cxf.transport.http_jetty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.security.SSLServerPolicy;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.http.AbstractHTTPTransportFactory;
import org.apache.cxf.transport.https_jetty.JettySslConnectorFactory;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.nio.SelectChannelConnector;


public class JettyHTTPTransportFactory extends AbstractHTTPTransportFactory {
    Map<String, JettyHTTPDestination> destinations = 
        new HashMap<String, JettyHTTPDestination>();
    
    /**
     * This field contains the JettyHTTPServerEngineFactory.
     * It holds a cache of engines that may be used for particular ports.
     */
    private JettyHTTPServerEngineFactory serverEngineFactory;
    
    public JettyHTTPTransportFactory() {
        super();
    }
    
    @Resource(name = "bus")
    public void setBus(Bus b) {
        super.setBus(b);
        // This cannot be called twice;
        assert serverEngineFactory == null;
        
        serverEngineFactory = new JettyHTTPServerEngineFactory(b);
    }

    /**
     * This method returns the Jetty HTTP Server Engine Factory.
     */
    protected JettyHTTPServerEngineFactory getJettyHTTPServerEngineFactory() {
        return serverEngineFactory;
    }
    
    @Override
    public Destination getDestination(EndpointInfo endpointInfo) throws IOException {
        String addr = endpointInfo.getAddress();
        JettyHTTPDestination destination = destinations.get(addr);
        if (destination == null) {
            destination = createDestination(endpointInfo);
        }
           
        return destination;
    }
    
    private synchronized JettyHTTPDestination createDestination(EndpointInfo endpointInfo) 
        throws IOException {
        // Cached Destinations could potentially use an "https" destination 
        // created by somebody else that will not be able to be reconfigured. 
        // As a result of trying would shutdown the server engine that may
        // be in use.
        
        JettyHTTPDestination destination = destinations.get(endpointInfo.getAddress());
        if (destination == null) {
            destination = new JettyHTTPDestination(getBus(), this, endpointInfo);
            
            destinations.put(endpointInfo.getAddress(), destination);
            
            configure(destination);
            destination.finalizeConfig(); 
        }
        return destination;
    }

    @Deprecated
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
    
    /**
     * This method creates a connector factory. If there are TLS parameters
     * then it creates a TLS enabled one.
     */
    protected static JettyConnectorFactory getConnectorFactory(
            TLSServerParameters tlsParams
    ) {
        return tlsParams == null
               ? new JettyConnectorFactory() {                     
                   public AbstractConnector createConnector(int port) {
                       SelectChannelConnector result = new SelectChannelConnector();
                       //SocketConnector result = new SocketConnector();
                       result.setPort(port);
                       return result;
                   }
               }
               : new JettySslConnectorFactory(tlsParams);
    }
}
