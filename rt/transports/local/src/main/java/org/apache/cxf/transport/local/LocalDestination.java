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

package org.apache.cxf.transport.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractConduit;
import org.apache.cxf.transport.AbstractDestination;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class LocalDestination extends AbstractDestination {

    private static final Logger LOG = LogUtils.getL7dLogger(LocalDestination.class);

    private LocalTransportFactory localDestinationFactory;

    public LocalDestination(LocalTransportFactory localDestinationFactory, EndpointReferenceType epr,
                            EndpointInfo ei) {
        super(epr, ei);
        this.localDestinationFactory = localDestinationFactory;
    }

    public void shutdown() {
        localDestinationFactory.remove(this);
    }

    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected Conduit getInbuiltBackChannel(Message inMessage) {
        Conduit conduit = (Conduit)inMessage.get(LocalConduit.IN_CONDUIT);
        if (conduit instanceof LocalConduit) {
            return new SynchronousConduit((LocalConduit)conduit);
        }
        return null;
    }

    static class SynchronousConduit extends AbstractConduit {
        private LocalConduit conduit;

        public SynchronousConduit(LocalConduit conduit) {
            super(null);
            this.conduit = conduit;
        }

        public void prepare(final Message message) throws IOException {
            final Exchange exchange = (Exchange)message.getExchange().get(LocalConduit.IN_EXCHANGE);
            
            if (Boolean.TRUE.equals(message.get(LocalConduit.DIRECT_DISPATCH))) {
                MessageImpl copy = new MessageImpl();
                copy.putAll(message);
                MessageImpl.copyContent(message, copy);
                
                if (exchange.getInMessage() == null) {
                    exchange.setInMessage(copy);
                }
                
                conduit.getMessageObserver().onMessage(copy);
                return;
            }

            final PipedInputStream stream = new PipedInputStream();
            final Runnable receiver = new Runnable() {
                public void run() {
                    MessageImpl m = new MessageImpl();
                    if (exchange != null) {
                        exchange.setInMessage(m);
                    }
                    m.setContent(InputStream.class, stream);
                    conduit.getMessageObserver().onMessage(m);
                }
            };

            PipedOutputStream outStream = new PipedOutputStream(stream);
            message.setContent(OutputStream.class, outStream);

            new Thread(receiver).start();
        }

        protected Logger getLogger() {
            return LOG;
        }
    }
    
}
