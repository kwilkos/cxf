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

package org.apache.cxf.ws.rm;

import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
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
import org.apache.cxf.ws.addressing.Names;

public class RMEndpoint {
    
    private static final QName SERVICE_NAME = 
        new QName(RMConstants.getNamespace(), "SequenceAbstractService");
    private static final QName INTERFACE_NAME = 
         new QName(RMConstants.getNamespace(), "SequenceAbstractPortType");
    private static final QName BINDING_NAME = 
        new QName(RMConstants.getNamespace(), "SequenceAbstractSoapBinding");
    private static final QName PORT_NAME = 
        new QName(RMConstants.getNamespace(), "SequenceAbstractSoapPort");
        
    private final RMManager manager;
    private final Endpoint applicationEndpoint;
    private org.apache.cxf.ws.addressing.EndpointReferenceType applicationReplyTo;
    private Source source;
    private Destination destination;
    private Service service;
    private Endpoint endpoint;
    private Proxy proxy;
    private Servant servant;
    
    
    public RMEndpoint(RMManager m, Endpoint ae) {
        manager = m;
        applicationEndpoint = ae;
        source = new Source(this);
        destination = new Destination(this);
        proxy = new Proxy(this);
        servant = new Servant(this);
    }
    
    public QName getName() {
        return applicationEndpoint.getEndpointInfo().getName();
    }
    
    /**
     * @return Returns the bus.
     */
    public RMManager getManager() {
        return manager;
    }
      
    /**
     * @return Returns the application endpoint.
     */
    public Endpoint getApplicationEndpoint() {
        return applicationEndpoint;
    }
    
    /**
     * @return Returns the RM protocol endpoint.
     */
    public Endpoint getEndpoint() {
        return endpoint;
    }
    
    /**
     * @return Returns the RM protocol service.
     */
    public Service getService() {
        return service;
    }
    
    /**
     * @return Returns the RM protocol binding info.
     */
    public BindingInfo getBindingInfo() {
        return service.getServiceInfo().getBinding(BINDING_NAME);
    }
    
    /**
     * @return Returns the proxy.
     */
    public Proxy getProxy() {
        return proxy;
    }
    
    /**
     * @return Returns the servant.
     */
    public Servant getServant() {
        return servant;
    }

    /** 
     * @return Returns the destination.
     */
    public Destination getDestination() {
        return destination;
    }
    
    /**
     * @param destination The destination to set.
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }
    
    /** 
     * @return Returns the source.
     */
    public Source getSource() {
        return source;
    }
    
    /**
     * @param source The source to set.
     */
    public void setSource(Source source) {
        this.source = source;
    } 
    
    /**
     * Returns the adress to which to send CreateSequenceResponse, TerminateSequence
     * and LastMessage requests (i.e. the replyTo address for twaoway application
     * messages).
     * 
     * @return the replyToAddress
     */
    org.apache.cxf.ws.addressing.EndpointReferenceType getApplicationReplyTo() {
        return applicationReplyTo;
    }
  
    void initialise(org.apache.cxf.ws.addressing.EndpointReferenceType replyTo) {  
        applicationReplyTo = replyTo;
        createService();
        createEndpoint();
    }
    
    
    void createService() {
        ServiceInfo si = new ServiceInfo();
        si.setName(SERVICE_NAME);
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
        service.setInvoker(servant);
    }

    void createEndpoint() {
        ServiceInfo si = service.getServiceInfo();
        buildBindingInfo(si);
        String transportId = applicationEndpoint.getEndpointInfo().getTransportId();
        EndpointInfo ei = new EndpointInfo(si, transportId);
        ei.setAddress(applicationEndpoint.getEndpointInfo().getAddress());
        ei.setName(PORT_NAME);
        ei.setBinding(si.getBinding(BINDING_NAME));

        // get the addressing extensor from the application endpoint (must exist)
        
        Object ua = getUsingAddressing(applicationEndpoint.getEndpointInfo());
        if (null != ua) {
            ei.addExtensor(ua);
        } 
        
        si.addEndpoint(ei);
    
        try {
            // REVISIT: asmyth
            // Using a JAX-WS endpoint here because the presence of the JAX-WS interceptors
            // on the outbound chain of the partial response to a oneway RM protocol message
            // seems to requires this (in their absence the output stream is flushed twice, 
            // with predictably devastating effect).
            // What we really should do here is on use the same interceptors on the outbound
            // path that would be used by the application endpoint without presuming any knowledge
            // of the applications endpoint's frontend.
            endpoint = new JaxWsEndpointImpl(manager.getBus(), service, ei);
            // endpoint = new JaxWsEndpointImpl(manager.getBus(), service, ei);
        } catch (EndpointException ex) {
            throw new RuntimeException(ex);
        }
        service.setExecutor(applicationEndpoint.getService().getExecutor());
    }

    void buildInterfaceInfo(ServiceInfo si) {
        InterfaceInfo ii = new InterfaceInfo(si, INTERFACE_NAME);
        buildOperationInfo(ii);
    }

