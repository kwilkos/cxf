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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.ws.rm.Identifier;
import org.apache.cxf.ws.rm.SequenceAcknowledgement;
import org.apache.cxf.ws.rm.SourceSequence;
import org.apache.cxf.ws.rm.persistence.RMStore;

public class Source extends AbstractEndpoint {

    private static final String REQUESTOR_SEQUENCE_ID = "";
    
    private Map<String, SourceSequenceImpl> map;
    private Map<String, SourceSequenceImpl> current;     
    private Lock sequenceCreationLock;
    private Condition sequenceCreationCondition;
    private boolean sequenceCreationNotified;

    Source(RMEndpoint reliableEndpoint) {
        super(reliableEndpoint);
        map = new HashMap<String, SourceSequenceImpl>();
        current = new HashMap<String, SourceSequenceImpl>();
             
        sequenceCreationLock = new ReentrantLock();
        sequenceCreationCondition = sequenceCreationLock.newCondition();
    }
    
    
    
    public SourceSequence getSequence(Identifier id) {        
        return map.get(id.getValue());
    }
    
    public void addSequence(SourceSequenceImpl seq) { 
        addSequence(seq, true);
    }
    
    public void addSequence(SourceSequenceImpl seq, boolean persist) {
        seq.setSource(this);
        map.put(seq.getIdentifier().getValue(), seq);
        if (persist) {
            RMStore store = getInterceptor().getStore();
            if (null != store) {
                store.createSourceSequence(seq);
            }
        }
    }
    
    public void removeSequence(SourceSequence seq) {        
        map.remove(seq.getIdentifier().getValue());
        RMStore store = getInterceptor().getStore();
        if (null != store) {
            store.removeSourceSequence(seq.getIdentifier());
        }
    }
    
    public Collection<SourceSequenceImpl> getAllSequences() {                 
        return CastUtils.cast(map.values());
    } 
    
    /**
     * Stores the received acknowledgment in the Sequence object identified in
     * the <code>SequenceAcknowldgement</code> parameter. Then purges any
     * acknowledged messages from the retransmission queue and requests sequence
     * termination if necessary.
     * 
     * @param acknowledgment
     */
    public void setAcknowledged(SequenceAcknowledgement acknowledgment) {
        Identifier sid = acknowledgment.getIdentifier();
        SourceSequenceImpl seq = getSequenceImpl(sid);        
        if (null != seq) {
            seq.setAcknowledged(acknowledgment);
            getInterceptor().getRetransmissionQueue().purgeAcknowledged(seq);
            if (seq.allAcknowledged()) {
                // TODO
                /*
                try {
                    // 
                    getHandler().getProxy().terminateSequence(seq); 
                } catch (IOException ex) {
                    Message msg = new Message("SEQ_TERMINATION_FAILURE", LOG, seq.getIdentifier());
                    LOG.log(Level.SEVERE, msg.toString(), ex);
                }
                */
            }
        }
    }
    
    /**
     * Returns a collection of all sequences for which have not yet been
     * completely acknowledged.
     * 
     * @return the collection of unacknowledged sequences.
     */
    public Collection<SourceSequence> getAllUnacknowledgedSequences() {
        Collection<SourceSequence> seqs = new ArrayList<SourceSequence>();
        for (SourceSequenceImpl seq : map.values()) {
            if (!seq.allAcknowledged()) {
                seqs.add(seq);
            }
        }
        return seqs;        
    }

    /**
     * Returns the current sequence used by a client side source.
     * 
     * @return the current sequence.
     */
    SourceSequence getCurrent() {
        return getCurrent(null);
    }
    
    /**
     * Sets the current sequence used by a client side source.
     * @param s the current sequence.
     */
    void setCurrent(SourceSequenceImpl s) {
        setCurrent(null, s);
    }
    
    /**
     * Returns the current sequence used by a server side source for responses to a message
     * sent as part of the inbound sequence with the specified identifier.
     * 
     * @return the current sequence.
     */
    SourceSequenceImpl getCurrent(Identifier i) {        
        sequenceCreationLock.lock();
        try {
            return getAssociatedSequence(i);
        } finally {
            sequenceCreationLock.unlock();
        }
    }

    /**
     * Returns the sequence associated with the given identifier.
     * 
     * @param i the corresponding sequence identifier
     * @return the associated sequence
     * @pre the sequenceCreationLock is already held
     */
    SourceSequenceImpl getAssociatedSequence(Identifier i) {        
        return current.get(i == null ? REQUESTOR_SEQUENCE_ID : i.getValue());
    }
    
    /**
     * Await the availability of a sequence corresponding to the given identifier.
     * 
     * @param i the sequence identifier
     * @return
     */
    SourceSequenceImpl awaitCurrent(Identifier i) {
        sequenceCreationLock.lock();
        try {
            SourceSequenceImpl seq = getAssociatedSequence(i);
            while (seq == null) {
                while (!sequenceCreationNotified) {
                    try {
                        sequenceCreationCondition.await();
                    } catch (InterruptedException ie) {
                        // ignore
                    }
                }
                seq = getAssociatedSequence(i);
            }
            return seq;
        } finally {
            sequenceCreationLock.unlock();
        }
    }
    
    /**
     * Sets the current sequence used by a server side source for responses to a message
     * sent as part of the inbound sequence with the specified identifier.
     * @param s the current sequence.
     */
    void setCurrent(Identifier i, SourceSequenceImpl s) {        
        sequenceCreationLock.lock();
        try {
            current.put(i == null ? REQUESTOR_SEQUENCE_ID : i.getValue(), s);
            sequenceCreationNotified = true;
            sequenceCreationCondition.signal();
        } finally {
            sequenceCreationLock.unlock();
        }
    }
    
    SourceSequenceImpl getSequenceImpl(Identifier id) {        
        return map.get(id.getValue());
    }
}
