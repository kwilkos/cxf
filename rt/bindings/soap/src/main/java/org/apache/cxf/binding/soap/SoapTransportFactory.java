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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOutput;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.binding.soap.model.SoapHeaderInfo;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.extensions.soap.SoapAddress;
import org.apache.cxf.tools.common.extensions.soap.SoapBinding;
import org.apache.cxf.tools.common.extensions.soap.SoapBody;
import org.apache.cxf.tools.common.extensions.soap.SoapHeader;
import org.apache.cxf.tools.common.extensions.soap.SoapOperation;
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
    private Collection<String> activationNamespaces;
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
        createSoapExtensors(ei, bi, bi.getSoapVersion() instanceof Soap12);
    }

    private void createSoapExtensors(EndpointInfo ei, SoapBindingInfo bi, boolean isSoap12) {
        try {
            // We need to populate the soap extensibilityelement proxy for soap11 and soap12
            ExtensionRegistry extensionRegistry = WSDLFactory.newInstance().newPopulatedExtensionRegistry();
            
            SoapAddress soapAddress = SOAPBindingUtil.createSoapAddress(extensionRegistry, isSoap12);
            String address = ei.getAddress();
            if (address == null) {
                address = "http://localhost:9090";
            }

            soapAddress.setLocationURI(address);
            ei.addExtensor(soapAddress);
            
            SoapBinding soapBinding = SOAPBindingUtil.createSoapBinding(extensionRegistry, isSoap12);
            soapBinding.setStyle(bi.getStyle());
            soapBinding.setTransportURI(bi.getTransportURI());
            bi.addExtensor(soapBinding);

            for (BindingOperationInfo b : bi.getOperations()) {
                SoapOperationInfo soi = b.getExtensor(SoapOperationInfo.class);
                
                SoapOperation soapOperation = SOAPBindingUtil.createSoapOperation(extensionRegistry,
                                                                                  isSoap12);
                soapOperation.setSoapActionURI(soi.getAction());
                soapOperation.setStyle(soi.getStyle());

                b.addExtensor(soapOperation);

                if (b.getInput() != null) {
                    List<String> bodyParts = new ArrayList<String>();
                    SoapHeaderInfo headerInfo = b.getInput().getExtensor(SoapHeaderInfo.class);
                    if (headerInfo != null) {
                        SoapHeader soapHeader = SOAPBindingUtil.createSoapHeader(extensionRegistry,
                                                                                 BindingInput.class,
                                                                                 isSoap12);
                        soapHeader.setMessage(b.getInput().getMessageInfo().getName());
                        soapHeader.setPart(headerInfo.getPart().getName().getLocalPart());
                        soapHeader.setUse("literal");
                        b.getInput().addExtensor(soapHeader);

                        for (MessagePartInfo part : b.getInput().getMessageParts()) {
                            bodyParts.add(part.getName().getLocalPart());
                        }
                    } 
                    SoapBody body = SOAPBindingUtil.createSoapBody(extensionRegistry,
                                                                   BindingInput.class,
                                                                   isSoap12);
                    body.setUse("literal");

                    if (!StringUtils.isEmpty(bodyParts)) {
                        body.setParts(bodyParts);
                    }

                    b.getInput().addExtensor(body);
                }

                if (b.getOutput() != null) {
                    List<String> bodyParts = new ArrayList<String>();
                    SoapHeaderInfo headerInfo = b.getOutput().getExtensor(SoapHeaderInfo.class);
                    if (headerInfo != null) {
                        SoapHeader soapHeader = SOAPBindingUtil.createSoapHeader(extensionRegistry,
                                                                                 BindingOutput.class,
                                                                                 isSoap12);
                        soapHeader.setMessage(b.getOutput().getMessageInfo().getName());
                        soapHeader.setPart(headerInfo.getPart().getName().getLocalPart());
                        soapHeader.setUse("literal");
                        b.getOutput().addExtensor(soapHeader);

                        for (MessagePartInfo part : b.getOutput().getMessageParts()) {
                            bodyParts.add(part.getName().getLocalPart());
                        }
                    }
                    SoapBody body = SOAPBindingUtil.createSoapBody(extensionRegistry,
                                                                   BindingOutput.class,
                                                                   isSoap12);
                    body.setUse("literal");

                    if (!StringUtils.isEmpty(bodyParts)) {
                        body.setParts(bodyParts);
                    }
                    
                    b.getOutput().addExtensor(body);
                }
            }
            
        } catch (WSDLException e) {
            e.printStackTrace();
        }
    }
    
    public EndpointInfo createEndpointInfo(ServiceInfo serviceInfo, BindingInfo b, Port port) {
        List ees = port.getExtensibilityElements();
        for (Iterator itr = ees.iterator(); itr.hasNext();) {
            Object extensor = itr.next();

            if (SOAPBindingUtil.isSOAPAddress(extensor)) {
                final SoapAddress sa = SOAPBindingUtil.getSoapAddress(extensor);

                SoapBindingInfo sbi = (SoapBindingInfo)b;
                EndpointInfo info = new EndpointInfo(serviceInfo, sbi.getTransportURI()) {
                    public void setAddress(String s) {
                        super.setAddress(s);
                        sa.setLocationURI(s);
                    }
                };
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

    @Resource(name = "bus")
    public void setBus(Bus bus) {
        this.bus = bus;
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

        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
        if (null != dfm) {
            for (String ns : activationNamespaces) {
                dfm.registerDestinationFactory(ns, this);
            }
        }
    }


}
