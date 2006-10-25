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
import javax.xml.namespace.QName;

import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.apache.cxf.ws.addressing.v200408.EndpointReferenceType;
import org.apache.cxf.ws.rm.DestinationSequence;
import org.apache.cxf.ws.rm.RMConstants;

/**
 * 
 */
public class Proxy {

    static final QName SERVICE_NAME = 
        new QName(RMConstants.WSRM_NAMESPACE_NAME, "SequenceAbstractService");
    static final QName INTERFACE_NAME = 
         new QName(RMConstants.WSRM_NAMESPACE_NAME, "SequenceAbstractPortType");

    private static final Logger LOG = Logger.getLogger(Proxy.class.getName());

    private RMEndpoint reliableEndpoint;
    private Service service;

    Proxy(RMEndpoint rme) {
        reliableEndpoint = rme;
        buildService();
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

    void createSequence(org.apache.cxf.ws.addressing.EndpointReferenceType to, EndpointReferenceType acksTo,
                        RelatesToType relatesTo) throws IOException {
        OperationInfo oi = service.getServiceInfo().getInterface()
            .getOperation(RMConstants.getCreateSequenceOperationName());
        invokeOneway(oi, null);
    }

    final void buildService() {
        ServiceInfo si = new ServiceInfo();
        si.setName(SERVICE_NAME);
        buildInterfaceInfo(si);
        buildBindingInfo(si);
        service = new ServiceImpl(si);
        DataBinding dataBinding = null;
        try {
            dataBinding = new JAXBDataBinding(SequenceService.class);
        } catch (JAXBException e) {
            throw new ServiceConstructionException(e);
        }

        service.setDataBinding(dataBinding);
    }

    final void buildInterfaceInfo(ServiceInfo si) {
        InterfaceInfo ii = new InterfaceInfo(si, INTERFACE_NAME);
        buildOperationInfo(ii);
    }

    final void buildOperationInfo(InterfaceInfo ii) {
        OperationInfo oi = null;
        MessageInfo mi = null;

        oi = ii.addOperation(RMConstants.getCreateSequenceOperationName());
        mi = oi.createMessage(RMConstants.getCreateSequenceOperationName());
        oi.setInput(mi.getName().getLocalPart(), mi);

        oi = ii.addOperation(RMConstants.getCreateSequenceResponseOperationName());
        mi = oi.createMessage(RMConstants.getCreateSequenceResponseOperationName());
        oi.setInput(mi.getName().getLocalPart(), mi);

        oi = ii.addOperation(RMConstants.getTerminateSequenceOperationName());
        mi = oi.createMessage(RMConstants.getTerminateSequenceOperationName());
        oi.setInput(mi.getName().getLocalPart(), mi);
    }

    final void buildBindingInfo(ServiceInfo si) {
        // use same binding id as for application endpoint
        if (null != reliableEndpoint) {
            String bindingId = reliableEndpoint.getEndpoint().getEndpointInfo().getBinding().getBindingId();
            BindingInfo bi = new BindingInfo(si, bindingId);
            bi.buildOperation(RMConstants.getCreateSequenceOperationName(), "create", null);
            bi.buildOperation(RMConstants.getCreateSequenceResponseOperationName(), "createResponse", null);
            bi.buildOperation(RMConstants.getTerminateSequenceOperationName(), "terminate", null);
            si.addBinding(bi);
        }
    }

    void invokeOneway(OperationInfo oi, Object[] params) {
        LOG
            .log(Level.INFO, "Invoking out-of-band RM protocol message {0}.", oi == null ? null : oi
                .getName());
    }
}
