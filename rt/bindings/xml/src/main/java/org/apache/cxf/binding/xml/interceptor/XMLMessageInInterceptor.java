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

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.bindings.xformat.XMLBindingMessageFormat;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.AbstractInDatabindingInterceptor;
import org.apache.cxf.interceptor.BareInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.WrappedInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.staxutils.DepthXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;

public class XMLMessageInInterceptor extends AbstractInDatabindingInterceptor {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(WrappedInInterceptor.class);

    public void handleMessage(Message message) throws Fault {

        XMLStreamReader xsr = message.getContent(XMLStreamReader.class);
        DepthXMLStreamReader dxsr = new DepthXMLStreamReader(xsr);
        Endpoint ep = message.getExchange().get(Endpoint.class);
        BindingInfo service = ep.getEndpointInfo().getBinding();
        Map<Class<?>, Object> objMap = new HashMap<Class<?>, Object>();
        // StaxUtils.nextEvent(xmlReader);
        if (!StaxUtils.toNextElement(dxsr)) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_OPERATION_ELEMENT", BUNDLE));
        }
        QName startQName = new QName(dxsr.getNamespaceURI(), dxsr.getLocalName());
        for (BindingOperationInfo boi : service.getOperations()) {
            setObjectMap(boi, message, objMap);
            MessageInfo mi = getObject(MessageInfo.class, objMap);
            QName rootInModel = getObject(QName.class, objMap);
            if (rootInModel != null && rootInModel.equals(startQName)) {
                if (mi.getMessageParts().size() != 1) {
                    // handle multi param in bare mode
                    message.getExchange().put(BindingOperationInfo.class, boi);
                    StaxUtils.nextEvent(dxsr);
                    StaxUtils.toNextElement(dxsr);
                    new BareInInterceptor().handleMessage(message);
                    break;
                } else {
                    if (!boi.isUnwrapped()) {
                        // it's bare with one part and part name equals
                        // operation name (not support yet)
                        if (rootInModel.equals(startQName)) {
                            message.getExchange().put(BindingOperationInfo.class, boi);
                            new BareInInterceptor().handleMessage(message);
                            break;
                        }
                    } else {
                        // processing wrap here
                        message.getExchange().put(BindingOperationInfo.class, boi);
                        new WrappedInInterceptor().handleMessage(message);
                        break;
                    }
                }
            } else {
                // bare with one part and part name not equal operation name,
                // check param match
                if (!boi.isUnwrapped()) {
                    if (mi.getMessageParts().size() != 1) {
                        continue;
                    }
                    if (rootInModel.equals(startQName)) {
                        message.getExchange().put(BindingOperationInfo.class, boi);
                        new BareInInterceptor().handleMessage(message);
                        break;
                    }
                }
            }
        }
    }

    private <T> T getObject(Class<T> cls, Map<Class<?>, Object> objMap) {
        return cls.cast(objMap.get(cls));
    }

    private void setObjectMap(BindingOperationInfo boi, Message message, Map<Class<?>, Object> objMap) {
        MessageInfo mi;
        BindingMessageInfo bmi;
        if (!isRequestor(message)) {
            mi = boi.getOperationInfo().getInput();
            bmi = boi.getInput();
        } else {
            mi = boi.getOperationInfo().getOutput();
            bmi = boi.getOutput();
        }
        QName paramFirst = null;
        if (mi.getMessageParts().size() > 0) {
            MessagePartInfo mpiFirst = mi.getMessagePartByIndex(0);            
            if (mpiFirst.isElement()) {
                paramFirst = mpiFirst.getElementQName();
            } else {
                // currently this has not been suppoerted by JAXBEncoderDecoder
                paramFirst = mpiFirst.getTypeQName();
            }
        }
        QName rootInModel = null;
        Object ext = bmi.getExtensor(XMLBindingMessageFormat.class);
        if (ext instanceof XMLBindingMessageFormat) {
            // it's bare mode method, the root node exist for multi param
            rootInModel = ((XMLBindingMessageFormat) ext).getRootNode();
        } else {
            // its wrap mode or bare-single-param mode, using operation name
            if (mi.getMessageParts().size() == 1) {
                rootInModel = paramFirst;
            } else {
                rootInModel = boi.getName();
            }
        }
        objMap.put(QName.class, rootInModel);
        objMap.put(MessageInfo.class, mi);
    }
}
