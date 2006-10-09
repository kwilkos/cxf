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

package org.apache.cxf.binding.soap;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.wsdl.Port;
import javax.wsdl.extensions.soap.SOAPAddress;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.extensions.soap.SoapAddress;
import org.apache.cxf.tools.util.SOAPBindingUtil;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.wsdl11.WSDLEndpointFactory;

public class SoapDestinationFactory implements DestinationFactory, WSDLEndpointFactory {
    private DestinationFactoryManager destinationFactoryManager;

    private Bus bus;
    private Collection<String> activationNamespaces;
    
    public SoapDestinationFactory() {
        super();
    }
    
    public SoapDestinationFactory(DestinationFactoryManager destinationFactoyrManager) {
        super();
        this.destinationFactoryManager = destinationFactoyrManager;
    }

    
    @Resource
    public void setBus(Bus b) {
        bus = b;
    }
    
    @PostConstruct
    void register() {
        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
        this.destinationFactoryManager = dfm;
        if (null != dfm) {
            for (String ns : activationNamespaces) {
                dfm.registerDestinationFactory(ns, this);
            }
        }
    }
    
    public Destination getDestination(EndpointInfo ei) throws IOException {
        SoapBindingInfo binding = (SoapBindingInfo)ei.getBinding();
        DestinationFactory destinationFactory;
        try {
            destinationFactory = destinationFactoryManager.getDestinationFactory(binding.getTransportURI());

            return destinationFactory.getDestination(ei);
        } catch (BusException e) {
            throw new RuntimeException("Could not find destination factory for transport "
                                       + binding.getTransportURI());
        }
    }

    public void createPortExtensors(EndpointInfo ei, Service service) {
        SOAPAddress address = new SOAPAddressImpl();
        address.setLocationURI(ei.getAddress());
        address.setRequired(Boolean.TRUE);

        ei.addExtensor(address);
    }

    public EndpointInfo createEndpointInfo(ServiceInfo serviceInfo, BindingInfo b, Port port) {
        List ees = port.getExtensibilityElements();
        for (Iterator itr = ees.iterator(); itr.hasNext();) {
            Object extensor = itr.next();

            if (SOAPBindingUtil.isSOAPAddress(extensor)) {
                SoapAddress sa = SOAPBindingUtil.getSoapAddress(extensor);

                SoapBindingInfo sbi = (SoapBindingInfo) b;
                EndpointInfo info = new EndpointInfo(serviceInfo, sbi.getTransportURI());
                info.setAddress(sa.getLocationURI());
                return info;
            }
        }

        return null;
    }

    public DestinationFactoryManager getDestinationFactoryManager() {
        return destinationFactoryManager;
    }

    @Resource
    public void setDestinationFactoryManager(DestinationFactoryManager destinationFactoryManager) {
        this.destinationFactoryManager = destinationFactoryManager;
    }

    @Resource
    public void setActivationNamespaces(Collection<String> activationNamespaces) {
        this.activationNamespaces = activationNamespaces;
    }
    
}
