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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.Duration;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.ws.addressing.AddressingPropertiesImpl;
import org.apache.cxf.ws.addressing.JAXWSAConstants;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.apache.cxf.ws.addressing.VersionTransformer;
import org.apache.cxf.ws.addressing.v200408.EndpointReferenceType;
import org.apache.cxf.ws.rm.manager.SourcePolicyType;


/**
 * 
 */
public class Proxy {

    private static final Logger LOG = LogUtils.getL7dLogger(Proxy.class);

    private RMEndpoint reliableEndpoint;
    
    

    public Proxy(RMEndpoint rme) {
        reliableEndpoint = rme;
    }

    RMEndpoint getReliableEndpoint() {
        return reliableEndpoint;
    }

    void acknowledge(DestinationSequence ds) throws IOException {
        OperationInfo oi = reliableEndpoint.getService().getServiceInfo().getInterface()
            .getOperation(RMConstants.getSequenceAckOperationName());
        Map<String, Object> requestContext = new HashMap<String, Object>();
        AddressingPropertiesImpl maps = new AddressingPropertiesImpl();
        maps.setTo(VersionTransformer.convert(ds.getAcksTo()).getAddress());        
        maps.setReplyTo(reliableEndpoint.getTransportDestination().getAddress());
        requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, maps);
        Map<String, Object> context = CastUtils.cast(
            Collections.singletonMap(Client.REQUEST_CONTEXT, requestContext),
            String.class,  Object.class);
        invoke(oi, new Object[] {}, context);
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
        return (CreateSequenceResponseType)invoke(oi, new Object[] {create}, null);
    }
    
    void lastMessage(SourceSequence s) throws IOException {
        // TODO
    }
       
    Object invoke(OperationInfo oi, Object[] params, Map<String, Object> context) {
        LOG.log(Level.INFO, "Invoking out-of-band RM protocol message {0}.", 
                oi == null ? null : oi.getName());
        
        // assuming we are on the client side
        
        RMManager manager = reliableEndpoint.getManager();
        Bus bus = manager.getBus();
        Endpoint endpoint = reliableEndpoint.getEndpoint();
        BindingInfo bi = reliableEndpoint.getBindingInfo();
        
        if (null == reliableEndpoint.getConduit()) {
            LOG.severe("No conduit to available.");
            return null;
        }
        Client client = new RMClient(bus, endpoint, reliableEndpoint.getConduit());
       
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
    
    class RMClient extends ClientImpl {
        RMClient(Bus bus, Endpoint endpoint, Conduit conduit) {
            super(bus, endpoint, conduit);
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
