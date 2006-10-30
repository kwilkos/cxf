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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.helpers.NSStack;
import org.apache.cxf.interceptor.AbstractOutDatabindingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceModelUtil;
import org.apache.cxf.staxutils.StaxUtils;

public class RPCOutInterceptor extends AbstractOutDatabindingInterceptor {

    public RPCOutInterceptor() {
        super();
        setPhase(Phase.MARSHAL);
    }


    public void handleMessage(Message message) {
        try {
            NSStack nsStack = new NSStack();
            nsStack.push();

            BindingOperationInfo operation = (BindingOperationInfo) message.getExchange().get(
                            BindingOperationInfo.class.getName());

            assert operation.getName() != null;

            XMLStreamWriter xmlWriter = getXMLStreamWriter(message);
            DataWriter<Message> dataWriter = getMessageDataWriter(message);

            addOperationNode(nsStack, message, xmlWriter);

            int countParts = 0;
            List<MessagePartInfo> parts = null;

            System.out.println("operation " + operation);
            System.out.println("name " + operation.getName());
            System.out.println("output " + operation.getOutput());
            
            if (!isRequestor(message)) {
                parts = operation.getOutput().getMessageInfo().getMessageParts();
            } else {
                parts = operation.getInput().getMessageInfo().getMessageParts();
            }
            countParts = parts.size();

            if (countParts > 0) {
                List<?> objs = (List<?>) message.getContent(List.class);                
                if (objs.size() < parts.size()) {
                    throw new SoapFault("The number of arguments is not equal!", 
                                        ((SoapMessage) message).getVersion().getSender());
                }

                for (int idx = 0; idx < countParts; idx++) {
                    Object arg = objs.get(idx);
                    MessagePartInfo part = (MessagePartInfo) parts.get(idx);

                    dataWriter.write(arg, part, message);
                }
            }
            // Finishing the writing.
            xmlWriter.writeEndElement();            
            message.getInterceptorChain().finishSubChain();
        } catch (XMLStreamException e) {
            throw new Fault(e);
        }
    }

    protected String addOperationNode(NSStack nsStack, Message message, XMLStreamWriter xmlWriter) 
        throws XMLStreamException {
        String responseSuffix = !isRequestor(message) ? "Response" : "";
        String namespaceURI = ServiceModelUtil.getTargetNamespace(message.getExchange());
        nsStack.add(namespaceURI);
        String prefix = nsStack.getPrefix(namespaceURI);

        String operationName = getOperationName(message) + responseSuffix;

        StaxUtils.writeStartElement(xmlWriter, prefix, operationName, namespaceURI);
        return namespaceURI;
    }

    protected XMLStreamWriter getXMLStreamWriter(Message message) {
        return message.getContent(XMLStreamWriter.class);
    }

    private String getOperationName(Message message) {
        BindingOperationInfo boi = (BindingOperationInfo) message.getExchange().get(
                        BindingOperationInfo.class);       
        return boi.getOperationInfo().getName().getLocalPart();
    }

}
