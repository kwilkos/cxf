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
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.ws.addressing.v200408.EndpointReferenceType;
import org.apache.cxf.ws.rm.DestinationSequence;
import org.apache.cxf.ws.rm.Identifier;
import org.apache.cxf.ws.rm.RMConstants;
import org.apache.cxf.ws.rm.SequenceAcknowledgement;
import org.apache.cxf.ws.rm.SequenceAcknowledgement.AcknowledgementRange;
import org.apache.cxf.ws.rm.SequenceFault;
import org.apache.cxf.ws.rm.SequenceFaultType;
import org.apache.cxf.ws.rm.interceptor.AcksPolicyType;
import org.apache.cxf.ws.rm.interceptor.DeliveryAssuranceType;
import org.apache.cxf.ws.rm.persistence.RMStore;
import org.apache.cxf.ws.rm.policy.RMAssertion;

public class DestinationSequenceImpl extends AbstractSequenceImpl implements DestinationSequence {

    private static final Logger LOG = LogUtils.getL7dLogger(DestinationSequenceImpl.class);

    private Destination destination;
    private EndpointReferenceType acksTo;
    private BigInteger lastMessageNumber;
    private SequenceMonitor monitor;
    private boolean acknowledgeOnNextOccasion;
    private List<DeferredAcknowledgment> deferredAcknowledgments;
    private String correlationID;
    
    public DestinationSequenceImpl(Identifier i, EndpointReferenceType a, Destination d) {
        this(i, a, null, null);
        setDestination(d);
    }
    
    public DestinationSequenceImpl(Identifier i, EndpointReferenceType a,
                              BigInteger lmn, SequenceAcknowledgement ac) {
        super(i);
        acksTo = a;
        lastMessageNumber = lmn;
        acknowledgement = ac;
        if (null == acknowledgement) {
            acknowledgement = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
            acknowledgement.setIdentifier(id);
        }
        monitor = new SequenceMonitor();
    }

    
    // RMDestinationSequence interface
    
    /* (non-Javadoc)
     * @see org.apache.cxf.ws.rm.DestinationSequence#acknowledge(java.math.BigInteger)
     */
    public void acknowledge(BigInteger messageNumber) throws SequenceFault {
        if (null != lastMessageNumber && messageNumber.compareTo(lastMessageNumber) > 0) {
            SequenceFaultType sf = RMUtils.getWSRMFactory().createSequenceFaultType();
            sf.setFaultCode(RMConstants.getLastMessageNumberExceededFaultCode());
            Message msg = new Message("LAST_MESSAGE_NUMBER_EXCEEDED_EXC", LOG, this);
            throw new SequenceFault(msg.toString(), sf);
        }
        
        monitor.acknowledgeMessage();
        
        synchronized (this) {
            boolean done = false;
            int i = 0;
            for (; i < acknowledgement.getAcknowledgementRange().size(); i++) {
                AcknowledgementRange r = acknowledgement.getAcknowledgementRange().get(i);
                if (r.getLower().compareTo(messageNumber) <= 0 
                    && r.getUpper().compareTo(messageNumber) >= 0) {
                    done = true;
                    break;
                } else {
                    BigInteger diff = r.getLower().subtract(messageNumber);
                    if (diff.signum() == 1) {
                        if (diff.equals(BigInteger.ONE)) {
                            r.setLower(messageNumber);
                            done = true;
                        }
                        break;
                    } else if (messageNumber.subtract(r.getUpper()).equals(BigInteger.ONE)) {
                        r.setUpper(messageNumber);
                        done = true;
                        break;
                    }
                }
            }

            if (!done) {
                AcknowledgementRange range = RMUtils.getWSRMFactory()
                    .createSequenceAcknowledgementAcknowledgementRange();
                range.setLower(messageNumber);
                range.setUpper(messageNumber);
                acknowledgement.getAcknowledgementRange().add(i, range);
            }
            
            notifyAll();
        }
        
        purgeAcknowledged(messageNumber);
        
        scheduleAcknowledgement();
        
    }

    /* (non-Javadoc)
     * @see org.apache.cxf.ws.rm.DestinationSequence#getAcknowledgment()
     */
    public SequenceAcknowledgement getAcknowledgment() {
        return acknowledgement;
    }

