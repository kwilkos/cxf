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

import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.bindings.xformat.XMLBindingMessageFormat;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.BareInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.WrappedInInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.staxutils.DepthXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;

public class XMLMessageInInterceptor
                extends AbstractXMLBindingInterceptor {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(WrappedInInterceptor.class);

    public void handleMessage(Message message) throws Fault {
        XMLStreamReader xsr = message.getContent(XMLStreamReader.class);
        DepthXMLStreamReader dxsr = new DepthXMLStreamReader(xsr);
        Endpoint ep = message.getExchange().get(Endpoint.class);
        BindingInfo service = ep.getEndpointInfo().getBinding();
        boolean result = false;
        for (BindingOperationInfo boi : service.getOperations()) {
            if (!boi.isUnwrappedCapable()) {
                // process bare mode
                MessageInfo mi;
                if (isRequestor(message)) {
                    mi = boi.getOperationInfo().getInput();
                } else {
                    mi = boi.getOperationInfo().getOutput();
                }
                if (mi.getMessageParts().size() != 1) {
                    // Part more than 1, needs a root, which should match the
                    // root defined in wsdl binding
                    // Retrive the root value from input message xml infoset
                    if (!StaxUtils.toNextElement(dxsr)) {
                        throw new Fault(
                            new org.apache.cxf.common.i18n.Message("NO_OPERATION_ELEMENT", BUNDLE));
                    }
                    String rootMsg = dxsr.getLocalName();
                    String rootInModel = ((XMLBindingMessageFormat) mi
                                    .getExtensor(XMLBindingMessageFormat.class)).getRootNode();
                    if (rootInModel != null && rootInModel.equals(rootMsg)) {
                        message.getExchange().put(BindingOperationInfo.class, boi);
                        new BareInInterceptor().handleMessage(message);
                        result = true;
                        break;
                    }
                } else {
                    // Only one part, no root, using the BareInInterceptor inner
                    // logic to find operation
                    new BareInInterceptor().handleMessage(message);
                    result = true;
                    break;
                }
            } else {
                // process wrap mode
                new WrappedInInterceptor().handleMessage(message);
            }
        }
        if (!result) {
            // fault processing
        }
    }
}
