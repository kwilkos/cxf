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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.cxf.binding.soap.HeaderUtil;
import org.apache.cxf.binding.soap.model.SoapHeaderInfo;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.AbstractInDatabindingInterceptor;
import org.apache.cxf.interceptor.BareInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;

public class SoapInPostInterceptor extends AbstractInDatabindingInterceptor {

    public SoapInPostInterceptor() {
        super();
        setPhase(Phase.UNMARSHAL);
        addAfter(BareInInterceptor.class.getName());
    }

    public void handleMessage(Message message) throws Fault {
        Exchange exchange = message.getExchange();

        List<Object> parameters = (List<Object>) message.getContent(List.class);

        Endpoint ep = exchange.get(Endpoint.class);
        Service service = ep.getService();
        BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);

        if (message.get(Element.class) != null) {
            parameters.addAll(abstractParamsFromHeader(message.get(Element.class), ep, message));
        }

        // if we didn't know the operation going into this, find it.
        if (bop == null) {
            for (OperationInfo op : service.getServiceInfo().getInterface().getOperations()) {
                Method method = (Method) op.getExtensor(Method.class);
                if (!isMethodMatch(parameters, method)) {
                    continue;
                }
                bop = ep.getEndpointInfo().getBinding().getOperation(op);
                exchange.put(BindingOperationInfo.class, bop);
                exchange.setOneWay(op.isOneWay());
                break;
            }
            if (bop == null) {
                throw new Fault(new RuntimeException("Can not find target operation"));
            }
        }

        message.setContent(List.class, parameters);
    }

    private boolean isMethodMatch(List params, Method method) {
        Class[] cls = method.getParameterTypes();
        Type[] types = method.getGenericParameterTypes();
        if (params.size() != cls.length) {
            return false;
        }
        for (int i = 0; i < cls.length; i++) {
            Class valueClass = cls[i];
            if (cls[i].getName().equals("javax.xml.ws.Holder")) {
                valueClass = ((ParameterizedType) types[i]).getRawType().getClass();
            }
            if (!params.get(i).getClass().isAssignableFrom(valueClass)) {
                return false;
            }
        }
        return true;
    }
    
    
    private List<Object> abstractParamsFromHeader(Element headerElement, Endpoint ep, Message message) {
        List<Object> paramInHeader = new ArrayList<Object>();
        List<MessagePartInfo> parts = null;
        List<Element> elemInHeader = new ArrayList<Element>();
        for (BindingOperationInfo bop : ep.getEndpointInfo().getBinding().getOperations()) {
            BindingMessageInfo bmi = null;
            if (isRequestor(message)) {
                parts = bop.getOutput().getMessageInfo().getMessageParts();
                bmi = bop.getOutput();
            } else {
                parts = bop.getInput().getMessageInfo().getMessageParts();
                bmi = bop.getInput();
            }
            List<SoapHeaderInfo> headers = bmi.getExtensors(SoapHeaderInfo.class);
            for (MessagePartInfo mpi : parts) {
                if (HeaderUtil.isHeaderParam(headers, mpi)) {
                    NodeList nodeList = headerElement.getChildNodes();
                    if (nodeList != null) {
                        for (int i = 0; i < nodeList.getLength(); i++) {
                            if (nodeList.item(i).getNamespaceURI().equals(
                                    mpi.getElementQName().getNamespaceURI())
                                    && nodeList.item(i).getLocalName().equals(
                                            mpi.getElementQName().getLocalPart())) {
                                Element param = (Element) nodeList.item(i);
                                if (!elemInHeader.contains(param)) {
                                    elemInHeader.add(param);
                                }
                            }
                        }

                    }

                }
            }
        }

        for (Iterator iter = elemInHeader.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            paramInHeader.add(getNodeDataReader(message).read(element));
        }
        return paramInHeader;
    }

}
