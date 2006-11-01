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
import java.util.ResourceBundle;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.Conduit;

/**
 * Takes the Conduit from the exchange and sends the message through it.
 */
public class MessageSenderInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(MessageSenderInterceptor.class);

    public MessageSenderInterceptor() {
        super();
        setPhase(Phase.PREPARE_SEND);
    }

    public void handleMessage(Message message) {
        Exchange exchange = message.getExchange();
        Conduit conduit =
            message.getConduit() != null
            ? message.getConduit()
            : exchange.getConduit() != null
              ? exchange.getConduit()
              : exchange.getOutMessage() != null
                ? OutgoingChainInterceptor.getBackChannelConduit(exchange)
                : null;

        try {
            conduit.send(message);

            if (message.getInterceptorChain().doIntercept(message)) {
                conduit.close(message);
            } else {
                if (message.getContent(Exception.class) != null) {
                    if (message.getContent(Exception.class) instanceof Fault) {
                        throw (Fault)message.getContent(Exception.class);
                    } else {
                        throw new Fault(message.getContent(Exception.class));
                    }
                }
            }
        } catch (IOException ex) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("COULD_NOT_SEND", BUNDLE), ex);
        }
    }
}
