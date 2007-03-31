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
import java.util.logging.Logger;

import org.apache.cxf.attachment.CachedOutputStream;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.transport.AbstractConduit;

public class LocalConduit extends AbstractConduit {

    public static final String IN_CONDUIT = LocalConduit.class.getName() + ".inConduit";
    public static final String RESPONSE_CONDUIT = LocalConduit.class.getName() + ".inConduit";
    public static final String IN_EXCHANGE = LocalConduit.class.getName() + ".inExchange";
    public static final String DIRECT_DISPATCH = LocalConduit.class.getName() + ".directDispatch";

    private static final Logger LOG = LogUtils.getL7dLogger(LocalConduit.class);
    
    private LocalDestination destination;

    public LocalConduit(LocalDestination destination) {
        super(destination.getAddress());
        this.destination = destination;
    }
    
    public void send(final Message message) throws IOException {
        if (Boolean.TRUE.equals(message.get(DIRECT_DISPATCH))) {
            dispatchDirect(message);
        } else {
            dispatchViaPipe(message);
        }
    }

    private void dispatchDirect(Message message) {
        if (destination.getMessageObserver() == null) {
            throw new IllegalStateException("Local destination does not have a MessageObserver on address " 
                                            + destination.getAddress().getAddress().getValue());
        }

        MessageImpl copy = new MessageImpl();
        copy.put(IN_CONDUIT, this);
        copy.setDestination(destination);
        
        // copy all the contents
        copy.putAll(message);
        MessageImpl.copyContent(message, copy);
        copy.remove(Message.REQUESTOR_ROLE);
        
        // Create a new incoming exchange and store the original exchange for the response
        ExchangeImpl ex = new ExchangeImpl();
        ex.setInMessage(copy);
        ex.put(IN_EXCHANGE, message.getExchange());
        ex.setDestination(destination);
        
        destination.getMessageObserver().onMessage(copy);
    }

    private void dispatchViaPipe(final Message message) throws IOException {
        final PipedInputStream stream = new PipedInputStream();
        final LocalConduit conduit = this;
        final Exchange exchange = message.getExchange();
        
        if (destination.getMessageObserver() == null) {
            throw new IllegalStateException("Local destination does not have a MessageObserver on address " 
                                            + destination.getAddress().getAddress().getValue());
        }
        
        final Runnable receiver = new Runnable() {
            public void run() {
                MessageImpl inMsg = new MessageImpl();
                inMsg.setContent(InputStream.class, stream);
                inMsg.setDestination(destination);
                inMsg.put(IN_CONDUIT, conduit);
                
                ExchangeImpl ex = new ExchangeImpl();
                ex.setInMessage(inMsg);
                ex.put(IN_EXCHANGE, exchange);
                destination.getMessageObserver().onMessage(inMsg);
            }
        };

        final AbstractCachedOutputStream outStream = new CachedOutputStream(stream);

        message.setContent(OutputStream.class, outStream);

        // TODO: put on executor
        new Thread(receiver).start();
    }
    
    protected Logger getLogger() {
        return LOG;
    }
}
