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
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.staxutils.DepthXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;

public class BareInInterceptor extends AbstractInDatabindingInterceptor {

    public BareInInterceptor() {
        super();
        setPhase(Phase.UNMARSHAL);
    }

    public void handleMessage(Message message) {
        DepthXMLStreamReader xmlReader = getXMLStreamReader(message);
        Exchange exchange = message.getExchange();

        BindingOperationInfo operation = exchange.get(BindingOperationInfo.class);

        DataReader<XMLStreamReader> dr = getDataReader(message);
        List<Object> parameters = new ArrayList<Object>();

        List<MessagePartInfo> piList = null;
        if (operation != null) {
            piList = operation.getOperationInfo().getInput().getMessageParts();
        }
        while (StaxUtils.toNextElement(xmlReader)) {
            QName streamParaQName = new QName(xmlReader.getNamespaceURI(), xmlReader.getLocalName());
            Object o = null;
            if (piList != null) {                
                for (MessagePartInfo mpi : piList) {
                    QName paraQName = null;
                    if (mpi.isElement()) {
                        paraQName = mpi.getElementQName();
                    } else {
                        paraQName = mpi.getTypeQName();
                    }
                    if (streamParaQName.equals(paraQName)) {
                        o = dr.read(paraQName, xmlReader);
                        break;
                    }
                }
                if (o == null) {
                    o = dr.read(xmlReader);                    
                }
            } else {
                o = dr.read(xmlReader);
            }
            if (o != null) {
                parameters.add(o);
            }
        }
        
        Endpoint ep = exchange.get(Endpoint.class);
        Service service = ep.getService();

        if (message.get(Element.class) != null) {
            parameters.addAll(abstractParamsFromHeader(message.get(Element.class), ep, message));
        }

        if (operation == null) {            
            // If we didn't know the operation going into this, lets try to
            // figure
            // it out
            OperationInfo op = findOperation(service.getServiceInfo().getInterface().getOperations(),
                            parameters, isRequestor(message));
            for (BindingOperationInfo bop : ep.getEndpointInfo().getBinding().getOperations()) {
                if (bop.getOperationInfo().equals(op)) {
                    operation = bop;
                    exchange.put(BindingOperationInfo.class, bop);
                    exchange.setOneWay(bop.getOutput() == null);
                    break;
                }
            }
        }

        List<Object> newParameters = new ArrayList<Object>();
        for (Iterator iter = parameters.iterator(); iter.hasNext();) {
            Object element = (Object) iter.next();
            if (element instanceof JAXBElement) {
                element = ((JAXBElement) element).getValue();
            }
            newParameters.add(element);

        }

        message.setContent(List.class, newParameters);
    }

    private List<Object> abstractParamsFromHeader(Element headerElement, Endpoint ep, Message message) {

        List<Object> paramInHeader = new ArrayList<Object>();
        List<MessagePartInfo> parts = null;
        for (BindingOperationInfo bop : ep.getEndpointInfo().getBinding().getOperations()) {

            if (isRequestor(message)) {
                parts = bop.getOutput().getMessageInfo().getMessageParts();
            } else {
                parts = bop.getInput().getMessageInfo().getMessageParts();
            }

            for (MessagePartInfo mpi : parts) {
                if (mpi.isInSoapHeader()) {
                    NodeList nodeList = headerElement.getChildNodes();
                    if (nodeList != null) {
                        for (int i = 0; i < nodeList.getLength(); i++) {
                            if (nodeList.item(i).getNamespaceURI().equals(
                                            mpi.getElementQName().getNamespaceURI())
                                            && nodeList.item(i).getLocalName().equals(
                                                            mpi.getElementQName().getLocalPart())) {
                                Element param = (Element) nodeList.item(i);
                                paramInHeader.add(getNodeDataReader(message).read(param));
                            }
                        }

                    }

                }
            }
        }

        return paramInHeader;
    }
}
