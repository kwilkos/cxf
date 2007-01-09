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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.model.SoapHeaderInfo;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.AbstractInDatabindingInterceptor;
import org.apache.cxf.interceptor.BareInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;

/**
 * Perform databinding of the SOAP headers.
 */
public class SoapHeaderInterceptor extends AbstractInDatabindingInterceptor {

    public SoapHeaderInterceptor() {
        super();
        setPhase(Phase.UNMARSHAL);
        addAfter(BareInInterceptor.class.getName());
    }

    public void handleMessage(Message m) throws Fault {
        SoapMessage message = (SoapMessage) m;
        Exchange exchange = message.getExchange();

        List<Object> parameters = CastUtils.cast(message.getContent(List.class));

        if (null == parameters) {
            parameters = new ArrayList<Object>();
        }

        BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
        if (bop.isUnwrapped()) {
            bop = bop.getWrappedOperation();
        }
        
        boolean client = isRequestor(message);
        BindingMessageInfo bmi = client ? bop.getOutput() : bop.getInput();
        if (bmi == null) {
            // one way operation.
            return;
        }
        
        List<SoapHeaderInfo> headers = bmi.getExtensors(SoapHeaderInfo.class);
        if (headers == null) {
            return;
        }
        
        Element headerElement = message.getHeaders(Element.class);
        for (SoapHeaderInfo header : headers) {
            MessagePartInfo mpi = header.getPart();
            Element param = findHeader(headerElement, mpi);
            
            int idx = mpi.getIndex();
            Object object = null;
            if (param != null) {
                object = getNodeDataReader(message).read(mpi, param);
            }
            
            if (client) {
                // Return parameter needs to be first, so bump everything
                // back one notch.
                idx++;
            }
            
            if (idx > parameters.size()) {
                parameters.add(object);
            } else if (idx == -1) {
                parameters.add(0, object);
            } else {
                parameters.add(idx, object);
            }
        }
        if (parameters.size() > 0) {
            message.setContent(List.class, parameters);
        }
    }

    private Element findHeader(Element headerElement, MessagePartInfo mpi) {
        NodeList nodeList = headerElement.getChildNodes();
        Element param = null;
        if (nodeList != null) {
            QName name = mpi.getConcreteName();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node n = nodeList.item(i);
                if (n.getNamespaceURI() != null 
                        && n.getNamespaceURI().equals(name.getNamespaceURI())
                        && n.getLocalName() != null
                        && n.getLocalName().equals(name.getLocalPart())) {
                    param = (Element) n;
                }
            }
        }
        return param;
    }
}
