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


package org.apache.cxf.jaxws.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractTransportFactory;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;

public class ServletTransportFactory extends AbstractTransportFactory
    implements DestinationFactory {

    private Bus bus;    
    private Map<String, ServletDestination> destinations = 
        new ConcurrentHashMap<String, ServletDestination>();
    
    public ServletTransportFactory(Bus b) {
        bus = b;
    }

    public ServletTransportFactory() {
    }
    
    public Bus getBus() {
        return bus;
    }

    @Resource
    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public Destination getDestination(EndpointInfo endpointInfo)
        throws IOException {
        ServletDestination d = destinations.get(endpointInfo.getAddress());
        if (d == null) { 
            d = new ServletDestination(bus, null, endpointInfo);
            destinations.put(endpointInfo.getAddress(), d);
        }
        return d;
    }
    
    public List<ServletDestination> getDestinations() {
        List<ServletDestination> result = new ArrayList<ServletDestination>();
        for (ServletDestination sd : destinations.values()) {
            result.add(sd);
        }
        return result;        
    }
}
