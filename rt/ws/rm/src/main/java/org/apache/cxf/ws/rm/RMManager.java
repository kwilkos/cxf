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
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AddressingPropertiesImpl;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.apache.cxf.ws.addressing.VersionTransformer;
import org.apache.cxf.ws.addressing.v200408.EndpointReferenceType;
import org.apache.cxf.ws.rm.manager.DeliveryAssuranceType;
import org.apache.cxf.ws.rm.manager.DestinationPolicyType;
import org.apache.cxf.ws.rm.manager.RMManagerConfigBean;
import org.apache.cxf.ws.rm.manager.SourcePolicyType;
import org.apache.cxf.ws.rm.persistence.RMStore;
import org.apache.cxf.ws.rm.policy.RMAssertion;
import org.apache.cxf.ws.rm.policy.RMAssertion.BaseRetransmissionInterval;
import org.apache.cxf.ws.rm.soap.RetransmissionQueueImpl;

/**
 * 
 */
public class RMManager extends RMManagerConfigBean {

    private static final Logger LOG = LogUtils.getL7dLogger(RMManager.class);
    
    private Bus bus;
    private RMStore store;
    private RetransmissionQueue retransmissionQueue;
    private Map<Endpoint, RMEndpoint> reliableEndpoints = new HashMap<Endpoint, RMEndpoint>();
    private Map<String, SourceSequence> sourceSequences;
    private Timer timer = new Timer(true);

    public Bus getBus() {
        return bus;
    }

    @Resource
    public void setBus(Bus b) {
        bus = b;
    }
    
    @PostConstruct
    public void register() {
        if (null != bus) {
            bus.setExtension(this, RMManager.class);
        }
    }

    public RMStore getStore() {
        return store;
    }

    public void setStore(RMStore s) {
        store = s;
    }

    public RetransmissionQueue getRetransmissionQueue() {
        return retransmissionQueue;
    }

    public void setRetransmissionQueue(RetransmissionQueue rq) {
        retransmissionQueue = rq;
    }

    public Timer getTimer() {
        return timer;
    }
    
