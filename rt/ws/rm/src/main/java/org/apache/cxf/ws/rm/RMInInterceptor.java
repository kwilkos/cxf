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

import java.util.Collection;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptor;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AddressingPropertiesImpl;
import org.apache.cxf.ws.addressing.MAPAggregator;

/**
 * 
 */
public class RMInInterceptor extends AbstractRMInterceptor {
    
    private static final Logger LOG = LogUtils.getL7dLogger(RMInInterceptor.class);
    private Set<String> before = Collections.singleton(MAPAggregator.class.getName());
    
    public Set<String> getBefore() {
        return before;
    }

    public Set<String> getAfter() {
        return CastUtils.cast(Collections.EMPTY_SET);
    }

    public String getId() {
        return RMInInterceptor.class.getName();
    }
    
    void handleMessage(Message message, boolean isFault) throws SequenceFault {
        LOG.entering(getClass().getName(), "handleMessage");
        
        RMProperties rmps = RMContextUtils.retrieveRMProperties(message, false);
        
        final AddressingPropertiesImpl maps = RMContextUtils.retrieveMAPs(message, false, false);
        assert null != maps;

        String action = null;
        if (null != maps.getAction()) {
            action = maps.getAction().getValue();
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Action: " + action);
        }
        
        Object originalRequestor = message.get(RMMessageConstants.ORIGINAL_REQUESTOR_ROLE);
        if (null != originalRequestor) {
            LOG.fine("Restoring original requestor role to: " + originalRequestor);
            message.put(Message.REQUESTOR_ROLE, originalRequestor);
        }
        
        // Destination destination = getManager().getDestination(message);
        // RMEndpoint rme = getManager().getReliableEndpoint(message);
        // Servant servant = new Servant(rme);
        
        boolean isServer = RMContextUtils.isServerSide(message);
        LOG.fine("isServerSide: " + isServer);
        
        if (RMConstants.getCreateSequenceAction().equals(action) && !isServer) {
            LOG.fine("Processing inbound CreateSequence on client side.");
            RMEndpoint rme = getManager().getReliableEndpoint(message);
            Servant servant = rme.getServant();
            CreateSequenceResponseType csr = servant.createSequence(message);
            Proxy proxy = rme.getProxy();
            proxy.createSequenceResponse(csr);
            return;
        }


        if (RMConstants.getCreateSequenceAction().equals(action)
            || RMConstants.getCreateSequenceResponseAction().equals(action)
            || RMConstants.getTerminateSequenceAction().equals(action)) {

            InterceptorChain chain = message.getInterceptorChain();
            ListIterator it = chain.getIterator();            
            while (it.hasNext()) {
                PhaseInterceptor pi = (PhaseInterceptor)it.next();
                if ("org.apache.cxf.jaxws.interceptors.WrapperClassInInterceptor".equals(pi.getId())) {
                    chain.remove(pi);
                    LOG.fine("Removed WrapperClassInInterceptor from interceptor chain.");
                    break;
                }
            }

            return;
        } else if (RMConstants.getSequenceAckAction().equals(action)) {
            processAcknowledgments(rmps);
            return;
        }
        
        // for application AND out of band messages
        
        Destination destination = getManager().getDestination(message);

        if (null != rmps) {            

            processAcknowledgments(rmps);
            
            processAcknowledgmentRequests(rmps);  
            
            processSequence(destination, rmps, maps);
            
            processDeliveryAssurance(rmps);
        }
    }
    
    void processAcknowledgments(RMProperties rmps) throws SequenceFault {
        
        Collection<SequenceAcknowledgement> acks = rmps.getAcks();
        if (null != acks) {
            for (SequenceAcknowledgement ack : acks) {
                Identifier id = ack.getIdentifier();
                SourceSequence ss = getManager().getSourceSequence(id);                
                if (null != ss) {
                    ss.setAcknowledged(ack);
                } else {
                    throw (new SequenceFaultFactory()).createUnknownSequenceFault(id);
                }
            }
        }
    }

    void processAcknowledgmentRequests(RMProperties rmps) {
        
    }
    
    void processSequence(Destination destination, RMProperties rmps, AddressingProperties maps) 
        throws SequenceFault {
        SequenceType s = rmps.getSequence();
        if (null == s) {
            return;
        }  

        destination.acknowledge(s, 
            null == maps.getReplyTo() ? null : maps.getReplyTo().getAddress().getValue());
    }
    
    void processDeliveryAssurance(RMProperties rmps) {
        
    }
}
