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

package org.apache.cxf.interceptor;

import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.databinding.DataReaderFactory;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceModelUtil;
import org.apache.cxf.staxutils.DepthXMLStreamReader;

public abstract class AbstractInDatabindingInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final QName XSD_ANY = new QName("http://www.w3.org/2001/XMLSchema", "anyType", "xsd");

    private static final ResourceBundle BUNDLE = BundleUtils
        .getBundle(AbstractInDatabindingInterceptor.class);

    protected boolean isRequestor(Message message) {
        return Boolean.TRUE.equals(message.containsKey(Message.REQUESTOR_ROLE));
    }

    protected DataReader getDataReader(Message message, Class<?> input) {
        Service service = ServiceModelUtil.getService(message.getExchange());
        DataReaderFactory factory = service.getDataBinding().getDataReaderFactory();

        DataReader dataReader = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == input) {
                dataReader = factory.createReader(input);
                break;
            }
        }
        if (dataReader == null) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_DATAREADER", 
                                                                   BUNDLE, service.getName()));
        }
        return dataReader;
    }

    protected DataReader<Message> getMessageDataReader(Message message) {
        Service service = ServiceModelUtil.getService(message.getExchange());
        DataReaderFactory factory = service.getDataBinding().getDataReaderFactory();

        DataReader<Message> dataReader = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == Message.class) {
                dataReader = factory.createReader(Message.class);
                break;
            }
        }
        if (dataReader == null) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_DATAREADER", 
                                                                   BUNDLE, service.getName()));
        }
        return dataReader;
    }

    protected DataReader<XMLStreamReader> getDataReader(Message message) {
        Service service = ServiceModelUtil.getService(message.getExchange());
        DataReaderFactory factory = service.getDataBinding().getDataReaderFactory();

        DataReader<XMLStreamReader> dataReader = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == XMLStreamReader.class) {
                dataReader = factory.createReader(XMLStreamReader.class);
                break;
            }
        }
        if (dataReader == null) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_DATAREADER", 
                                                                   BUNDLE, service.getName()));
        }
        return dataReader;
    }

    protected DataReader<Node> getNodeDataReader(Message message) {
        Service service = ServiceModelUtil.getService(message.getExchange());
        DataReaderFactory factory = service.getDataBinding().getDataReaderFactory();

        DataReader<Node> dataReader = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == Node.class) {
                dataReader = factory.createReader(Node.class);
                break;
            }
        }
        if (dataReader == null) {
            throw new Fault(
                            new org.apache.cxf.common.i18n.Message("NO_DATAREADER", BUNDLE, 
                                                                   service.getName()));
        }
        return dataReader;
    }

    protected DepthXMLStreamReader getXMLStreamReader(Message message) {
        XMLStreamReader xr = message.getContent(XMLStreamReader.class);
        return new DepthXMLStreamReader(xr);
    }

    /**
     * Find the next possible message part in the message. If an operation in
     * the list of operations is no longer a viable match, it will be removed
     * from the Collection.
     * 
     * @param exchange
     * @param operations
     * @param name
     * @param client
     * @param index
     * @return
     */
    protected MessagePartInfo findMessagePart(Exchange exchange, Collection<OperationInfo> operations,
                                              QName name, boolean client, int index) {
        MessagePartInfo lastChoice = null;
        for (Iterator<OperationInfo> itr = operations.iterator(); itr.hasNext();) {
            OperationInfo op = itr.next();

            MessageInfo msgInfo = null;
            if (client) {
                msgInfo = op.getOutput();
            } else {
                msgInfo = op.getInput();
            }

            if (msgInfo == null) {
                itr.remove();
                continue;
            }
            
            Collection bodyParts = msgInfo.getMessageParts();
            if (bodyParts.size() == 0 || bodyParts.size() <= index) {
                itr.remove();
                continue;
            }

            MessagePartInfo p = (MessagePartInfo)msgInfo.getMessageParts().get(index);

            if (name.equals(p.getConcreteName())) {
                return p;
            }

            if (XSD_ANY.equals(p.getTypeQName())) {
                lastChoice = p;
            } else {
                itr.remove();
            }
        }
        return lastChoice;
    }

    protected MessageInfo getMessageInfo(Message message, BindingOperationInfo operation, Exchange ex) {
        return getMessageInfo(message, operation, isRequestor(message));
    }
    
    protected MessageInfo getMessageInfo(Message message, BindingOperationInfo operation, boolean requestor) {
        MessageInfo msgInfo;
        OperationInfo intfOp = operation.getOperationInfo();
        if (requestor) {
            msgInfo = intfOp.getOutput();
            message.put(MessageInfo.class, intfOp.getOutput());
        } else {
            msgInfo = intfOp.getInput();
            message.put(MessageInfo.class, intfOp.getInput());
        }
        return msgInfo;
    }
}
