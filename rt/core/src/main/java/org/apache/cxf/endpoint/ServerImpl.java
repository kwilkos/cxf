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
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.MessageObserver;

public class ServerImpl implements Server {
    
    private static final Logger LOG = Logger.getLogger(ServerImpl.class.getName());
    private Destination destination;
    private MessageObserver messageObserver;
    private Endpoint endpoint;
    private ServerRegistry serverRegistry;
    private Bus bus;
    private ServerLifeCycleManager mgr;
    
    public ServerImpl(Bus bus, Endpoint endpoint, MessageObserver observer) 
        throws BusException, IOException {
        this(bus, endpoint, null, observer);
    }
    
    public ServerImpl(Bus bus, 
                      Endpoint endpoint, 
                      DestinationFactory destinationFactory, 
                      MessageObserver observer) throws BusException, IOException {
        this.endpoint = endpoint;
        this.messageObserver = observer;  
        this.bus = bus;

        EndpointInfo ei = endpoint.getEndpointInfo();
        
        //Treat local transport as a special case, transports loaded by transportId can be replaced
        //by local transport when the publishing address is a local transport protocol. 
        //Of course its not an ideal situation here to use a hard-coded prefix. To be refactored.
        if (destinationFactory == null) {
            if (ei.getAddress() != null && ei.getAddress().indexOf("local://") != -1) {
                destinationFactory = bus.getExtension(DestinationFactoryManager.class)
                    .getDestinationFactoryForUri(ei.getAddress());
            }

            if (destinationFactory == null) {
                destinationFactory = bus.getExtension(DestinationFactoryManager.class)
                    .getDestinationFactory(ei.getTransportId());
            }
        }
            
        destination = destinationFactory.getDestination(ei);
        serverRegistry = bus.getExtension(ServerRegistry.class);
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public void start() {     
        
        getDestination().setMessageObserver(messageObserver);
        //regist the active server to run
        if (null != serverRegistry) {
            LOG.fine("register the server to serverRegistry ");
            serverRegistry.register(this);
        }
        mgr = bus.getExtension(ServerLifeCycleManager.class);
        if (mgr != null) {
            mgr.startServer(this);
        }
    }

    public void stop() {
        LOG.fine("Server is stopping.");
        if (mgr != null) {
            mgr.stopServer(this);
        }
        getDestination().setMessageObserver(null);
        if (null != serverRegistry) {
            LOG.fine("unregister the server to serverRegistry ");
            serverRegistry.unregister(this);
        }
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