    void buildOperationInfo(InterfaceInfo ii) {
        buildCreateSequenceOperationInfo(ii);
        buildTerminateSequenceOperationInfo(ii);
        buildSequenceAckOperationInfo(ii);
    }

    void buildCreateSequenceOperationInfo(InterfaceInfo ii) {
        
        OperationInfo operationInfo = null;
        MessagePartInfo partInfo = null;
        UnwrappedOperationInfo unwrappedOperationInfo = null;
        MessageInfo messageInfo = null;
        MessageInfo unwrappedMessageInfo = null;

        operationInfo = ii.addOperation(RMConstants.getCreateSequenceOperationName());
        messageInfo = operationInfo.createMessage(RMConstants.getCreateSequenceOperationName());
        operationInfo.setInput(messageInfo.getName().getLocalPart(), messageInfo);
        partInfo = messageInfo.addMessagePart("create");
        partInfo.setElementQName(RMConstants.getCreateSequenceOperationName());
        partInfo.setElement(true);
        partInfo.setTypeClass(CreateSequenceType.class);
        unwrappedMessageInfo = new MessageInfo(operationInfo, messageInfo.getName());
        unwrappedOperationInfo = new UnwrappedOperationInfo(operationInfo);
        operationInfo.setUnwrappedOperation(unwrappedOperationInfo);
        unwrappedOperationInfo.setInput(operationInfo.getInputName(), unwrappedMessageInfo);
        partInfo = unwrappedMessageInfo.addMessagePart("create");
        partInfo.setElementQName(RMConstants.getCreateSequenceOperationName());
        partInfo.setElement(true);
        partInfo.setTypeClass(CreateSequenceType.class);
        
        messageInfo = operationInfo.createMessage(RMConstants.getCreateSequenceResponseOperationName());
        operationInfo.setOutput(messageInfo.getName().getLocalPart(), messageInfo);
        partInfo = messageInfo.addMessagePart("createResponse");
        partInfo.setElementQName(RMConstants.getCreateSequenceResponseOperationName());
        partInfo.setElement(true);
        partInfo.setTypeClass(CreateSequenceResponseType.class);
        unwrappedMessageInfo = new MessageInfo(operationInfo, messageInfo.getName());
        unwrappedOperationInfo.setOutput(operationInfo.getOutputName(), unwrappedMessageInfo);
        partInfo = unwrappedMessageInfo.addMessagePart("createResponse");
        partInfo.setElementQName(RMConstants.getCreateSequenceResponseOperationName());
        partInfo.setElement(true);
        partInfo.setTypeClass(CreateSequenceResponseType.class);
        
        operationInfo = ii.addOperation(RMConstants.getCreateSequenceOnewayOperationName());
        messageInfo = operationInfo.createMessage(RMConstants.getCreateSequenceOperationName());
        operationInfo.setInput(messageInfo.getName().getLocalPart(), messageInfo);
        partInfo = messageInfo.addMessagePart("create");
        partInfo.setElementQName(RMConstants.getCreateSequenceOperationName());
        partInfo.setElement(true);
        partInfo.setTypeClass(CreateSequenceType.class);
        unwrappedMessageInfo = new MessageInfo(operationInfo, messageInfo.getName());
        unwrappedOperationInfo = new UnwrappedOperationInfo(operationInfo);
        operationInfo.setUnwrappedOperation(unwrappedOperationInfo);
        unwrappedOperationInfo.setInput(operationInfo.getInputName(), unwrappedMessageInfo);
        partInfo = unwrappedMessageInfo.addMessagePart("create");
        partInfo.setElementQName(RMConstants.getCreateSequenceOperationName());
        partInfo.setElement(true);
        partInfo.setTypeClass(CreateSequenceType.class);
        
        operationInfo = ii.addOperation(RMConstants.getCreateSequenceResponseOnewayOperationName());
        messageInfo = operationInfo.createMessage(RMConstants.getCreateSequenceResponseOperationName());
        operationInfo.setInput(messageInfo.getName().getLocalPart(), messageInfo);
        partInfo = messageInfo.addMessagePart("createResponse");
        partInfo.setElementQName(RMConstants.getCreateSequenceResponseOperationName());
        partInfo.setElement(true);
        partInfo.setTypeClass(CreateSequenceResponseType.class);
        unwrappedMessageInfo = new MessageInfo(operationInfo, messageInfo.getName());
        unwrappedOperationInfo = new UnwrappedOperationInfo(operationInfo);
        operationInfo.setUnwrappedOperation(unwrappedOperationInfo);
        unwrappedOperationInfo.setInput(operationInfo.getInputName(), unwrappedMessageInfo);
        partInfo = unwrappedMessageInfo.addMessagePart("createResponse");
        partInfo.setElementQName(RMConstants.getCreateSequenceResponseOperationName());
        partInfo.setElement(true);
        partInfo.setTypeClass(CreateSequenceResponseType.class);
    }
    
