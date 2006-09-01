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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cxf.bindings.xformat.XMLBindingMessageFormat;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.interceptor.BareOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.WrappedOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;

public class XMLMessageOutInterceptor extends AbstractXMLBindingInterceptor {

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(WrappedOutInterceptor.class);

    public void handleMessage(Message message) throws Fault {

        MessageInfo mi;
        BindingOperationInfo boi = message.getExchange().get(BindingOperationInfo.class);
        if (isRequestor(message)) {
            mi = boi.getOperationInfo().getInput();
        } else {
            mi = boi.getOperationInfo().getOutput();
        }
        String rootInModel = ((XMLBindingMessageFormat) mi.getExtensor(XMLBindingMessageFormat.class))
                        .getRootNode();

        if (boi.isUnwrapped()) {
            if (mi.getMessageParts().size() != 1) {
                if (rootInModel == null) {
                    throw new RuntimeException("Bare style must define the rootNode in this case!");
                }
                XMLStreamWriter xmlWriter = message.getContent(XMLStreamWriter.class);

                MessageInfo messageInfo = message.get(MessageInfo.class);
                QName name = messageInfo.getName();

                try {
                    xmlWriter.writeStartElement(name.getLocalPart(), name.getNamespaceURI());
                    new BareOutInterceptor().handleMessage(message);
                    xmlWriter.writeEndElement();
                } catch (XMLStreamException e) {
                    throw new Fault(new org.apache.cxf.common.i18n.Message("STAX_READ_EXC", BUNDLE, e));
                }

            } else {
                new BareOutInterceptor().handleMessage(message);
            }

        }
    }

}
