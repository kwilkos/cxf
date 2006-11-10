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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.Duration;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.apache.cxf.ws.addressing.v200408.EndpointReferenceType;
import org.apache.cxf.ws.rm.manager.SourcePolicyType;


/**
 * 
 */
public class Proxy {

    private static final Logger LOG = Logger.getLogger(Proxy.class.getName());

    private RMEndpoint reliableEndpoint;
    
    

    public Proxy(RMEndpoint rme) {
        reliableEndpoint = rme;
    }

    RMEndpoint getReliableEndpoint() {
        return reliableEndpoint;
    }

    void acknowledge(DestinationSequence ds) throws IOException {

    }

    public CreateSequenceResponseType createSequence(org.apache.cxf.ws.addressing.EndpointReferenceType to, 
                        EndpointReferenceType defaultAcksTo,
                        RelatesToType relatesTo) throws IOException {
        
        SourcePolicyType sp = reliableEndpoint.getManager().getSourcePolicy();
        CreateSequenceType create = RMUtils.getWSRMFactory().createCreateSequenceType();

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
        }
        
        OperationInfo oi = reliableEndpoint.getService().getServiceInfo().getInterface()
            .getOperation(RMConstants.getCreateSequenceOperationName());
        Object result = invoke(oi, new Object[] {create});
        LOG.info("result: " + result);
        return (CreateSequenceResponseType)result;
        
    }
    
    void lastMessage(SourceSequence s) throws IOException {
        // TODO
    }
       
    Object invoke(OperationInfo oi, Object[] params) {
        LOG.log(Level.INFO, "Invoking out-of-band RM protocol message {0}.", 
                oi == null ? null : oi.getName());
        LOG.log(Level.INFO, "params: " + params);
        
        // assuming we are on the client side
        
        RMManager manager = reliableEndpoint.getManager();
        Bus bus = manager.getBus();
        Endpoint endpoint = reliableEndpoint.getEndpoint();
        BindingInfo bi = reliableEndpoint.getBindingInfo();
        
                
        Client client = new RMClient(bus, endpoint);
        BindingOperationInfo boi = bi.getOperation(oi);
        try {
            Object[] result = client.invoke(boi, params, null);
            if (result != null && result.length > 0) {
                return result[0];
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    class RMClient extends ClientImpl {
        RMClient(Bus bus, Endpoint endpoint) {
            super(bus, endpoint);
        }

        @Override
        protected PhaseInterceptorChain setupInterceptorChain() {
            Endpoint originalEndpoint = getEndpoint();
            setEndpoint(Proxy.this.reliableEndpoint.getApplicationEndpoint());
            PhaseInterceptorChain chain = super.setupInterceptorChain();
            setEndpoint(originalEndpoint);
            return chain;
        }
        
        
        
        
    }
    

}