    public synchronized RMEndpoint getReliableEndpoint(Message message) {
        Endpoint endpoint = message.getExchange().get(Endpoint.class);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Getting RMEndpoint for endpoint with info: " + endpoint.getEndpointInfo().getName());
        }
        if (endpoint.getEndpointInfo().getName().equals(
            new QName(RMConstants.getWsdlNamespace(), "SequenceAbstractSoapPort"))) {
            WrappedEndpoint wrappedEndpoint = (WrappedEndpoint)endpoint;
            endpoint = wrappedEndpoint.getWrappedEndpoint();
        }
        RMEndpoint rme = reliableEndpoints.get(endpoint);
        if (null == rme) {
            rme = new RMEndpoint(this, endpoint);
            org.apache.cxf.transport.Destination destination = message.getExchange().getDestination();
            org.apache.cxf.ws.addressing.EndpointReferenceType replyTo = null;
            if (null != destination) {
                AddressingPropertiesImpl maps = RMContextUtils.retrieveMAPs(message, false, false);
                replyTo = maps.getReplyTo();
            } 
            rme.initialise(message.getExchange().getConduit(message), replyTo);
            reliableEndpoints.put(endpoint, rme); 
            LOG.fine("Created new RMEndpoint.");
        }
        return rme;
    }

    public Destination getDestination(Message message) {
        RMEndpoint rme = getReliableEndpoint(message);
        if (null != rme) {
            return rme.getDestination();
        }
        return null;
    }
    
    public SourceSequence getSourceSequence(Identifier id) {
        return sourceSequences.get(id.getValue());
    }

    public Source getSource(Message message) {
        RMEndpoint rme = getReliableEndpoint(message);
        if (null != rme) {
            return rme.getSource();
        }
        return null;
    }

    public SourceSequence getSequence(Identifier inSeqId, Message message, AddressingProperties maps)
        throws SequenceFault {

        Source source = getSource(message);
        SourceSequence seq = source.getCurrent(inSeqId);
        if (null == seq) {
            // TODO: better error handling
            org.apache.cxf.ws.addressing.EndpointReferenceType to = null;
            boolean isServer = RMContextUtils.isServerSide(message);
            try {
                EndpointReferenceType acksTo = null;
                RelatesToType relatesTo = null;
                if (isServer) {

                    AddressingPropertiesImpl inMaps = RMContextUtils.retrieveMAPs(message, false, false);
                    inMaps.exposeAs(VersionTransformer.Names200408.WSA_NAMESPACE_NAME);
                    acksTo = RMUtils.createReference2004(inMaps.getTo().getValue());
                    to = inMaps.getReplyTo();
                    source.getReliableEndpoint().getServant().setUnattachedIdentifier(inSeqId);
                    relatesTo = (new org.apache.cxf.ws.addressing.ObjectFactory()).createRelatesToType();
                    Destination destination = getDestination(message);
                    DestinationSequence inSeq = inSeqId == null ? null : destination.getSequence(inSeqId);
                    relatesTo.setValue(inSeq != null ? inSeq.getCorrelationID() : null);

                } else {
                    to = RMUtils.createReference(maps.getTo().getValue());
                    acksTo = VersionTransformer.convert(maps.getReplyTo()); 
                    if (RMConstants.getNoneAddress().equals(acksTo.getAddress().getValue())) {
                        org.apache.cxf.transport.Destination dest = message.getExchange()
                            .getConduit(message).getBackChannel();
                        if (null == dest) {
                            acksTo = RMUtils.createAnonymousReference2004();
                        } else {
                            acksTo = VersionTransformer.convert(dest.getAddress());
                        }
                    }
                }

                Proxy proxy = source.getReliableEndpoint().getProxy();
                CreateSequenceResponseType createResponse = 
                    proxy.createSequence(acksTo, relatesTo, isServer);
                if (!isServer) {
                    Servant servant = source.getReliableEndpoint().getServant();
                    servant.createSequenceResponse(createResponse);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            seq = source.awaitCurrent(inSeqId);
            seq.setTarget(to);
        }

        return seq;
    }
    
    @PreDestroy
    public void shutdown() {
        // shutdown retransmission queue
        if (null != retransmissionQueue) {
            retransmissionQueue.stop();
        }
        
        // cancel outstanding timer tasks (deferred acknowledgements) for all destination sequences
        for (RMEndpoint rme : reliableEndpoints.values()) {
            for (DestinationSequence ds : rme.getDestination().getAllSequences()) {
                ds.cancelDeferredAcknowledgments();
            }
        }
        
        // remove references to timer tasks cancelled above to make them eligible for garbage collection
        timer.purge();
        timer.cancel();
    }

    @PostConstruct
    void initialise() {
        if (!isSetRMAssertion()) {
            org.apache.cxf.ws.rm.policy.ObjectFactory factory = 
                new org.apache.cxf.ws.rm.policy.ObjectFactory();
            RMAssertion rma = factory.createRMAssertion();
            BaseRetransmissionInterval bri = factory.createRMAssertionBaseRetransmissionInterval();
            bri.setMilliseconds(new BigInteger(RetransmissionQueue.DEFAULT_BASE_RETRANSMISSION_INTERVAL));
            rma.setBaseRetransmissionInterval(bri);
            rma.setExponentialBackoff(factory.createRMAssertionExponentialBackoff());
            setRMAssertion(rma);
        }
        org.apache.cxf.ws.rm.manager.ObjectFactory factory = new org.apache.cxf.ws.rm.manager.ObjectFactory();
        if (!isSetDeliveryAssurance()) {
            DeliveryAssuranceType da = factory.createDeliveryAssuranceType();
            da.setAtLeastOnce(factory.createDeliveryAssuranceTypeAtLeastOnce());
            setDeliveryAssurance(da);
        }
        if (!isSetSourcePolicy()) {
            SourcePolicyType sp = factory.createSourcePolicyType();
            setSourcePolicy(sp);
            
        }
        if (!getSourcePolicy().isSetSequenceTerminationPolicy()) {
            getSourcePolicy().setSequenceTerminationPolicy(
                factory.createSequenceTerminationPolicyType());            
        }
        if (!isSetDestinationPolicy()) {
            DestinationPolicyType dp = factory.createDestinationPolicyType();
            dp.setAcksPolicy(factory.createAcksPolicyType());
            setDestinationPolicy(dp);
        }    
        if (null == retransmissionQueue) {
            retransmissionQueue = new RetransmissionQueueImpl(this);
        }
    }
    
    void addSourceSequence(SourceSequence ss) {
        if (null == sourceSequences) {
            sourceSequences = new HashMap<String, SourceSequence>();
        }
        sourceSequences.put(ss.getIdentifier().getValue(), ss);
    }
    
    void removeSourceSequence(Identifier id) {
        if (null != sourceSequences) {
            sourceSequences.remove(id.getValue());
        }
    }  
}
