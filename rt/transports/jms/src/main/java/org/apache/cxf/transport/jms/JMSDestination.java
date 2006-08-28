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

package org.apache.cxf.transport.jms;

import java.io.IOException;
import java.util.logging.Logger;

import javax.wsdl.WSDLException;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

public class JMSDestination implements Destination {
    static final Logger LOG = LogUtils.getL7dLogger(JMSDestination.class);
    final EndpointInfo endpointInfo;
    final EndpointReferenceType reference;
    final ConduitInitiator conduitInitiator;
    final JMSDestinationConfiguration config;
    
    
    public JMSDestination(Bus b,
                          ConduitInitiator ci,
                          EndpointInfo endpointInfo) throws IOException {
        this(b, ci, endpointInfo, new JMSDestinationConfiguration(b, endpointInfo));
    }
    
    public JMSDestination(Bus b,
                          ConduitInitiator ci,
                          EndpointInfo info,
                          JMSDestinationConfiguration cfg) throws IOException {
        endpointInfo = info;
        conduitInitiator = ci;
        config = cfg;        
        reference = new EndpointReferenceType();
        AttributedURIType address = new AttributedURIType();
        address.setValue(config.getAddress());
        reference.setAddress(address);
    }
    
    public EndpointReferenceType getAddress() {       
        return reference;
    }

    public Conduit getBackChannel(Message inMessage, 
                                  Message partialResponse, 
                                  EndpointReferenceType address) throws WSDLException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public void shutdown() {
        // TODO Auto-generated method stub
        
    }

    public void setMessageObserver(MessageObserver observer) {
        // TODO Auto-generated method stub
        
    }

}
