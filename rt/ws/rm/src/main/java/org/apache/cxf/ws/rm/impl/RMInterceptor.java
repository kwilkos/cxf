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

package org.apache.cxf.ws.rm.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptor;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AddressingPropertiesImpl;
import org.apache.cxf.ws.addressing.MAPAggregator;
import org.apache.cxf.ws.addressing.RelatesToType;
import org.apache.cxf.ws.addressing.VersionTransformer;
import org.apache.cxf.ws.addressing.v200408.AttributedURI;
import org.apache.cxf.ws.addressing.v200408.EndpointReferenceType;
import org.apache.cxf.ws.rm.Identifier;
import org.apache.cxf.ws.rm.RMContextUtils;
import org.apache.cxf.ws.rm.RetransmissionQueue;
import org.apache.cxf.ws.rm.SequenceAcknowledgement;
import org.apache.cxf.ws.rm.SequenceFault;
import org.apache.cxf.ws.rm.interceptor.DeliveryAssuranceType;
import org.apache.cxf.ws.rm.interceptor.DestinationPolicyType;
import org.apache.cxf.ws.rm.interceptor.RMInterceptorConfigBean;
import org.apache.cxf.ws.rm.interceptor.SourcePolicyType;
import org.apache.cxf.ws.rm.persistence.RMStore;
import org.apache.cxf.ws.rm.policy.RMAssertion;
import org.apache.cxf.ws.rm.policy.RMAssertion.BaseRetransmissionInterval;

/**
 * Interceptor resposible for implementing exchange of RM protocol messages,
 * aggregating the RM metadata in the application message and processing the 
 * RM metadata contained in incoming application messages.
 * The same interceptor can be used on multiple endpoints.
 *
 */
public class RMInterceptor extends RMInterceptorConfigBean implements PhaseInterceptor<Message> {

    private static final Logger LOG = LogUtils.getL7dLogger(RMInterceptor.class);

    private Bus bus;      
    private RMStore store;
    private RetransmissionQueue retransmissionQueue;

