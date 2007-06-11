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

package org.apache.cxf.binding.soap.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.interceptor.AbstractInDatabindingInterceptor;
import org.apache.cxf.interceptor.BareInInterceptor;
import org.apache.cxf.interceptor.URIMappingInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceModelUtil;
import org.apache.cxf.staxutils.DepthXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;

public class RPCInInterceptor extends AbstractInDatabindingInterceptor {

    private static final Logger LOG = Logger.getLogger(RPCInInterceptor.class.getName());
    
    public RPCInInterceptor() {
        super(Phase.UNMARSHAL);
        addAfter(URIMappingInterceptor.class.getName());
    }

    private BindingOperationInfo getOperation(Message message, QName opName) {
        return ServiceModelUtil.getOperation(message.getExchange(), opName);
    }

    public void handleMessage(Message message) {
        if (isGET(message)) {
            LOG.info("RPCInInterceptor skipped in HTTP GET method");
            return;
        }
        DepthXMLStreamReader xmlReader = getXMLStreamReader(message);

        BindingOperationInfo operation = null;
        if (!StaxUtils.toNextElement(xmlReader)) {
            message.setContent(Exception.class, new RuntimeException("There must be a method name element."));
        }
        String opName = xmlReader.getLocalName();
        if (isRequestor(message) && opName.endsWith("Response")) {
            opName = opName.substring(0, opName.length() - 8);
        }

        if (message.getExchange().get(BindingOperationInfo.class) == null) {
            operation = getOperation(message, new QName(xmlReader.getNamespaceURI(), opName));
            if (operation == null) {
                // it's doc-lit-bare
                new BareInInterceptor().handleMessage(message);
                return;
            } else {
                message.getExchange().put(BindingOperationInfo.class, operation);
                message.getExchange().put(OperationInfo.class, operation.getOperationInfo());
            }
        } else {
            operation = message.getExchange().get(BindingOperationInfo.class);
        }
        MessageInfo msg;
        DataReader<XMLStreamReader> dr = getDataReader(message, XMLStreamReader.class);

        if (!isRequestor(message)) {
            msg = operation.getOperationInfo().getInput();
        } else {
            msg = operation.getOperationInfo().getOutput();
        }

        List<Object> parameters = new ArrayList<Object>();

        StaxUtils.nextEvent(xmlReader);
        while (StaxUtils.toNextElement(xmlReader)) {
            QName name = xmlReader.getName();            
            MessagePartInfo part = null;
            for (MessagePartInfo mpi : msg.getMessageParts()) {
                if (mpi.getName().getLocalPart().equals(name.getLocalPart())) { 
                    part = mpi;
                    break;
                }
            }
            if (part == null) {
                throw new SoapFault("Parameter " + xmlReader.getName() + " does not exist!",
                              ((SoapMessage)message).getVersion().getSender());
            }            
            Object param = dr.read(part, xmlReader);
            parameters.add(param);
        }
        message.setContent(List.class, parameters);
    }
}
