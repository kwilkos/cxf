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
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingOperationInfo;
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

        //StaxUtils.nextEvent(xmlReader);
        while (StaxUtils.toNextElement(xmlReader)) {
            Object o = dr.read(xmlReader);
            
            parameters.add(o);
        }

        // If we didn't know the operation going into this, lets try to figure
        // it out
        if (operation == null) {
            Endpoint ep = exchange.get(Endpoint.class);
            Service service = ep.getService();

            OperationInfo op = findOperation(service.getServiceInfo().getInterface().getOperations(),
                                             parameters);

            for (BindingOperationInfo bop : ep.getEndpointInfo().getBinding().getOperations()) {
                if (bop.getOperationInfo().equals(op)) {
                    exchange.put(BindingOperationInfo.class, bop);
                    exchange.setOneWay(bop.getOutput() == null);
                    break;
                }
            }
        }
        List<Object> newParameters = new ArrayList<Object>();
        for (Iterator iter = parameters.iterator(); iter.hasNext();) {
            Object element = (Object)iter.next();
            if (element instanceof JAXBElement) {
                element = ((JAXBElement)element).getValue();
            }
            newParameters.add(element);
            
        }
        message.setContent(List.class, newParameters);
    }
}
