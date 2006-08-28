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

import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;

import org.apache.cxf.BusException;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class SoapDestinationFactory implements DestinationFactory {
    private DestinationFactoryManager destinationFactoryManager;
    
    public SoapDestinationFactory(DestinationFactoryManager destinationFactoyrManager) {
        super();
        this.destinationFactoryManager = destinationFactoyrManager;
    }

    public Destination getDestination(EndpointInfo ei) throws IOException {
        EndpointReferenceType epr = new EndpointReferenceType();
        AttributedURIType uri = new AttributedURIType();
        
        // TODO: make non wsdl4j specific
        SOAPAddress add = ei.getExtensor(SOAPAddress.class);
        uri.setValue(add.getLocationURI());
        epr.setAddress(uri);
        
        SoapBindingInfo binding = (SoapBindingInfo) ei.getBinding();
        DestinationFactory destinationFactory;
        try {
            destinationFactory = destinationFactoryManager.getDestinationFactory(binding.getTransportURI());
            
            return destinationFactory.getDestination(ei);
        } catch (BusException e) {
            throw new RuntimeException("Could not find destination factory for transport "
                                       + binding.getTransportURI());
        }
    }

    public Destination getDestination(EndpointReferenceType reference) throws WSDLException, IOException {
        // TODO How do we get actual destination factory??
        throw new UnsupportedOperationException();
    }

    public DestinationFactoryManager getDestinationFactoryManager() {
        return destinationFactoryManager;
    }

    public void setDestinationFactoryManager(DestinationFactoryManager destinationFactoryManager) {
        this.destinationFactoryManager = destinationFactoryManager;
    }
}
