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
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.wsdl.Port;
import javax.wsdl.extensions.soap.SOAPAddress;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;
import com.ibm.wsdl.extensions.soap.SOAPBodyImpl;
import com.ibm.wsdl.extensions.soap.SOAPOperationImpl;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.extensions.soap.SoapAddress;
import org.apache.cxf.tools.util.SOAPBindingUtil;
import org.apache.cxf.transport.AbstractTransportFactory;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl11.WSDLEndpointFactory;

public class SoapTransportFactory extends AbstractTransportFactory implements DestinationFactory,
    WSDLEndpointFactory, ConduitInitiator {
    public static final String TRANSPORT_ID = "http://schemas.xmlsoap.org/soap/";
    private Bus bus;

    public SoapTransportFactory() {
        super();
    }

    public Destination getDestination(EndpointInfo ei) throws IOException {
        SoapBindingInfo binding = (SoapBindingInfo)ei.getBinding();
        DestinationFactory destinationFactory;
        try {
            destinationFactory = bus.getExtension(DestinationFactoryManager.class)
                .getDestinationFactory(binding.getTransportURI());

            return destinationFactory.getDestination(ei);
        } catch (BusException e) {
            throw new RuntimeException("Could not find destination factory for transport "
                                       + binding.getTransportURI());
        }
    }

    public void createPortExtensors(EndpointInfo ei, Service service) {
        SoapBindingInfo bi = (SoapBindingInfo)ei.getBinding();
        if (bi.getSoapVersion() instanceof Soap11) {
            createSoap11Extensors(ei, bi);
        }
    }

    private void createSoap11Extensors(EndpointInfo ei, SoapBindingInfo bi) {
        SOAPAddress address = new SOAPAddressImpl();
        address.setLocationURI(ei.getAddress());

        ei.addExtensor(address);

        SOAPBindingImpl sbind = new SOAPBindingImpl();
        sbind.setStyle(bi.getStyle());
        sbind.setTransportURI(bi.getTransportURI());
        bi.addExtensor(sbind);

        for (BindingOperationInfo b : bi.getOperations()) {
            SoapOperationInfo soi = b.getExtensor(SoapOperationInfo.class);

            SOAPOperationImpl op = new SOAPOperationImpl();
            op.setSoapActionURI(soi.getAction());
            op.setStyle(soi.getStyle());

            b.addExtensor(op);
            
            if (b.getInput() != null) {
                SOAPBodyImpl body = new SOAPBodyImpl();
                body.setUse("literal");
                b.getInput().addExtensor(body);
            }
            
            if (b.getOutput() != null) {
                SOAPBodyImpl body = new SOAPBodyImpl();
                body.setUse("literal");
                b.getOutput().addExtensor(body);
            }
        }
    }

    public EndpointInfo createEndpointInfo(ServiceInfo serviceInfo, BindingInfo b, Port port) {
        List ees = port.getExtensibilityElements();
        for (Iterator itr = ees.iterator(); itr.hasNext();) {
            Object extensor = itr.next();

            if (SOAPBindingUtil.isSOAPAddress(extensor)) {
                SoapAddress sa = SOAPBindingUtil.getSoapAddress(extensor);

                SoapBindingInfo sbi = (SoapBindingInfo)b;
                EndpointInfo info = new EndpointInfo(serviceInfo, sbi.getTransportURI());
                info.setAddress(sa.getLocationURI());
                return info;
            }
        }

        return null;
    }
    

    public Conduit getConduit(EndpointInfo ei, EndpointReferenceType target) throws IOException {
        return getConduit(ei);
    }

    public Conduit getConduit(EndpointInfo ei) throws IOException {
        SoapBindingInfo binding = (SoapBindingInfo)ei.getBinding();
        ConduitInitiator conduitInit;
        try {
            conduitInit = bus.getExtension(ConduitInitiatorManager.class)
                .getConduitInitiator(binding.getTransportURI());

            return conduitInit.getConduit(ei);
        } catch (BusException e) {
            throw new RuntimeException("Could not find destination factory for transport "
                                       + binding.getTransportURI());
        }
    }

    public Bus getBus() {
        return bus;
    }

    @Resource
    public void setBus(Bus bus) {
        this.bus = bus;
    }

}
