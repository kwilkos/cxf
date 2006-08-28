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

import org.apache.cxf.binding.attachment.CachedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.transport.AbstractCachedOutputStream;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class LocalConduit implements Conduit {

    public static final String IN_CONDUIT = LocalConduit.class.getName() + ".inConduit";
    public static final String IN_EXCHANGE = LocalConduit.class.getName() + ".inExchange";

    private LocalDestination destination;
    private MessageObserver observer;

    public LocalConduit(LocalDestination destination) {
        this.destination = destination;
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
        return destination.getAddress();
    }

    public void send(Message message) throws IOException {
        final PipedInputStream stream = new PipedInputStream();
        final LocalConduit conduit = this;
        final Exchange exchange = message.getExchange();
        
        final Runnable receiver = new Runnable() {
            public void run() {
                MessageImpl m = new MessageImpl();
                m.setContent(InputStream.class, stream);
                m.setDestination(destination);
                m.put(IN_CONDUIT, conduit);
                m.put(IN_EXCHANGE, exchange);
                destination.getMessageObserver().onMessage(m);
            }
        };

        final AbstractCachedOutputStream outStream = new CachedOutputStream(stream);

        message.setContent(OutputStream.class, outStream);

        // TODO: put on executor
        new Thread(receiver).start();
    }

    public void setMessageObserver(MessageObserver o) {
        this.observer = o;
    }

    public MessageObserver getMessageObserver() {
        return observer;
    }
}
