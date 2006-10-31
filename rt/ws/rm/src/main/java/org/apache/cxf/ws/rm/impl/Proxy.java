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
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.service.model.UnwrappedOperationInfo;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.apache.cxf.ws.addressing.v200408.EndpointReferenceType;
import org.apache.cxf.ws.rm.CreateSequenceResponseType;
import org.apache.cxf.ws.rm.CreateSequenceType;
import org.apache.cxf.ws.rm.DestinationSequence;
import org.apache.cxf.ws.rm.Expires;
import org.apache.cxf.ws.rm.Identifier;
import org.apache.cxf.ws.rm.OfferType;
import org.apache.cxf.ws.rm.RMConstants;
import org.apache.cxf.ws.rm.SequenceFaultType;
import org.apache.cxf.ws.rm.TerminateSequenceType;
import org.apache.cxf.ws.rm.interceptor.SourcePolicyType;

/**
 * 
 */
public class Proxy {

    static final QName SERVICE_NAME = 
        new QName(RMConstants.WSRM_NAMESPACE_NAME, "SequenceAbstractService");
    static final QName INTERFACE_NAME = 
         new QName(RMConstants.WSRM_NAMESPACE_NAME, "SequenceAbstractPortType");
    static final QName BINDING_NAME = 
        new QName(RMConstants.WSRM_NAMESPACE_NAME, "SequenceAbstractSoapBinding");
    static final QName PORT_NAME = 
        new QName(RMConstants.WSRM_NAMESPACE_NAME, "SequenceAbstractSoapPort");

    private static final Logger LOG = Logger.getLogger(Proxy.class.getName());

    private RMEndpoint reliableEndpoint;
    private Service service;
    private BindingInfo bindingInfo;
    private Endpoint endpoint;
    private Bus bus;
    
    // REVISIT assumption there is only a single outstanding offer
    private Identifier offeredIdentifier;

    Proxy(Bus b, RMEndpoint rme) {
        bus = b;
        reliableEndpoint = rme;
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
                        EndpointReferenceType defaultAcksTo,
                        RelatesToType relatesTo) throws IOException {
        
        SourcePolicyType sp = reliableEndpoint.getInterceptor().getSourcePolicy();
        CreateSequenceType create = RMUtils.getWSRMFactory().createCreateSequenceType();

        String address = sp.getAcksTo();
        EndpointReferenceType acksTo = null;
        if (null != address) {
            acksTo = RMUtils.createReference2004(address);
        } else {
            acksTo = defaultAcksTo; 
        }
        create.setAcksTo(acksTo);

        Duration d = sp.getSequenceExpiration();
        if (null != d) {
            Expires expires = RMUtils.getWSRMFactory().createExpires();
            expires.setValue(d);  
            create.setExpires(expires);
        }
        
        if (sp.isIncludeOffer()) {
            OfferType offer = RMUtils.getWSRMFactory().createOfferType();
            d = sp.getOfferedSequenceExpiration();
            if (null != d) {
                Expires expires = RMUtils.getWSRMFactory().createExpires();
                expires.setValue(d);  
                offer.setExpires(expires);
            }
            offer.setIdentifier(reliableEndpoint.getSource().generateSequenceIdentifier());
            create.setOffer(offer);
        }
        
        OperationInfo oi = service.getServiceInfo().getInterface()
            .getOperation(RMConstants.getCreateSequenceOperationName());
        invokeOneway(oi, new Object[] {create});
    }
    
    void lastMessage(SourceSequenceImpl s) throws IOException {
        // TODO
    }
    
    void createService() {
        ServiceInfo si = new ServiceInfo();
        si.setName(SERVICE_NAME);
        buildTypeInfo(si);
        buildInterfaceInfo(si);
        buildBindingInfo(si);
        service = new ServiceImpl(si);
        
        DataBinding dataBinding = null;
        try {
            dataBinding = new JAXBDataBinding(CreateSequenceType.class,
                                              CreateSequenceResponseType.class,
                                              TerminateSequenceType.class,
                                              SequenceFaultType.class);
        } catch (JAXBException e) {
            throw new ServiceConstructionException(e);
        }
        service.setDataBinding(dataBinding);
    }
    
    void initialise() {
        createService();
        createEndpoint();
    }

    void createEndpoint() {
        ServiceInfo si = service.getServiceInfo();
        String transportId = reliableEndpoint.getEndpoint().getEndpointInfo().getTransportId();
        EndpointInfo ei = new EndpointInfo(si, transportId);
        ei.setAddress(reliableEndpoint.getEndpoint().getEndpointInfo().getAddress());
        ei.setName(PORT_NAME);
        ei.setBinding(bindingInfo);
        si.addEndpoint(ei);
    
        try {
            endpoint = new EndpointImpl(bus, service, ei);
        } catch (EndpointException ex) {
            ex.printStackTrace();
        }
    }
    
