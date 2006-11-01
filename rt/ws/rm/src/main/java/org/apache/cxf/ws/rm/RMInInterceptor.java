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

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
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
        
        // Destination destination = getManager().getDestination(message);
        RMEndpoint rme = getManager().getReliableEndpoint(message);
        Servant servant = new Servant(rme);
        
        if (RMConstants.getCreateSequenceResponseAction().equals(action)) {
            servant.createSequenceResponse(message);
            return;
        } else if (RMConstants.getCreateSequenceAction().equals(action)) {
            servant.createSequence(message);
            /*
            Runnable response = new Runnable() {
                public void run() {
                    try {
                        getProxy().createSequenceResponse(maps, csr);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (SequenceFault sf) {
                        sf.printStackTrace();
                    }
                }
            };
            getBinding().getBus().getWorkQueueManager().getAutomaticWorkQueue().execute(response);
            */    
            return;
        } else if (RMConstants.getTerminateSequenceAction().equals(action)) {
            servant.terminateSequence(message);
        }
        
        // for application AND out of band messages

        if (null != rmps) {            
            
            processAcknowledgments(rmps);

            processAcknowledgmentRequests(rmps);  
            
            processSequence(rmps, maps);
            
            processDeliveryAssurance(rmps);
        }
    }
    
    void processAcknowledgments(RMProperties rmps) {
        
    }

    void processAcknowledgmentRequests(RMProperties rmps) {
        
    }
    
    void processSequence(RMProperties rmps, AddressingProperties maps) {
        
    }
    
    void processDeliveryAssurance(RMProperties rmps) {
        
    }
}