    private Timer timer = new Timer();    
    private Set<String> after = Collections.singleton(MAPAggregator.class.getName());
    private Map<Endpoint, RMEndpoint> reliableEndpoints =
        new HashMap<Endpoint, RMEndpoint>();
       
    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus b) {
        bus = b;
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
    
    // PhaseInterceptor interface

    public Set<String> getAfter() {
        return after;
    }

    public Set<String> getBefore() {
        return CastUtils.cast(Collections.EMPTY_SET);
    }

    public String getId() {
        return RMInterceptor.class.getName();
    }

    public String getPhase() {
        return Phase.PRE_LOGICAL;
    }

    public void handleMessage(Message msg) throws Fault {
        try {
            if (ContextUtils.isOutbound(msg)) {
                handleOutbound(msg, true);
            } else {
                handleInbound(msg, true);
            }
        } catch (SequenceFault ex) {
            LOG.log(Level.SEVERE, "SequenceFault", ex);
            throw new Fault(ex);
        }
    }
    
    public void handleFault(Message msg) {
        try {
            if (ContextUtils.isOutbound(msg)) {
                handleOutbound(msg, true);
            } else {
                handleInbound(msg, true);
            }
        } catch (SequenceFault ex) {
            LOG.log(Level.SEVERE, "SequenceFault", ex);
        }
    }
    
    
    // rm logic
    
    void handleOutbound(Message message, boolean isFault) throws SequenceFault {
        LOG.entering(getClass().getName(), "handleOutbound");
       
        AddressingProperties maps =
            ContextUtils.retrieveMAPs(message, false, true);
        ContextUtils.ensureExposedVersion(maps);
        
        Source source = getSource(message);
        Destination destination = getDestination(message);

        String action = null;
        if (maps != null && null != maps.getAction()) {
            action = maps.getAction().getValue();
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Action: " + action);
        }

        boolean isApplicationMessage = isAplicationMessage(action);
        System.out.println("isApplicationMessage: " + isApplicationMessage);
        
        RMPropertiesImpl rmpsOut = (RMPropertiesImpl)RMContextUtils.retrieveRMProperties(message, true);
        if (null == rmpsOut) {
            rmpsOut = new RMPropertiesImpl();
            RMContextUtils.storeRMProperties(message, rmpsOut, true);
        } else {
            System.out.println("Got existing RMPropertiesImpl");
        }
        
        RMPropertiesImpl rmpsIn = null;
        Identifier inSeqId = null;
        BigInteger inMessageNumber = null;
        
        if (isApplicationMessage) {
                        
            rmpsIn = (RMPropertiesImpl)RMContextUtils.retrieveRMProperties(message, false);
            
            if (null != rmpsIn && null != rmpsIn.getSequence()) {
                inSeqId = rmpsIn.getSequence().getIdentifier();
                inMessageNumber = rmpsIn.getSequence().getMessageNumber();
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("inbound sequence: " + (null == inSeqId ? "null" : inSeqId.getValue()));
            }
            
            // get the current sequence, requesting the creation of a new one if necessary
            
            SourceSequenceImpl seq = getSequence(inSeqId, message, maps);
            assert null != seq;

            // increase message number and store a sequence type object in
            // context

            seq.nextMessageNumber(inSeqId, inMessageNumber);
            rmpsOut.setSequence(seq);

            // if this was the last message in the sequence, reset the
            // current sequence so that a new one will be created next 
            // time the handler is invoked

            if (seq.isLastMessage()) {
                source.setCurrent(null);
            }
        }
        
        // add Acknowledgements (to application messages or explicitly 
        // created Acknowledgement messages only)

        if (isApplicationMessage 
            || RMUtils.getRMConstants().getSequenceAcknowledgmentAction().equals(action)) {
            AttributedURI to = VersionTransformer.convert(maps.getTo());
            assert null != to;
            addAcknowledgements(destination, rmpsOut, inSeqId, to);
        }     
    }
    
    void handleInbound(Message message, boolean isFault) {
        LOG.entering(getClass().getName(), "handleInbound");
    }
    
    @PostConstruct
    void initialise() {
        if (!isSetRMAssertion()) {
            org.apache.cxf.ws.rm.policy.ObjectFactory factory = 
                new org.apache.cxf.ws.rm.policy.ObjectFactory();
            RMAssertion rma = factory.createRMAssertion();
            BaseRetransmissionInterval bri = factory.createRMAssertionBaseRetransmissionInterval();
            bri.setMilliseconds(new BigInteger("3000"));
            rma.setBaseRetransmissionInterval(bri);
            rma.setExponentialBackoff(factory.createRMAssertionExponentialBackoff());
            setRMAssertion(rma);
        }
        org.apache.cxf.ws.rm.interceptor.ObjectFactory factory = 
            new org.apache.cxf.ws.rm.interceptor.ObjectFactory();
        if (!isSetDeliveryAssurance()) {
            DeliveryAssuranceType da = factory.createDeliveryAssuranceType();
            da.setAtLeastOnce(factory.createDeliveryAssuranceTypeAtLeastOnce());
            setDeliveryAssurance(da);
        }
        if (!isSetSourcePolicy()) {
            SourcePolicyType sp = factory.createSourcePolicyType();
            sp.setSequenceTerminationPolicy(factory.createSequenceTerminationPolicyType());
            setSourcePolicy(sp);
        }
        if (!isSetDestinationPolicy()) {
            DestinationPolicyType dp = factory.createDestinationPolicyType();
            dp.setAcksPolicy(factory.createAcksPolicyType());
            setDestinationPolicy(dp);
        }
    }
    
    synchronized RMEndpoint getReliableEndpoint(Message message) {
        Endpoint endpoint = ContextUtils.getEndpoint(message);
        RMEndpoint rme = reliableEndpoints.get(endpoint);
        if (null == rme) {
            rme = new RMEndpoint(this, endpoint);
            reliableEndpoints.put(endpoint, rme);
        }
        return rme;
    }
    
    Destination getDestination(Message message) {
        RMEndpoint rme = getReliableEndpoint(message);
        if (null != rme) {
            return rme.getDestination();
        }
        return null;
    }
    
    Source getSource(Message message) {
        RMEndpoint rme = getReliableEndpoint(message);
        if (null != rme) {
            return rme.getSource();
        }
        return null;
    }
    
    boolean isAplicationMessage(String action) {
        if (RMUtils.getRMConstants().getCreateSequenceAction().equals(action)
            || RMUtils.getRMConstants().getCreateSequenceResponseAction().equals(action)
            || RMUtils.getRMConstants().getTerminateSequenceAction().equals(action)
            || RMUtils.getRMConstants().getLastMessageAction().equals(action)
            || RMUtils.getRMConstants().getSequenceAcknowledgmentAction().equals(action)
            || RMUtils.getRMConstants().getSequenceInfoAction().equals(action)) {
            return false;
        }
        return true;
    }
    
    void addAcknowledgements(Destination destination, 
                             RMPropertiesImpl rmpsOut, 
                             Identifier inSeqId, 
                             AttributedURI to) {

        for (DestinationSequenceImpl seq : destination.getAllSequences()) {
            if (seq.sendAcknowledgement()
                && ((seq.getAcksTo().getAddress().getValue().equals(RMUtils.getAddressingConstants()
                    .getAnonymousURI()) && AbstractSequenceImpl.identifierEquals(seq.getIdentifier(), 
                                                                                inSeqId))
                    || to.getValue().equals(seq.getAcksTo().getAddress().getValue()))) {
                rmpsOut.addAck(seq);
            } else if (LOG.isLoggable(Level.FINE)) {
                if (!seq.sendAcknowledgement()) {
                    LOG.fine("no need to add an acknowledgements for sequence "
                             + seq.getIdentifier().getValue());
                } else {
                    LOG.fine("sequences acksTo (" + seq.getAcksTo().getAddress().getValue()
                             + ") does not match to (" + to.getValue() + ")");
                }
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            Collection<SequenceAcknowledgement> acks = rmpsOut.getAcks();
            if (null == acks) {
                LOG.fine("No acknowledgements added");
            } else {
                LOG.fine("Added " + acks.size() + " acknowledgements.");
            }
        }
    }
    
    SourceSequenceImpl getSequence(Identifier inSeqId, Message message, AddressingProperties maps)
        throws SequenceFault {

        Source source = getSource(message);
        SourceSequenceImpl seq = source.getCurrent(inSeqId);
        if (null == seq) {
            // TODO: better error handling
            org.apache.cxf.ws.addressing.EndpointReferenceType to = null;
            try {
                EndpointReferenceType acksTo = null;
                RelatesToType relatesTo = null;
                if (ContextUtils.isServerSide(message)) {

                    
                    AddressingPropertiesImpl inMaps = ContextUtils.retrieveMAPs(message, false, false);
                    inMaps.exposeAs(VersionTransformer.Names200408.WSA_NAMESPACE_NAME);
                    acksTo = RMUtils.createReference(inMaps.getTo().getValue());
                    to = inMaps.getReplyTo();
                    // getServant().setUnattachedIdentifier(inSeqId);
                    relatesTo = (new org.apache.cxf.ws.addressing.ObjectFactory()).createRelatesToType();
                    Destination destination = getDestination(message);
                    DestinationSequenceImpl inSeq = destination.getSequenceImpl(inSeqId);
                    relatesTo.setValue(inSeq != null ? inSeq.getCorrelationID() : null);

                } else {
                    acksTo = VersionTransformer.convert(maps.getReplyTo());
                    // for oneways
                    if (Names.WSA_NONE_ADDRESS.equals(acksTo.getAddress().getValue())) {
                        acksTo = RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS);
                    }
                }
                
                source.getProxy().createSequence(to, acksTo, relatesTo);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            seq = source.awaitCurrent(inSeqId);
            seq.setTarget(to);
        }

        return seq;
    }



    /*
     * private static final Logger LOG = LogUtils.getL7dLogger(RMHandler.class);
     * private static Map<BindingBase, RMHandler> handlers; private RMSource
     * source; private RMDestination destination; private RMProxy proxy; private
     * RMServant servant; private ConfigurationHelper configurationHelper;
     * private PersistenceManager persistenceManager; private Timer timer;
     * private boolean busLifeCycleListenesRegistered; @Resource(name =
     * JAXWSConstants.BUS_PROPERTY) private Bus bus; @Resource(name =
     * JAXWSConstants.CLIENT_BINDING_PROPERTY) private ClientBinding
     * clientBinding; @Resource(name = JAXWSConstants.SERVER_BINDING_PROPERTY)
     * private ServerBinding serverBinding; @Resource(name =
     * JAXWSConstants.CLIENT_TRANSPORT_PROPERTY) private ClientTransport
     * clientTransport; @Resource(name =
     * JAXWSConstants.SERVER_TRANSPORT_PROPERTY) private ServerTransport
     * serverTransport; public RMHandler() { proxy = new RMProxy(this); servant =
     * new RMServant(); } @PostConstruct protected synchronized void
     * initialise() { /* getHandlerMap().put(getBinding(), this); if (null ==
     * configurationHelper) { configurationHelper = new
     * ConfigurationHelper(getBinding(), null == clientBinding); } if (null ==
     * getSource()) { source = new RMSource(this); } if (null == destination) {
     * destination = new RMDestination(this); } if (null == timer) { timer = new
     * Timer(); } if (!busLifeCycleListenerRegistered) {
     * getBinding().getBus().getLifeCycleManager()
     * .registerLifeCycleListener(new RMBusLifeCycleListener(getSource()));
     * busLifeCycleListenerRegistered = true; } } public static Map<BindingBase,
     * RMHandler> getHandlerMap() { if (null == handlers) { handlers = new
     * HashMap<BindingBase, RMHandler>(); } return handlers; } public void
     * close(MessageContext context) { // TODO commit transaction } public
     * boolean handleFault(LogicalMessageContext context) { return
     * handle(context); } public boolean handleMessage(LogicalMessageContext
     * context) { return handle(context); } public PersistenceManager
     * getPersistenceManager() { return persistenceManager; } public void
     * setPersistenceManager(PersistenceManager pm) { persistenceManager = pm; }
     * public RMStore getStore() { if (null != persistenceManager) { return
     * persistenceManager.getStore(); } return null; } public Timer getTimer() {
     * return timer; } public Bus getBus() { return bus; } public Transport
     * getTransport() { return null == clientTransport ? serverTransport :
     * clientTransport; } public ClientTransport getClientTransport() { return
     * clientTransport; } public ServerTransport getServerTransport() { return
     * serverTransport; } public ClientBinding getClientBinding() { return
     * clientBinding; } public ServerBinding getServerBinding() { return
     * serverBinding; } public boolean isServerSide() { return null !=
     * serverBinding; } public AbstractBindingBase getBinding() { if (null !=
     * clientBinding) { return (AbstractBindingBase)clientBinding; } return
     * (AbstractBindingBase)serverBinding; } public RMProxy getProxy() { return
     * proxy; } public RMServant getServant() { return servant; } public
     * RMSource getSource() { return source; } public RMDestination
     * getDestination() { return destination; } protected void
     * open(LogicalMessageContext context) { // TODO begin transaction }
     * protected boolean handle(LogicalMessageContext context) { try { if
     * (ContextUtils.isOutbound(context)) { handleOutbound(context); } else {
     * handleInbound(context); } } catch (SequenceFault sf) {
     * LOG.log(Level.SEVERE, "SequenceFault", sf); } return true; } protected
     * void handleOutbound(LogicalMessageContext context) throws SequenceFault {
     * LOG.entering(getClass().getName(), "handleOutbound");
     * AddressingPropertiesImpl maps = ContextUtils.retrieveMAPs(context, false,
     * true); // ensure the appropriate version of WS-Addressing is used
     * maps.exposeAs(VersionTransformer.Names200408.WSA_NAMESPACE_NAME); String
     * action = null; if (maps != null && null != maps.getAction()) { action =
     * maps.getAction().getValue(); } // nothing to do if this is a
     * CreateSequence, TerminateSequence or // SequenceInfo request if
     * (LOG.isLoggable(Level.FINE)) { LOG.fine("Action: " + action); } boolean
     * isApplicationMessage = true; if
     * (RMUtils.getRMConstants().getCreateSequenceAction().equals(action) ||
     * RMUtils.getRMConstants().getCreateSequenceResponseAction().equals(action) ||
     * RMUtils.getRMConstants().getTerminateSequenceAction().equals(action) ||
     * RMUtils.getRMConstants().getLastMessageAction().equals(action) ||
     * RMUtils.getRMConstants().getSequenceAcknowledgmentAction().equals(action) ||
     * RMUtils.getRMConstants().getSequenceInfoAction().equals(action)) {
     * isApplicationMessage = false; } RMPropertiesImpl rmpsOut =
     * (RMPropertiesImpl)RMContextUtils.retrieveRMProperties(context, true); if
     * (null == rmpsOut) { rmpsOut = new RMPropertiesImpl();
     * RMContextUtils.storeRMProperties(context, rmpsOut, true); }
     * RMPropertiesImpl rmpsIn = null; Identifier inSeqId = null; BigInteger
     * inMessageNumber = null; if (isApplicationMessage) { rmpsIn =
     * (RMPropertiesImpl)RMContextUtils.retrieveRMProperties(context, false); if
     * (null != rmpsIn && null != rmpsIn.getSequence()) { inSeqId =
     * rmpsIn.getSequence().getIdentifier(); inMessageNumber =
     * rmpsIn.getSequence().getMessageNumber(); } if
     * (LOG.isLoggable(Level.FINE)) { LOG.fine("inbound sequence: " + (null ==
     * inSeqId ? "null" : inSeqId.getValue())); } // not for partial responses
     * to oneway requests if (!(isServerSide() &&
     * BindingContextUtils.isOnewayTransport(context))) { if
     * (!ContextUtils.isRequestor(context)) { assert null != inSeqId; } // get
     * the current sequence, requesting the creation of a new one if necessary
     * SourceSequence seq = getSequence(inSeqId, context, maps); assert null !=
     * seq; // increase message number and store a sequence type object in //
     * context seq.nextMessageNumber(inSeqId, inMessageNumber);
     * rmpsOut.setSequence(seq); // if this was the last message in the
     * sequence, reset the // current sequence so that a new one will be created
     * next // time the handler is invoked if (seq.isLastMessage()) {
     * source.setCurrent(null); } } } // add Acknowledgements (to application
     * messages or explicitly // created Acknowledgement messages only) if
     * (isApplicationMessage ||
     * RMUtils.getRMConstants().getSequenceAcknowledgmentAction().equals(action)) {
     * AttributedURI to = VersionTransformer.convert(maps.getTo()); assert null !=
     * to; addAcknowledgements(rmpsOut, inSeqId, to); } // indicate to the
     * binding that a response is expected from the transport although // the
     * web method is a oneway method if
     * (BindingContextUtils.isOnewayMethod(context) ||
     * RMUtils.getRMConstants().getLastMessageAction().equals(action)) {
     * context.put(OutputStreamMessageContext.ONEWAY_MESSAGE_TF, Boolean.FALSE); } }
     * protected void handleInbound(LogicalMessageContext context) throws
     * SequenceFault { LOG.entering(getClass().getName(), "handleInbound");
     * RMProperties rmps = RMContextUtils.retrieveRMProperties(context, false);
     * final AddressingPropertiesImpl maps = ContextUtils.retrieveMAPs(context,
     * false, false); assert null != maps; String action = null; if (null !=
     * maps.getAction()) { action = maps.getAction().getValue(); } if
     * (LOG.isLoggable(Level.FINE)) { LOG.fine("Action: " + action); } if
     * (RMUtils.getRMConstants().getCreateSequenceResponseAction().equals(action)) {
     * Object[] parameters =
     * (Object[])context.get(ObjectMessageContext.METHOD_PARAMETERS);
     * CreateSequenceResponseType csr =
     * (CreateSequenceResponseType)parameters[0];
     * getServant().createSequenceResponse(getSource(), csr,
     * getProxy().getOfferedIdentifier()); return; } else if
     * (RMUtils.getRMConstants().getCreateSequenceAction().equals(action)) {
     * Object[] parameters =
     * (Object[])context.get(ObjectMessageContext.METHOD_PARAMETERS);
     * CreateSequenceType cs = (CreateSequenceType)parameters[0]; final
     * CreateSequenceResponseType csr =
     * getServant().createSequence(getDestination(), cs, maps); Runnable
     * response = new Runnable() { public void run() { try {
     * getProxy().createSequenceResponse(maps, csr); } catch (IOException ex) {
     * ex.printStackTrace(); } catch (SequenceFault sf) { sf.printStackTrace(); } } };
     * getBinding().getBus().getWorkQueueManager().getAutomaticWorkQueue().execute(response);
     * return; } else if
     * (RMUtils.getRMConstants().getTerminateSequenceAction().equals(action)) {
     * Object[] parameters =
     * (Object[])context.get(ObjectMessageContext.METHOD_PARAMETERS);
     * TerminateSequenceType cs = (TerminateSequenceType)parameters[0];
     * getServant().terminateSequence(getDestination(), cs.getIdentifier()); } //
     * for application AND out of band messages if (null != rmps) {
     * processAcknowledgments(rmps); processAcknowledgmentRequests(rmps);
     * processSequence(rmps, maps); processDeliveryAssurance(rmps); } } void
     * processAcknowledgments(RMProperties rmps) { Collection<SequenceAcknowledgement>
     * acks = rmps.getAcks(); if (null != acks) { for (SequenceAcknowledgement
     * ack : acks) { getSource().setAcknowledged(ack); } } } void
     * processSequence(RMProperties rmps, AddressingProperties maps) throws
     * SequenceFault { SequenceType s = rmps.getSequence(); if (null == s) {
     * return; } getDestination().acknowledge(s, null == maps.getReplyTo() ?
     * null : maps.getReplyTo().getAddress().getValue()); } void
     * processAcknowledgmentRequests(RMProperties rmps) { Collection<AckRequestedType>
     * requested = rmps.getAcksRequested(); if (null != requested) { for
     * (AckRequestedType ar : requested) { DestinationSequence seq =
     * getDestination().getSequence(ar.getIdentifier()); if (null != seq) {
     * seq.scheduleImmediateAcknowledgement(); } else { LOG.severe("No such
     * sequence."); } } } } boolean processDeliveryAssurance(RMProperties rmps) {
     * SequenceType s = rmps.getSequence(); if (null == s) { return true; }
     * DestinationSequence ds = destination.getSequence(s.getIdentifier());
     * return ds.applyDeliveryAssurance(s.getMessageNumber()); }
     */

    /*
   
    protected void setInitialised(ConfigurationHelper ch,
                                  RMSource s,
                                  RMDestination d,
                                  Timer t,
                                  boolean registered
                                  ) {
        configurationHelper = ch;
        source = s;
        destination = d;
        timer = t;
        busLifeCycleListenerRegistered = registered;
        initialise();
    }
    */
    
}
