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
        super();
        setPhase(Phase.UNMARSHAL);
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
            message.getExchange().put(BindingOperationInfo.class, operation);
            message.getExchange().put(OperationInfo.class, operation.getOperationInfo());

            if (operation == null) {
                // it's doc-lit-bare
                new BareInInterceptor().handleMessage(message);
            }
        } else {
            operation = message.getExchange().get(BindingOperationInfo.class);
        }
        MessageInfo msg;
        DataReader<Message> dr = getMessageDataReader(message);

        if (!isRequestor(message)) {
            msg = operation.getOperationInfo().getInput();
        } else {
            msg = operation.getOperationInfo().getOutput();
        }

        List<Object> parameters = new ArrayList<Object>();

        StaxUtils.nextEvent(xmlReader);
        while (StaxUtils.toNextElement(xmlReader)) {
            QName name = xmlReader.getName();
            int idx = parameters.size();
            MessagePartInfo p = msg.getMessageParts().get(idx);
            if (p == null) {
                throw new SoapFault("Parameter " + xmlReader.getName() + " does not exist!",
                                    ((SoapMessage)message).getVersion().getSender());
            }
            QName elName = new QName(operation.getOperationInfo().getName().getNamespaceURI(), 
                    p.getName().getLocalPart());

            if (!elName.getLocalPart().equals(name.getLocalPart())) {
                String expMessage = "Parameter " + name + " is not equal to the name ["
                                    + elName.getLocalPart() + "] in the servicemodel!";
                throw new SoapFault(expMessage, ((SoapMessage)message).getVersion().getSender());
            }
            Object param = null;
            param = dr.read(p, message);
            if (param != null) {
                parameters.add(param);
            } else {
                throw new RuntimeException(p.getName() + " can not be unmarshalled");
            }
        }
        message.setContent(List.class, parameters);
    }
}