    void buildTypeInfo(ServiceInfo si) {
        // TODO
    }

    void buildInterfaceInfo(ServiceInfo si) {
        InterfaceInfo ii = new InterfaceInfo(si, INTERFACE_NAME);
        buildOperationInfo(ii);
    }

    void buildOperationInfo(InterfaceInfo ii) {
        OperationInfo oi = null;
        MessagePartInfo pi = null;
        OperationInfo unwrapped = null;
        MessageInfo mi = null;
        MessageInfo unwrappedInput = null;

        oi = ii.addOperation(RMConstants.getCreateSequenceOperationName());
        mi = oi.createMessage(RMConstants.getCreateSequenceOperationName());
        oi.setInput(mi.getName().getLocalPart(), mi);
        pi = mi.addMessagePart("create");
        pi.setElementQName(RMConstants.getCreateSequenceOperationName());
        pi.setElement(true);
        // pi.setXmlSchema(null);
        unwrappedInput = new MessageInfo(oi, mi.getName());
        unwrapped = new UnwrappedOperationInfo(oi);
        oi.setUnwrappedOperation(unwrapped);
        unwrapped.setInput(oi.getInputName(), unwrappedInput);

        oi = ii.addOperation(RMConstants.getCreateSequenceResponseOperationName());
        mi = oi.createMessage(RMConstants.getCreateSequenceResponseOperationName());
        oi.setInput(mi.getName().getLocalPart(), mi);
        pi = mi.addMessagePart("createResponse");
        pi.setElementQName(RMConstants.getCreateSequenceResponseOperationName());
        pi.setElement(true);
        // pi.setXmlSchema(null);
        unwrappedInput = new MessageInfo(oi, mi.getName());
        unwrapped = new UnwrappedOperationInfo(oi);
        oi.setUnwrappedOperation(unwrapped);
        unwrapped.setInput(oi.getInputName(), unwrappedInput);

        oi = ii.addOperation(RMConstants.getTerminateSequenceOperationName());
        mi = oi.createMessage(RMConstants.getTerminateSequenceOperationName());
        oi.setInput(mi.getName().getLocalPart(), mi);
        pi = mi.addMessagePart("createResponse");
        pi.setElementQName(RMConstants.getTerminateSequenceOperationName());
        pi.setElement(true);
        // pi.setXmlSchema(null);
        unwrappedInput = new MessageInfo(oi, mi.getName());
        unwrapped = new UnwrappedOperationInfo(oi);
        oi.setUnwrappedOperation(unwrapped);
        unwrapped.setInput(oi.getInputName(), unwrappedInput);
        
    }

    void buildBindingInfo(ServiceInfo si) {
        // use same binding id as for application endpoint
        if (null != reliableEndpoint) {
            String bindingId = reliableEndpoint.getEndpoint().getEndpointInfo().getBinding().getBindingId();
            SoapBindingInfo bi = new SoapBindingInfo(si, bindingId);
            bi.setName(BINDING_NAME);
            BindingOperationInfo boi = null;
            SoapOperationInfo soi = null;

            boi = bi.buildOperation(RMConstants.getCreateSequenceOperationName(), 
                RMConstants.getCreateSequenceOperationName().getLocalPart(), null);
            soi = new SoapOperationInfo();
            soi.setAction(RMConstants.getCreateSequenceAction());
            boi.addExtensor(soi);
            bi.addOperation(boi);
            
            boi = bi.buildOperation(RMConstants.getCreateSequenceResponseOperationName(), 
                RMConstants.getCreateSequenceResponseOperationName().getLocalPart(), null);
            soi = new SoapOperationInfo();
            soi.setAction(RMConstants.getCreateSequenceResponseAction());
            boi.addExtensor(soi);
            bi.addOperation(boi);

            boi = bi.buildOperation(RMConstants.getTerminateSequenceOperationName(), 
                RMConstants.getTerminateSequenceOperationName().getLocalPart(), null);
            soi = new SoapOperationInfo();
            soi.setAction(RMConstants.getTerminateSequenceAction());
            boi.addExtensor(soi);
            bi.addOperation(boi);

            si.addBinding(bi);
            bindingInfo = bi;
        }
    }

    void invokeOneway(OperationInfo oi, Object[] params) {
        LOG.log(Level.INFO, "Invoking out-of-band RM protocol message {0}.", 
                oi == null ? null : oi.getName());
        LOG.log(Level.INFO, "params: " + params);
        
        // assuming we are on the client side
        
                
        Client client = new ClientImpl(bus, endpoint);
        BindingOperationInfo boi = bindingInfo.getOperation(oi);
        try {
            client.invoke(boi, params, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    protected Identifier getOfferedIdentifier() {
        return offeredIdentifier;    
    }
    
    protected void setOfferedIdentifier(OfferType offer) { 
        if (offer != null) {
            offeredIdentifier = offer.getIdentifier();
        }
    }
}
