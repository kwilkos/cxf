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

package org.apache.cxf.ws.rm;


import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.Duration;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.apache.cxf.ws.addressing.v200408.EndpointReferenceType;
import org.apache.cxf.ws.rm.manager.SourcePolicyType;


/**
 * 
 */
public class Proxy {

    private static final Logger LOG = LogUtils.getL7dLogger(Proxy.class);

    private RMEndpoint reliableEndpoint;
    // REVISIT assumption there is only a single outstanding offer
    private Identifier offeredIdentifier;
    

    public Proxy(RMEndpoint rme) {
        reliableEndpoint = rme;
    }

    RMEndpoint getReliableEndpoint() {
        return reliableEndpoint;
    }

    void acknowledge(DestinationSequence ds) throws IOException {        
        if (RMConstants.getAnonymousAddress().equals(ds.getAcksTo().getAddress().getValue())) {
            LOG.log(Level.WARNING, "STANDALONE_ANON_ACKS_NOT_SUPPORTED");
            return;
        }
        
        OperationInfo oi = reliableEndpoint.getService().getServiceInfo().getInterface()
            .getOperation(RMConstants.getSequenceAckOperationName());
        invoke(oi, new Object[] {}, null);
    }
    
    void terminate(SourceSequence ss) throws IOException {
        OperationInfo oi = reliableEndpoint.getService().getServiceInfo().getInterface()
            .getOperation(RMConstants.getTerminateSequenceOperationName());
        
        TerminateSequenceType ts = RMUtils.getWSRMFactory().createTerminateSequenceType();
        ts.setIdentifier(ss.getIdentifier());
        invoke(oi, new Object[] {ts}, null);
    }
    
    void createSequenceResponse(final CreateSequenceResponseType createResponse) {
        LOG.fine("sending CreateSequenceResponse from client side");
        final OperationInfo oi = reliableEndpoint.getService().getServiceInfo().getInterface()
            .getOperation(RMConstants.getCreateSequenceResponseOnewayOperationName());
        
        // TODO: need to set relatesTo

        invoke(oi, new Object[] {createResponse}, null);
       
    }

    public CreateSequenceResponseType createSequence(
                        EndpointReferenceType defaultAcksTo,
                        RelatesToType relatesTo,
                        boolean isServer) throws IOException {
        
        SourcePolicyType sp = reliableEndpoint.getManager().getSourcePolicy();
        final CreateSequenceType create = RMUtils.getWSRMFactory().createCreateSequenceType();        

        String address = sp.getAcksTo();
        EndpointReferenceType acksTo = null;
        if (null != address) {
            acksTo = RMUtils.createReference2004(address);
        } else {
            acksTo = defaultAcksTo; 
        }
        create.setAcksTo(acksTo);

        Duration d = sp.getSequenceExpiration();
        if (null != d) {
            Expires expires = RMUtils.getWSRMFactory().createExpires();
            expires.setValue(d);  
            create.setExpires(expires);
        }
        
        if (sp.isIncludeOffer()) {
            OfferType offer = RMUtils.getWSRMFactory().createOfferType();
            d = sp.getOfferedSequenceExpiration();
            if (null != d) {
                Expires expires = RMUtils.getWSRMFactory().createExpires();
                expires.setValue(d);  
                offer.setExpires(expires);
            }
            offer.setIdentifier(reliableEndpoint.getSource().generateSequenceIdentifier());
            create.setOffer(offer);
            setOfferedIdentifier(offer);
        }
        
        InterfaceInfo ii = reliableEndpoint.getService().getServiceInfo().getInterface();
        
        final OperationInfo oi = isServer 
            ? ii.getOperation(RMConstants.getCreateSequenceOnewayOperationName())
            : ii.getOperation(RMConstants.getCreateSequenceOperationName());
        
        // tried using separate thread - did not help either
        
        if (isServer) {
            LOG.fine("sending CreateSequenceRequest from server side");
            Runnable r = new Runnable() {
                public void run() {
                    invoke(oi, new Object[] {create}, null);
                }
            };
            reliableEndpoint.getApplicationEndpoint().getExecutor().execute(r);
            return null;
        }
        
        
        return (CreateSequenceResponseType)invoke(oi, new Object[] {create}, null);
    }
    
    void lastMessage(SourceSequence s) throws IOException {
        // TODO
    }
    
    Identifier getOfferedIdentifier() {
        return offeredIdentifier;    
    }
    
    void setOfferedIdentifier(OfferType offer) { 
        if (offer != null) {
            offeredIdentifier = offer.getIdentifier();
        }
    }
       
    Object invoke(OperationInfo oi, Object[] params, Map<String, Object> context) {
        
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Invoking out-of-band RM protocol message {0} on thread "
                    + Thread.currentThread(), 
                    oi == null ? null : oi.getName());
        }
        
        RMManager manager = reliableEndpoint.getManager();
        Bus bus = manager.getBus();
        Endpoint endpoint = reliableEndpoint.getEndpoint();
        BindingInfo bi = reliableEndpoint.getBindingInfo();
        Conduit c = reliableEndpoint.getConduit();
        org.apache.cxf.ws.addressing.EndpointReferenceType replyTo = reliableEndpoint.getReplyTo();
        Client client = createClient(bus, endpoint, c, replyTo);
        
        BindingOperationInfo boi = bi.getOperation(oi);
        try {
            Object[] result = client.invoke(boi, params, context);
            if (result != null && result.length > 0) {
                return result[0];
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    protected Client createClient(Bus bus, Endpoint endpoint, Conduit conduit,
                                  org.apache.cxf.ws.addressing.EndpointReferenceType address) {
        return new RMClient(bus, endpoint, conduit, address);
    }
    
    class RMClient extends ClientImpl {

        org.apache.cxf.ws.addressing.EndpointReferenceType address;

        RMClient(Bus bus, Endpoint endpoint, Conduit conduit,
            org.apache.cxf.ws.addressing.EndpointReferenceType a) {
            super(bus, endpoint, conduit);  
            address = a;
        }

        @Override
        public void onMessage(Message m) {
            // TODO Auto-generated method stub
            m.getExchange().put(Endpoint.class, Proxy.this.reliableEndpoint.getApplicationEndpoint());
            super.onMessage(m);
        }    

        @Override
        public Conduit getConduit() {
            Conduit conduit = null;
            EndpointInfo endpointInfo = endpoint.getEndpointInfo();
            String originalAddress = endpointInfo.getAddress();
            try {
                if (null != address) {
                    endpointInfo.setAddress(address.getAddress().getValue());
                }
                conduit = super.getConduit();
            } finally {
                endpointInfo.setAddress(originalAddress);
            }
            return conduit;
        }
    }
    
    // for test
    
    void setReliableEndpoint(RMEndpoint rme) {
        reliableEndpoint = rme;
    }
    

}
