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

import javax.xml.ws.Holder;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;

public class HolderInInterceptor extends AbstractPhaseInterceptor<Message> {

    public static final String CLIENT_HOLDERS = "client.holders";
    
    public HolderInInterceptor() {
        super();
        setPhase(Phase.PRE_INVOKE);
    }

    @SuppressWarnings("unchecked")
    public void handleMessage(Message message) throws Fault {
        List<Object> inObjects = CastUtils.cast(message.getContent(List.class));

        Exchange exchange = message.getExchange();
        
        OperationInfo op = exchange.get(OperationInfo.class);
        if (op == null || !op.hasOutput() || op.getOutput().size() == 0) {
            return;
        }
        
        List<MessagePartInfo> parts = op.getOutput().getMessageParts();
        
        boolean client = Boolean.TRUE.equals(message.get(Message.REQUESTOR_ROLE));
        if (client) {
            int holderIdx = 0;
            int partIdx = 0;
            for (MessagePartInfo part : parts) {
                if (part.getIndex() == -1) {
                    partIdx++;
                    break;
                }
            }
            
            List<Holder> holders = CastUtils.cast((List)exchange.get(CLIENT_HOLDERS));
            for (MessagePartInfo part : parts) {
                int idx = part.getIndex();
                if (idx >= 0) {
                    Holder holder = holders.get(holderIdx);
                    holder.value = inObjects.get(partIdx);
                    holderIdx++;
                    partIdx++;
                }
            }
        } else {
            for (MessagePartInfo part : parts) {
                int idx = part.getIndex();
                if (idx >= 0) {
                    if (part.getProperty(ReflectionServiceFactoryBean.MODE_INOUT) != null) {
                        Object object = inObjects.get(idx);
                        inObjects.set(idx, new Holder<Object>(object));
                    } else {
                        inObjects.add(idx, new Holder());
                    } 
                }
            }
        }
    }
}
