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

package org.apache.cxf.interceptor;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.phase.PhaseManager;

/**
 * Sets up the outgoing chain if the operation has an output message.
 * @author Dan Diephouse
 */
public class OutgoingChainSetupInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final Logger LOG = Logger.getLogger(OutgoingChainSetupInterceptor.class.getName());
    public OutgoingChainSetupInterceptor() {
        super();
        setPhase(Phase.PRE_LOGICAL);
    }

    public void handleMessage(Message message) {
        Exchange ex = message.getExchange();
        if (ex.isOneWay()) {
            return;
        }
        
        Endpoint ep = ex.get(Endpoint.class);
        
        Message outMessage = message.getExchange().getOutMessage();
        if (outMessage == null) {
            outMessage = ep.getBinding().createMessage();
            ex.setOutMessage(outMessage);
        }

        Message faultMessage = message.getExchange().getOutFaultMessage();
        if (faultMessage == null) {
            faultMessage = ep.getBinding().createMessage();            
            ex.setOutFaultMessage(faultMessage);
        }
        outMessage.setInterceptorChain(getOutInterceptorChain(ex));
    }
    
    public static InterceptorChain getOutInterceptorChain(Exchange ex) {
        Bus bus = ex.get(Bus.class);
        PhaseManager pm = bus.getExtension(PhaseManager.class);
        PhaseInterceptorChain chain = new PhaseInterceptorChain(pm.getOutPhases());
        
        Endpoint ep = ex.get(Endpoint.class);
        List<Interceptor> il = ep.getOutInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by endpoint: " + il);
        }
        chain.add(il);
        il = ep.getService().getOutInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by service: " + il);
        }
        chain.add(il);
        il = bus.getOutInterceptors();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Interceptors contributed by bus: " + il);
        }
        chain.add(il);        
        if (ep.getBinding() != null) {
            il = ep.getBinding().getOutInterceptors();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Interceptors contributed by binding: " + il);
            }
            chain.add(il);
        }
        chain.setFaultObserver(ep.getOutFaultObserver());
        return chain;
    }
}