    void buildTerminateSequenceOperationInfo(InterfaceInfo ii) {
        
        OperationInfo operationInfo = null;
        MessagePartInfo partInfo = null;
        UnwrappedOperationInfo unwrappedOperationInfo = null;
        MessageInfo messageInfo = null;
        MessageInfo unwrappedMessageInfo = null;
        
        operationInfo = ii.addOperation(RMConstants.getTerminateSequenceOperationName());
        messageInfo = operationInfo.createMessage(RMConstants.getTerminateSequenceOperationName());
        operationInfo.setInput(messageInfo.getName().getLocalPart(), messageInfo);
        partInfo = messageInfo.addMessagePart("terminate");
        partInfo.setElementQName(RMConstants.getTerminateSequenceOperationName());
        partInfo.setElement(true);
        partInfo.setTypeClass(TerminateSequenceType.class);
        unwrappedMessageInfo = new MessageInfo(operationInfo, messageInfo.getName());
        unwrappedOperationInfo = new UnwrappedOperationInfo(operationInfo);
        operationInfo.setUnwrappedOperation(unwrappedOperationInfo);
        unwrappedOperationInfo.setInput(operationInfo.getInputName(), unwrappedMessageInfo);
        partInfo = unwrappedMessageInfo.addMessagePart("terminate");
        partInfo.setElementQName(RMConstants.getTerminateSequenceOperationName());
        partInfo.setElement(true);
        partInfo.setTypeClass(TerminateSequenceType.class);
    }

    void buildSequenceAckOperationInfo(InterfaceInfo ii) {

        OperationInfo operationInfo = null;
        UnwrappedOperationInfo unwrappedOperationInfo = null;
        MessageInfo messageInfo = null;
        MessageInfo unwrappedMessageInfo = null;

        operationInfo = ii.addOperation(RMConstants.getSequenceAckOperationName());
        messageInfo = operationInfo.createMessage(RMConstants.getSequenceAckOperationName());
        operationInfo.setInput(messageInfo.getName().getLocalPart(), messageInfo);
        unwrappedMessageInfo = new MessageInfo(operationInfo, messageInfo.getName());
        unwrappedOperationInfo = new UnwrappedOperationInfo(operationInfo);
        operationInfo.setUnwrappedOperation(unwrappedOperationInfo);
        unwrappedOperationInfo.setInput(operationInfo.getInputName(), unwrappedMessageInfo);
    }

    void buildBindingInfo(ServiceInfo si) {
        // use same binding id as for application endpoint
        if (null != applicationEndpoint) {
            String bindingId = applicationEndpoint.getEndpointInfo().getBinding().getBindingId();
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
            
            boi = bi.buildOperation(RMConstants.getTerminateSequenceOperationName(), 
                RMConstants.getTerminateSequenceOperationName().getLocalPart(), null);
            soi = new SoapOperationInfo();
            soi.setAction(RMConstants.getTerminateSequenceAction());
            boi.addExtensor(soi);
            bi.addOperation(boi);
            
            boi = bi.buildOperation(RMConstants.getSequenceAckOperationName(), 
                null, null);
            assert null != boi;
            soi = new SoapOperationInfo();
            soi.setAction(RMConstants.getSequenceAckAction());
            boi.addExtensor(soi);
            bi.addOperation(boi);
            
            boi = bi.buildOperation(RMConstants.getCreateSequenceOnewayOperationName(), 
                RMConstants.getCreateSequenceOperationName().getLocalPart(), null);
            soi = new SoapOperationInfo();
            soi.setAction(RMConstants.getCreateSequenceAction());
            boi.addExtensor(soi);
            bi.addOperation(boi);

            boi = bi.buildOperation(RMConstants.getCreateSequenceResponseOnewayOperationName(), 
                RMConstants.getCreateSequenceResponseOperationName().getLocalPart(), null);
            soi = new SoapOperationInfo();
            soi.setAction(RMConstants.getCreateSequenceResponseAction());
            boi.addExtensor(soi);
            bi.addOperation(boi);

            si.addBinding(bi);
        }
    }
    
    Object getUsingAddressing(EndpointInfo endpointInfo) {
        if (null == endpointInfo) {
            return null;
        }
        Object ua = null;
        List<ExtensibilityElement> exts = endpointInfo.getExtensors(ExtensibilityElement.class);
        ua = getUsingAddressing(exts);
        if (null != ua) {
            return ua;
        }
        exts = endpointInfo.getBinding() != null
            ? endpointInfo.getBinding().getExtensors(ExtensibilityElement.class) : null;
        ua = getUsingAddressing(exts);
        if (null != ua) {
            return ua;
        }
        exts = endpointInfo.getService() != null
            ? endpointInfo.getService().getExtensors(ExtensibilityElement.class) : null;
        ua = getUsingAddressing(exts);
        if (null != ua) {
            return ua;
        }
        return ua;        
    }
    
    Object getUsingAddressing(List<ExtensibilityElement> exts) {
        Object ua = null;
        if (exts != null) {
            for (ExtensibilityElement ext : exts) {
                if (Names.WSAW_USING_ADDRESSING_QNAME.equals(ext.getElementType())) {
                    ua = ext;
                }
            }
        }
        return ua;
    }
    
}
