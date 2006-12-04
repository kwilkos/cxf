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

package org.apache.cxf.jbi.transport;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.messaging.DeliveryChannel;



import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;


public class JBIConduit implements Conduit {
    
    private static final Logger LOG = LogUtils.getL7dLogger(JBIConduit.class);
       
    private MessageObserver incomingObserver;
    private EndpointReferenceType target;
    private DeliveryChannel channel;
           
    
    
    public JBIConduit(EndpointReferenceType target, DeliveryChannel dc) {           
        this.target = target;
        channel = dc;
    }

    public void send(Message message) throws IOException {
        LOG.log(Level.FINE, "JBIConduit send message");
                
        message.setContent(OutputStream.class,
                           new JBIConduitOutputStream(message, channel, target, this));
    }

    public void close(Message message) throws IOException {
        message.getContent(OutputStream.class).close();        
    }

    public EndpointReferenceType getTarget() {
        return target;
    }

    public Destination getBackChannel() {
        return null;
    }

    public void close() {
        
    }

    public void setMessageObserver(MessageObserver observer) {
        incomingObserver = observer;     
    }
    
    public MessageObserver getMessageObserver() {
        return incomingObserver;
    }

    
     
}
