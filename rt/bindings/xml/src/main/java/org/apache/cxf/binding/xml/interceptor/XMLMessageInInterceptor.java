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

package org.apache.cxf.binding.xml.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.binding.xml.XMLFault;
import org.apache.cxf.bindings.xformat.XMLBindingMessageFormat;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.AbstractInDatabindingInterceptor;
import org.apache.cxf.interceptor.BareInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.WrappedInInterceptor;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceModelUtil;
import org.apache.cxf.staxutils.DepthXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;

public class XMLMessageInInterceptor extends AbstractInDatabindingInterceptor {
    private static final Logger LOG = Logger.getLogger(XMLMessageInInterceptor.class.getName());
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(XMLMessageInInterceptor.class);
    
    // TODO: this should be part of the chain!!
    private BareInInterceptor bareInterceptor = new BareInInterceptor();
    private WrappedInInterceptor wrappedInterceptor = new WrappedInInterceptor();
    
    public XMLMessageInInterceptor() {
        super();
        setPhase(Phase.UNMARSHAL);
    }

    public void handleMessage(Message message) throws Fault {
        if (isGET(message)) {            
            LOG.info("XMLMessageInInterceptor skipped in HTTP GET method");
            return;
        }
        XMLStreamReader xsr = message.getContent(XMLStreamReader.class);
        
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xsr);
        
        Endpoint ep = message.getExchange().get(Endpoint.class);
        BindingInfo service = ep.getEndpointInfo().getBinding();
        
        if (!StaxUtils.toNextElement(reader)) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_OPERATION_ELEMENT", BUNDLE));
        }
        
        Exchange ex = message.getExchange();
        QName startQName = reader.getName();
        // handling xml fault message
        if (startQName.getLocalPart().equals(XMLFault.XML_FAULT_ROOT)) {            
            message.getInterceptorChain().abort();
            if (ep.getInFaultObserver() != null) {
                ep.getInFaultObserver().onMessage(message);
                return;
            }
        }
        // handling xml normal inbound message
        BindingOperationInfo bop = ex.get(BindingOperationInfo.class);
        MessagePartInfo part = null;
        if (bop == null) {
            part = getPartByRootNode(message, startQName, service, xsr);
            if (part == null) {
                List<OperationInfo> operations = new ArrayList<OperationInfo>();
                operations.addAll(service.getInterface().getOperations());            
                part = findMessagePart(ex, operations, startQName, false , 0);
            }
        } else {
            MessageInfo msgInfo = getMessageInfo(message, bop, ex);
            if (msgInfo.getMessageParts().size() > 0) {
                part = msgInfo.getMessageParts().get(0);
            }
        }

        if (part != null) {
            OperationInfo o = part.getMessageInfo().getOperation();
            // TODO: We already know the op, so we can optimize BareInInterceptor a bit yet
            if (!o.isUnwrappedCapable()) {
                bareInterceptor.handleMessage(message);
                return;
            } else {
                wrappedInterceptor.handleMessage(message);
                return;
            }
        } else {
            QName name = new QName(service.getInterface().getName().getNamespaceURI(), 
                                   "multiParamRootReq");
            if (reader.getName().equals(name)) {
                StaxUtils.nextEvent(reader);
                StaxUtils.toNextElement(reader);
                bareInterceptor.handleMessage(message);
                return;
            } else {
                // Do we have a bare request with no parts?
                bop = ServiceModelUtil.getOperation(ex, reader.getName());
                if (bop != null) {
                    ex.put(BindingOperationInfo.class, bop);
                    getMessageInfo(message, bop, ex);
                    message.setContent(List.class, Collections.EMPTY_LIST);
                    return;
                }
            }
        }
        
        throw new Fault(new org.apache.cxf.common.i18n.Message("REQ_NOT_UNDERSTOOD", BUNDLE, startQName));
    }

    private MessagePartInfo getPartByRootNode(Message message, QName startQName, 
            BindingInfo bi, XMLStreamReader xsr) {        
        for (BindingOperationInfo boi : bi.getOperations()) {
            MessageInfo mi;
            BindingMessageInfo bmi;
            if (!isRequestor(message)) {
                mi = boi.getOperationInfo().getInput();
                bmi = boi.getInput();
            } else {
                mi = boi.getOperationInfo().getOutput();
                bmi = boi.getOutput();
            }
            XMLBindingMessageFormat xmf = bmi.getExtensor(XMLBindingMessageFormat.class);
            if (xmf != null) {
                if (xmf.getRootNode().getLocalPart().equals(startQName.getLocalPart())) {
                    message.getExchange().put(BindingOperationInfo.class, boi);
                    try {
                        xsr.nextTag();
                    } catch (XMLStreamException xse) {
                        throw new Fault(new org.apache.cxf.common.i18n.Message("STAX_READ_EXC", BUNDLE));
                    }
                    if (mi.getMessageParts().size() > 0) {
                        return mi.getMessageParts().get(0);
                    }
                    break;
                }
            }
        }
        return null;
    }
}
