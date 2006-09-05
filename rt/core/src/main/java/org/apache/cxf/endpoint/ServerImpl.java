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

package org.apache.cxf.endpoint;

import java.io.IOException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.MessageObserver;

public class ServerImpl implements Server {
    
    private Destination destination;
    private MessageObserver messageObserver;
    private Endpoint endpoint;

    public ServerImpl(Bus bus, Endpoint endpoint, MessageObserver observer) 
        throws BusException, IOException {
        this.endpoint = endpoint;
        this.messageObserver = observer;

        EndpointInfo ei = endpoint.getEndpointInfo();
        DestinationFactory destinationFactory = bus.getExtension(DestinationFactoryManager.class)
            .getDestinationFactory(ei.getTransportId());
        destination = destinationFactory.getDestination(ei);
    }
    
    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public void start() {
        getDestination().setMessageObserver(messageObserver);
    }

    public void stop() {
        getDestination().setMessageObserver(null);
        
    }

    public MessageObserver getMessageObserver() {
        return messageObserver;
    }

    public void setMessageObserver(MessageObserver messageObserver) {
        this.messageObserver = messageObserver;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }
    
    
}
