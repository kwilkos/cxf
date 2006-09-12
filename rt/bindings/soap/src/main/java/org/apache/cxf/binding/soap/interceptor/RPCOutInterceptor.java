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

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.helpers.NSStack;
import org.apache.cxf.interceptor.AbstractOutDatabindingInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceModelUtil;
import org.apache.cxf.staxutils.StaxUtils;

public class RPCOutInterceptor extends AbstractOutDatabindingInterceptor {

    private NSStack nsStack;

    public RPCOutInterceptor() {
        super();
        setPhase(Phase.MARSHAL);
    }

    private void init() {
        nsStack = new NSStack();
        nsStack.push();
    }

    public void handleMessage(Message message) {
        try {
            init();

            BindingOperationInfo operation = (BindingOperationInfo) message.getExchange().get(
                            BindingOperationInfo.class.getName());

            assert operation.getName() != null;

            XMLStreamWriter xmlWriter = getXMLStreamWriter(message);
            DataWriter<Message> dataWriter = getMessageDataWriter(message);

            addOperationNode(message, xmlWriter);

            int countParts = 0;
            List<MessagePartInfo> parts = null;

            if (!isRequestor(message)) {
                parts = operation.getOutput().getMessageInfo().getMessageParts();
            } else {
                parts = operation.getInput().getMessageInfo().getMessageParts();
            }
            countParts = parts.size();

            if (countParts > 0) {
                List<?> objs = (List<?>) message.getContent(List.class);
                Object[] args = objs.toArray();
                Object[] els = parts.toArray();

                if (args.length != els.length) {
                    message.setContent(Exception.class, new RuntimeException(
                                    "The number of arguments is not equal!"));
                }

                for (int idx = 0; idx < countParts; idx++) {
                    Object arg = args[idx];
                    MessagePartInfo part = (MessagePartInfo) els[idx];
                    QName elName = getPartName(part);
                    dataWriter.write(arg, elName, message);
                }
            }
            // Finishing the writing.
            xmlWriter.writeEndElement();            
        } catch (Exception e) {
            e.printStackTrace();
            message.setContent(Exception.class, e);
        }
    }

    protected void addOperationNode(Message message, XMLStreamWriter xmlWriter) throws XMLStreamException {
        String responseSuffix = !isRequestor(message) ? "Response" : "";
        String namespaceURI = ServiceModelUtil.getTargetNamespace(message.getExchange());
        nsStack.add(namespaceURI);
        String prefix = nsStack.getPrefix(namespaceURI);

        String operationName = getOperationName(message) + responseSuffix;

        StaxUtils.writeStartElement(xmlWriter, prefix, operationName, namespaceURI);
        xmlWriter.flush();
    }

    protected XMLStreamWriter getXMLStreamWriter(Message message) {
        return message.getContent(XMLStreamWriter.class);
    }

    private String getOperationName(Message message) {
        BindingOperationInfo boi = (BindingOperationInfo) message.getExchange().get(
                        BindingOperationInfo.class);       
        return boi.getOperationInfo().getName().getLocalPart();
    }

    private QName getPartName(MessagePartInfo part) {
        QName name = part.getElementQName();
        if (name == null) {
            name = part.getTypeQName();
        }
        return new QName(name.getNamespaceURI(), part.getName().getLocalPart());
    }

}