    /* (non-Javadoc)
     * @see org.apache.cxf.ws.rm.DestinationSequence#getAcknowledgmentAsStream()
     */
    public InputStream getAcknowledgmentAsStream() {
        // return RMUtils.getPersistenceUtils().getAcknowledgementAsInputStream(acked);
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cxf.ws.rm.DestinationSequence#getAcksTo()
     */
    public EndpointReferenceType getAcksTo() {
        return acksTo;
    }

    /* (non-Javadoc)
     * @see org.apache.cxf.ws.rm.DestinationSequence#getEndpointIdentifier()
     */
    public String getEndpointIdentifier() {
        return destination.getName().toString();
    }

    /* (non-Javadoc)
     * @see org.apache.cxf.ws.rm.DestinationSequence#getLastMessageNr()
     */
    public BigInteger getLastMessageNumber() {
        return lastMessageNumber;
    }
    
    //  end RMDestinationSequence interface

    void setLastMessageNumber(BigInteger lmn) {
        lastMessageNumber = lmn;
    }
      
    boolean canPiggybackAckOnPartialResponse() {
        // TODO: should also check if we allow breaking the WI Profile rule by which no headers
        // can be included in a HTTP response
        return getAcksTo().getAddress().getValue().equals(RMConstants.WSA_ANONYMOUS_ADDRESS);
    }
     
    final void setDestination(Destination d) {
        destination = d;
    }
    
    Destination getDestination() {
        return destination;
    }
    
    /**
     * Returns the monitor for this sequence.
     * 
     * @return the sequence monitor.
     */
    SequenceMonitor getMonitor() {
        return monitor;
    }
    
    /**
     * Ensures that the delivery assurance is honored, e.g. by throwing an 
     * exception if the message had already been delivered and the delivery
     * assurance is AtMostOnce.
     * This method blocks in case the delivery assurance is 
     * InOrder and and not all messages with lower message numbers have been 
     * delivered.
     * 
     * @param s the SequenceType object including identifier and message number
     */
    boolean applyDeliveryAssurance(BigInteger mn) {
        DeliveryAssuranceType da = destination.getInterceptor().getDeliveryAssurance();
        if (da.isSetAtMostOnce() && isAcknowledged(mn)) {
            Message msg = new Message("MESSAGE_ALREADY_DELIVERED", LOG, mn, getIdentifier().getValue());
            LOG.log(Level.SEVERE, msg.toString());
            return false;
        } 
        if (da.isSetInOrder() && da.isSetAtLeastOnce()) {
            synchronized (this) {
                boolean ok = allPredecessorsAcknowledged(mn);
                while (!ok) {
                    try {
                        wait();                        
                        ok = allPredecessorsAcknowledged(mn);
                    } catch (InterruptedException ie) {
                        // ignore
                    }
                }
            }
        }
        return true;
    }
    
    synchronized boolean allPredecessorsAcknowledged(BigInteger mn) {
        return acknowledgement.getAcknowledgementRange().size() == 1
            && acknowledgement.getAcknowledgementRange().get(0).getLower().equals(BigInteger.ONE)
            && acknowledgement.getAcknowledgementRange().get(0).getUpper().subtract(mn).signum() >= 0;
    }
    
    void purgeAcknowledged(BigInteger messageNr) {
        RMStore store = destination.getInterceptor().getStore();
        if (null == store) {
            return;
        }
        Collection<BigInteger> messageNrs = new ArrayList<BigInteger>();
        messageNrs.add(messageNr);
        store.removeMessages(getIdentifier(), messageNrs, false);
    }

    /**
     * Called after an acknowledgement header for this sequence has been added to an outgoing message.
     */
    void acknowledgmentSent() {
        acknowledgeOnNextOccasion = false;
    }

    boolean sendAcknowledgement() {
        return acknowledgeOnNextOccasion;
    }
    
    /**
     * The correlation of the incoming CreateSequence call used to create this
     * sequence is recorded so that in the absence of an offer, the corresponding
     * outgoing CreateSeqeunce can be correlated.
     */
    void setCorrelationID(String cid) {
        correlationID = cid;
    }
   
    String getCorrelationID() {
        return correlationID;
    }

    private void scheduleAcknowledgement() {          
        RMAssertion rma = destination.getInterceptor().getRMAssertion();
        int delay = 0;
        if (null != rma.getAcknowledgementInterval()) {
            delay = rma.getAcknowledgementInterval().getMilliseconds().intValue();
        }
        AcksPolicyType ap = destination.getInterceptor().getDestinationPolicy().getAcksPolicy();
 
        if (delay > 0 && getMonitor().getMPM() >= ap.getIntraMessageThreshold()) {
            scheduleDeferredAcknowledgement(delay);
        } else {
            scheduleImmediateAcknowledgement();
        }
    }


    void scheduleImmediateAcknowledgement() {
        acknowledgeOnNextOccasion = true;
    }

    synchronized void scheduleDeferredAcknowledgement(int delay) {
        if (null == deferredAcknowledgments) {
            deferredAcknowledgments = new ArrayList<DeferredAcknowledgment>();
        }
        long now = System.currentTimeMillis();
        long expectedExecutionTime = now + delay;
        for (DeferredAcknowledgment da : deferredAcknowledgments) {
            if (da.scheduledExecutionTime() <= expectedExecutionTime) {
                return;
            }
        }
        DeferredAcknowledgment da = new DeferredAcknowledgment();
        deferredAcknowledgments.add(da);
        destination.getInterceptor().getTimer().schedule(da, delay);
    }

    final class DeferredAcknowledgment extends TimerTask {

        public void run() {
            DestinationSequenceImpl.this.scheduleImmediateAcknowledgement();
            try {                
                RMEndpoint rme = destination.getReliableEndpoint();
                rme.getProxy().acknowledge(DestinationSequenceImpl.this);
            } catch (IOException ex) {
                Message msg = new Message("SEQ_ACK_SEND_EXC", LOG, DestinationSequenceImpl.this);
                LOG.log(Level.SEVERE, msg.toString(), ex);
            }
        }
    }
    
    
}
