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
import java.util.List;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessagePartInfo;

public class BareOutInterceptor extends AbstractOutDatabindingInterceptor {

    public BareOutInterceptor() {
        super();
        setPhase(Phase.MARSHAL);
    }

    public void handleMessage(Message message) {
        Exchange exchange = message.getExchange();
        BindingOperationInfo operation = (BindingOperationInfo)exchange.get(BindingOperationInfo.class
            .getName());
        
        if (operation == null) {
            return;
        }
        
        DataWriter<Message> dataWriter = getMessageDataWriter(message);

        int countParts = 0;
        List<MessagePartInfo> parts = null;

        if (!isRequestor(message)) {
            if (operation.getOutput() != null) {
                parts = operation.getOutput().getMessageParts();
            } else {
                // partial response to oneway
                return;
            }
        } else {
            parts = operation.getInput().getMessageParts();
        }
        countParts = parts.size();

        if (countParts > 0) {
            List<?> objs = message.getContent(List.class);
            if (objs != null) {
                Object[] args = objs.toArray();
                Object[] els = parts.toArray();

                for (int idx = 0; idx < countParts; idx++) {
                    dataWriter.write(args[idx], (MessagePartInfo)els[idx], message);
                }
            }
        }
        message.getInterceptorChain().finishSubChain();
    }
    
}
