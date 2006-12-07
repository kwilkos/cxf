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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;

public class SoapActionInterceptor extends AbstractSoapInterceptor {
    
    public SoapActionInterceptor() {
        super();
        setPhase(Phase.POST_LOGICAL);
    }
    
    @SuppressWarnings("unchecked")
    public void handleMessage(SoapMessage message) throws Fault {
        // TODO Auto-generated method stub
        if (!(message == message.getExchange().getInMessage())) {
            setSoapAction(message);
        }
    }

    @SuppressWarnings("unchecked")
    private void setSoapAction(SoapMessage message) {
        List<String> value = new ArrayList<String>();
        
        BindingOperationInfo boi = message.getExchange().get(BindingOperationInfo.class);
        if (boi == null) {
            value.add("\"\"");
        } else {
            SoapOperationInfo soi = (SoapOperationInfo) boi.getExtensor(SoapOperationInfo.class);            
            value.add(soi == null ? "\"\"" : soi.getAction() == null ? "\"\"" : soi.getAction());
        }
        Map<String, List<String>> reqHeaders = (Map<String, List<String>>)
            message.get(Message.PROTOCOL_HEADERS);
        if (reqHeaders == null) {
            reqHeaders = new HashMap<String, List<String>>();
            message.put(Message.PROTOCOL_HEADERS, reqHeaders);
        }
        if (message.getVersion() instanceof Soap11 && !reqHeaders.containsKey("SOAPAction")) {            
            reqHeaders.put("SOAPAction", value);            
        } else if (message.getVersion() instanceof Soap12 && !reqHeaders.containsKey("action")) {
            reqHeaders.put("action", value);
        }
    }

}
