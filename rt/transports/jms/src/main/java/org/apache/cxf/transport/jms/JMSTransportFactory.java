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
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class JMSTransportFactory implements ConduitInitiator, DestinationFactory  {

    Bus bus;
    
    @Resource
    Collection<String> activationNamespaces;
    
    @PostConstruct
    void registerWithBindingManager() {
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
        return new JMSConduit(bus, targetInfo);
    }

    public Conduit getConduit(EndpointInfo endpointInfo, EndpointReferenceType target) throws IOException {
        return new JMSConduit(bus, endpointInfo, target);
    }

    public Destination getDestination(EndpointInfo endpointInfo) throws IOException {
        //TODO
        return new JMSDestination(bus, this, endpointInfo);
    }
    
    public Bus getBus() {
        return bus;
    }
    
    @Resource
    public void setBus(Bus bus) {
        this.bus = bus;
    }
}
