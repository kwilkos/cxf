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

package org.apache.cxf.ws.rm.impl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.apache.cxf.ws.addressing.v200408.EndpointReferenceType;
import org.apache.cxf.ws.rm.DestinationSequence;

 
/**
 * 
 */
public class Proxy {

    private static final Logger LOG = Logger.getLogger(Proxy.class.getName());

    private RMEndpoint reliableEndpoint;
    private Service service;
    
    Proxy(Bus bus, RMEndpoint rme) {
        reliableEndpoint = rme;
        if (null != bus) {
            buildService(bus);
        }
    }
   
    RMEndpoint getReliableEndpoint() {
        return reliableEndpoint;
    }
    
    Source getSource() {
        return reliableEndpoint.getSource();
    }

    Service getService() {
        return service;
    }
    
    void acknowledge(DestinationSequence ds) throws IOException {
        
    }
    
    void createSequence(org.apache.cxf.ws.addressing.EndpointReferenceType to, 
                        EndpointReferenceType acksTo, 
                        RelatesToType relatesTo) throws IOException {
        service.getServiceInfo();    
    }

    final void buildService(Bus bus) {
        ReflectionServiceFactoryBean serviceFactory = new ReflectionServiceFactoryBean();
        try {
            serviceFactory.setDataBinding(new JAXBDataBinding(SequenceService.class));
        } catch (JAXBException ex) {
            LOG.log(Level.SEVERE, "Failed to build service.", ex);
        }
        serviceFactory.setBus(bus);
        serviceFactory.setServiceClass(SequenceService.class);
        // that's the default: serviceFactory.setWrapped(true);
        service = serviceFactory.create();
    }
}
