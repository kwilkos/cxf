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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceModelUtil;
import org.apache.cxf.staxutils.DepthXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;

public class WrappedInInterceptor extends AbstractInDatabindingInterceptor {
    public static final String WRAPPER_CLASS = "wrapper.class";
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(WrappedInInterceptor.class);
    
    public WrappedInInterceptor() {
        super();
        setPhase(Phase.UNMARSHAL);
    }

    public void handleMessage(Message message) {
        DepthXMLStreamReader xmlReader = getXMLStreamReader(message);

        // Trying to find the operation name from the XML.
        if (!StaxUtils.toNextElement(xmlReader)) {
            // body may be empty for partial response to decoupled request
            return;
        }
        BindingOperationInfo operation = message.getExchange().get(BindingOperationInfo.class);
        boolean requestor = isRequestor(message);
        
        MessageInfo msgInfo;
        if (operation == null) {
            String opName = xmlReader.getLocalName();
            if (requestor && opName.endsWith("Response")) {
                opName = opName.substring(0, opName.length() - 8);
            }
    
            // TODO: Allow overridden methods.
            operation = ServiceModelUtil.getOperation(message.getExchange(), opName);
            if (operation == null) {
                throw new Fault(new org.apache.cxf.common.i18n.Message("NO_OPERATION", BUNDLE, opName));
            }
            message.getExchange().put(BindingOperationInfo.class, operation);
            message.getExchange().put(OperationInfo.class, operation.getOperationInfo());
            message.getExchange().setOneWay(operation.getOutput() == null);
        }
        if (requestor) {
            msgInfo = operation.getOperationInfo().getOutput();
            message.put(BindingMessageInfo.class, operation.getOutput());            
        } else {
            msgInfo = operation.getOperationInfo().getInput();
            message.put(BindingMessageInfo.class, operation.getInput());
        }
        message.put(MessageInfo.class, msgInfo);
        
        DataReader<Message> dr = getMessageDataReader(message);
        List<Object> objects;
        
        // Determine if there is a wrapper class
        if (operation.isUnwrapped() || operation.isUnwrappedCapable()) {
            objects = new ArrayList<Object>();
            Object wrappedObject = dr.read(message);
            if (wrappedObject instanceof JAXBElement) {
                wrappedObject = ((JAXBElement) wrappedObject).getValue();
            }
            objects.add(wrappedObject);
        } else {
            // Unwrap each part individually if we don't have a wrapper
            objects = new ArrayList<Object>();
            int depth = xmlReader.getDepth();
            
            try {
                while (xmlReader.nextTag() == XMLStreamReader.START_ELEMENT && xmlReader.getDepth() > depth) {
                    objects.add(dr.read(message));
                }
            } catch (XMLStreamException e) {
                throw new Fault(new org.apache.cxf.common.i18n.Message("STAX_READ_EXC", BUNDLE), e);
            }
        }
        
        message.setContent(List.class, objects);
    }

}

