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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptor;

/**
 * Interceptor responsible for implementing exchange of RM protocol messages,
 * aggregating RM metadata in the application message and processing of 
 * RM metadata contained in incoming application messages.
 * The same interceptor can be used on multiple endpoints.
 *
 */
public abstract class AbstractRMInterceptor implements PhaseInterceptor<Message> {

    private static final Logger LOG = LogUtils.getL7dLogger(AbstractRMInterceptor.class);      
    private RMManager manager;
    private Bus bus;
     
    public RMManager getManager() {
        if (null == manager) {
            return bus.getExtension(RMManager.class);
        }
        return manager;
    }

    public void setManager(RMManager m) {
        manager = m;
    }
    
    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }
    
    // PhaseInterceptor interface
    
   

    public String getPhase() {
        return Phase.PRE_LOGICAL;
    }

    // Interceptor interface 
    
    public void handleMessage(Message msg) throws Fault {
        
        try {
            handleMessage(msg, false);
        } catch (SequenceFault ex) {
            LOG.log(Level.SEVERE, "SequenceFault", ex);
            throw new Fault(ex);
        }
    }
    
    public void handleFault(Message msg) {
        try {
            handleMessage(msg, true);
        } catch (SequenceFault ex) {
            LOG.log(Level.SEVERE, "SequenceFault", ex);
        }
    } 
    
    // rm logic
    
    abstract void handleMessage(Message msg, boolean isFault) throws SequenceFault;
    
    protected boolean isAplicationMessage(String action) {
        if (RMConstants.getCreateSequenceAction().equals(action)
            || RMConstants.getCreateSequenceResponseAction().equals(action)
            || RMConstants.getTerminateSequenceAction().equals(action)
            || RMConstants.getLastMessageAction().equals(action)
            || RMConstants.getSequenceAcknowledgmentAction().equals(action)
            || RMConstants.getSequenceInfoAction().equals(action)) {
            return false;
        }
        return true;
    }
    
    protected boolean isPartialResponse(Message msg) {
        return RMContextUtils.isOutbound(msg) 
            && msg.getContent(List.class) == null
            && getException(msg.getExchange()) == null;   
    }
    
    private Exception getException(Exchange exchange) {
        if (exchange.getFaultMessage() != null) {
            return exchange.getFaultMessage().getContent(Exception.class);
        }
        return null;
    }
}
