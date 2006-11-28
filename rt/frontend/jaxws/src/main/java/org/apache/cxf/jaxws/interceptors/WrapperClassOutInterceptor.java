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

import java.util.Arrays;
import java.util.List;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxb.WrapperHelper;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;

public class WrapperClassOutInterceptor extends AbstractPhaseInterceptor<Message> {
    public WrapperClassOutInterceptor() {
        super();
        setPhase(Phase.PRE_LOGICAL);
    }

    @SuppressWarnings("unchecked")
    public void handleMessage(Message message) throws Fault {
        BindingOperationInfo bop = message.getExchange().get(BindingOperationInfo.class);

        MessageInfo messageInfo = message.get(MessageInfo.class);
        if (messageInfo == null || bop == null || !bop.isUnwrapped()) {
            return;
        }
        
        BindingOperationInfo newbop = bop.getWrappedOperation();
        MessageInfo wrappedMsgInfo;
        if (Boolean.TRUE.equals(message.get(Message.REQUESTOR_ROLE))) {
            wrappedMsgInfo = newbop.getInput().getMessageInfo();
        } else {
            wrappedMsgInfo = newbop.getOutput().getMessageInfo();
        }
             
        Class<?> wrapped = null;
        List<MessagePartInfo> parts = wrappedMsgInfo.getMessageParts();
        if (parts.size() > 0) {
            wrapped = parts.get(0).getTypeClass();
        }

        if (wrapped != null) {
            List<Object> objs = message.getContent(List.class);
            
            try {
                Object wrapperType = wrapped.newInstance();
                int i = 0;
                for (MessagePartInfo p : messageInfo.getMessageParts()) {
                    Object part = objs.get(i);

                    WrapperHelper.setWrappedPart(p.getName().getLocalPart(), wrapperType, part);

                    i++;
                }
                
                objs = Arrays.asList((Object)wrapperType);
                message.setContent(List.class, objs);
            } catch (Exception ex) {
                throw new Fault(ex);
            }
            
            // we've now wrapped the object, so use the wrapped binding op
            message.getExchange().put(BindingOperationInfo.class, newbop);
            message.getExchange().put(OperationInfo.class, newbop.getOperationInfo());
            
            if (messageInfo == bop.getOperationInfo().getInput()) {
                message.put(MessageInfo.class, newbop.getOperationInfo().getInput());
                message.put(BindingMessageInfo.class, newbop.getInput());
            } else if (messageInfo == bop.getOperationInfo().getOutput()) {
                message.put(MessageInfo.class, newbop.getOperationInfo().getOutput());
                message.put(BindingMessageInfo.class, newbop.getOutput());
            }
        }
    }
}
