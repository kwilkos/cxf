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

import java.util.List;

import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.interceptor.AbstractInDatabindingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;

public class ProviderOutDatabindingInterceptor extends AbstractInDatabindingInterceptor {

    public ProviderOutDatabindingInterceptor() {
        super(Phase.MARSHAL);
    }
    public ProviderOutDatabindingInterceptor(String phase) {
        super(phase);
    }

    public void handleMessage(Message message) throws Fault {
        Service s = message.getExchange().get(Service.class);
        
        XMLStreamWriter xsw = message.getContent(XMLStreamWriter.class);
        
        DataWriter<XMLStreamWriter> writer = 
            s.getDataBinding().createWriter(XMLStreamWriter.class);
        List<?> objs = (List<?>) message.getContent(List.class);
        
        for (Object o : objs) {
            if (o != null) {
                if (o instanceof SOAPMessage) {
                    message.setContent(SOAPMessage.class, o);
                } else {
                    writer.write(o, xsw);
                }
            }
        }
    }

}