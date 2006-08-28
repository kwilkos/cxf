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

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.Conduit;

/**
 * Takes the Conduit from the exchange and sends the message through it.
 *
 */
public class MessageSenderInterceptor extends AbstractPhaseInterceptor<Message> {

    public MessageSenderInterceptor() {
        super();
        setPhase(Phase.PREPARE_SEND);
    }

    public void handleMessage(Message message) {
        Conduit conduit = message.getConduit();
        if (conduit == null) {
            conduit = message.getExchange().getConduit();
        }
        
        try {
            conduit.send(message);
            
            message.getInterceptorChain().doIntercept(message);
            
            conduit.close(message);
        } catch (IOException ex) {
            // TODO: wrap in runtime exception
            ex.printStackTrace();
        }
    }
}
