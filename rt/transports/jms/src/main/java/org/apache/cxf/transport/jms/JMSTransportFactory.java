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

package org.apache.cxf.transport.jms;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class JMSTransportFactory implements ConduitInitiator, DestinationFactory  {

    private Bus bus;
    private Collection<String> activationNamespaces;
    
    @Resource
    public void setBus(Bus b) {
        bus = b;
    }
    
    public Bus getBus() {
        return bus;
    }
    
    @Resource
    public void setActivationNamespaces(Collection<String> ans) {
        activationNamespaces = ans;
    }
        
    @PostConstruct
    void register() {
        ConduitInitiatorManager cim = bus.getExtension(ConduitInitiatorManager.class);
        for (String ns : activationNamespaces) {
            cim.registerConduitInitiator(ns, this);
        }
        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
        for (String ns : activationNamespaces) {
            dfm.registerDestinationFactory(ns, this);
        }
    }
   
    public Conduit getConduit(EndpointInfo targetInfo) throws IOException {        
        return getConduit(targetInfo, null);
    }

    public Conduit getConduit(EndpointInfo endpointInfo, EndpointReferenceType target) throws IOException {
        Conduit conduit = 
            target == null ? new JMSConduit(bus, endpointInfo) : new JMSConduit(bus, endpointInfo, target);
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(conduit);
        }
        return conduit;
    }

    public Destination getDestination(EndpointInfo endpointInfo) throws IOException {
        JMSDestination destination = new JMSDestination(bus, this, endpointInfo);
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(destination);
        }
        return destination;
    }
   
}
