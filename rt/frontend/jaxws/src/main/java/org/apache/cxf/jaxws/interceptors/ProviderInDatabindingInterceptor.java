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
package org.apache.cxf.jaxws.interceptors;

import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.AbstractInDatabindingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.URIMappingInterceptor;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.staxutils.W3CDOMStreamReader;

public class ProviderInDatabindingInterceptor extends AbstractInDatabindingInterceptor {

    Class type;
    
    public ProviderInDatabindingInterceptor(Class type) {
        super(Phase.UNMARSHAL);
        addAfter(URIMappingInterceptor.class.getName());
        this.type = type;
    }

    public void handleMessage(Message message) throws Fault {
        Exchange ex = message.getExchange();
        Endpoint e = ex.get(Endpoint.class);
        BindingOperationInfo bop = e.getEndpointInfo().getBinding().getOperations().iterator().next();
        ex.put(BindingOperationInfo.class, bop);
        getMessageInfo(message, bop);
        
        List<Object> params = new ArrayList<Object>();
        
        if (isGET(message)) {
            params.add(null);
            message.setContent(Object.class, params);
            return;
        }
        
        Service s = ex.get(Service.class);
        
        if (SOAPMessage.class.equals(type)) {
            SOAPMessage msg = message.getContent(SOAPMessage.class);
            params.add(msg);
        } else {

            XMLStreamReader r = message.getContent(XMLStreamReader.class);
            if (r != null) {
                if (r instanceof W3CDOMStreamReader) {
                    Node nd = ((W3CDOMStreamReader)r).getCurrentElement();
                    DataReader<Node> reader = 
                        s.getDataBinding().createReader(Node.class);
                    Object object = reader.read(null, nd, type);
                    params.add(object);
                } else {
                    DataReader<XMLStreamReader> reader = 
                        s.getDataBinding().createReader(XMLStreamReader.class);
                    
                    Object object = reader.read(null, r, type);
                    params.add(object);
                }
            }
        }
        message.setContent(Object.class, params);

    }

}