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

import javax.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.security.SSLServerPolicy;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.http.AbstractHTTPTransportFactory;
import org.apache.cxf.transport.https_jetty.JettySslConnectorFactory;

import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.nio.SelectChannelConnector;


public class JettyHTTPTransportFactory extends AbstractHTTPTransportFactory {
    public JettyHTTPTransportFactory() {
        super();
        
    }
    
    @Resource(name = "bus")
    public void setBus(Bus b) {
        super.setBus(b);
    }

    @Override
    public Destination getDestination(EndpointInfo endpointInfo) throws IOException {
        JettyHTTPDestination destination = new JettyHTTPDestination(getBus(), this, endpointInfo);
        configure(destination);
        destination.retrieveEngine();        
        return destination;
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
