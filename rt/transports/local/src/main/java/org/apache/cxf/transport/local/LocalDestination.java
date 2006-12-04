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

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class LocalDestination implements Destination {
    private LocalTransportFactory localDestinationFactory;
    private MessageObserver messageObserver;
    private EndpointReferenceType epr;

    public LocalDestination(LocalTransportFactory localDestinationFactory, EndpointReferenceType epr) {
        super();
        this.localDestinationFactory = localDestinationFactory;
        this.epr = epr;
    }

    public EndpointReferenceType getAddress() {
        return epr;
    }

    public Conduit getBackChannel(Message inMessage, Message partialResponse, EndpointReferenceType address) {
        Conduit conduit = (Conduit)inMessage.get(LocalConduit.IN_CONDUIT);
        if (conduit instanceof LocalConduit) {
            return new SynchronousConduit((LocalConduit)conduit);
        }
        return null;
    }

    public void shutdown() {
        localDestinationFactory.remove(this);
    }

    public void setMessageObserver(MessageObserver observer) {
        this.messageObserver = observer;
    }

    public MessageObserver getMessageObserver() {
        return messageObserver;
    }

    static class SynchronousConduit implements Conduit {
        private LocalConduit conduit;

        public SynchronousConduit(LocalConduit conduit) {
            super();
            this.conduit = conduit;
        }
        public void close(Message msg) throws IOException {
            msg.getContent(OutputStream.class).close();        
        }

        public void close() {
        }

        public Destination getBackChannel() {
            return null;
        }

        public EndpointReferenceType getTarget() {
            return null;
        }

        public void send(final Message message) throws IOException {

            final PipedInputStream stream = new PipedInputStream();
            final Exchange exchange = (Exchange)message.getExchange().get(LocalConduit.IN_EXCHANGE);
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

        public void setMessageObserver(MessageObserver observer) {
        }
    }
}
