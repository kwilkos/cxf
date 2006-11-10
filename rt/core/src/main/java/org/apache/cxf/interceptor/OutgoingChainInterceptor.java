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

import java.io.IOException;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class OutgoingChainInterceptor extends AbstractPhaseInterceptor<Message> {

    public OutgoingChainInterceptor() {
        super();
        setPhase(Phase.POST_INVOKE);
    }

    public void handleMessage(Message message) {
        Exchange ex = message.getExchange();
        Message out = ex.getOutMessage();
        
        if (out != null) {
            getBackChannelConduit(ex);
            BindingOperationInfo bin = ex.get(BindingOperationInfo.class);
            if (bin != null) {
                out.put(MessageInfo.class, bin.getOperationInfo().getOutput());
                out.put(BindingMessageInfo.class, bin.getOutput());
            }
            out.getInterceptorChain().doIntercept(out);
        }
    }
    
    protected static Conduit getBackChannelConduit(Exchange ex) {
        Conduit conduit = null;
        if (ex.getOutMessage().getConduit() == null
            && ex.getConduit() == null
            && ex.getDestination() != null) {
            try {
                EndpointReferenceType target =
                    ex.get(EndpointReferenceType.class);
                conduit = ex.getDestination().getBackChannel(ex.getInMessage(), null, target);
                ex.setConduit(conduit);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return conduit;
    }
}
