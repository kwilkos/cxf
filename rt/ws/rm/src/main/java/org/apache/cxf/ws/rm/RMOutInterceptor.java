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

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.MAPAggregator;
import org.apache.cxf.ws.addressing.VersionTransformer;
import org.apache.cxf.ws.addressing.v200408.AttributedURI;

/**
 * 
 */
public class RMOutInterceptor extends AbstractRMInterceptor {
    
    private static final Logger LOG = LogUtils.getL7dLogger(RMOutInterceptor.class);
    private Set<String> after = Collections.singleton(MAPAggregator.class.getName());
    
    public Set<String> getAfter() {
        return after;
    }
    
    public Set<String> getBefore() {
        return CastUtils.cast(Collections.EMPTY_SET);
    }

    public String getId() {
        return RMOutInterceptor.class.getName();
    }
    
    void handleMessage(Message message, boolean isFault) throws SequenceFault {
        LOG.entering(getClass().getName(), "handleMessage");
       
        AddressingProperties maps =
            RMContextUtils.retrieveMAPs(message, false, true);
        RMContextUtils.ensureExposedVersion(maps);
        
        Source source = getManager().getSource(message);
        Destination destination = getManager().getDestination(message);

        String action = null;
        if (maps != null && null != maps.getAction()) {
            action = maps.getAction().getValue();
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Action: " + action);
        }

        boolean isApplicationMessage = isAplicationMessage(action);
        
        RMProperties rmpsOut = (RMProperties)RMContextUtils.retrieveRMProperties(message, true);
        if (null == rmpsOut) {
            rmpsOut = new RMProperties();
            RMContextUtils.storeRMProperties(message, rmpsOut, true);
        }
        
        RMProperties rmpsIn = null;
        Identifier inSeqId = null;
        BigInteger inMessageNumber = null;
        
        if (isApplicationMessage) {
                        
            rmpsIn = (RMProperties)RMContextUtils.retrieveRMProperties(message, false);
            
            if (null != rmpsIn && null != rmpsIn.getSequence()) {
                inSeqId = rmpsIn.getSequence().getIdentifier();
                inMessageNumber = rmpsIn.getSequence().getMessageNumber();
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("inbound sequence: " + (null == inSeqId ? "null" : inSeqId.getValue()));
            }
            
            // get the current sequence, requesting the creation of a new one if necessary
            
            SourceSequence seq = getManager().getSequence(inSeqId, message, maps);
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
            || RMConstants.getSequenceAcknowledgmentAction().equals(action)) {
            AttributedURI to = VersionTransformer.convert(maps.getTo());
            assert null != to;
            addAcknowledgements(destination, rmpsOut, inSeqId, to);
        }     
    }
    
    void addAcknowledgements(Destination destination, 
                             RMProperties rmpsOut, 
                             Identifier inSeqId, 
                             AttributedURI to) {

        for (DestinationSequence seq : destination.getAllSequences()) {
            if (seq.sendAcknowledgement()
                && ((seq.getAcksTo().getAddress().getValue().equals(RMUtils.getAddressingConstants()
                    .getAnonymousURI()) && AbstractSequence.identifierEquals(seq.getIdentifier(), 
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
}
