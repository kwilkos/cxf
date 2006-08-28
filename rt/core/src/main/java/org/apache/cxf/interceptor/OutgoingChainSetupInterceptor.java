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

import java.io.IOException;

import javax.wsdl.WSDLException;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.phase.PhaseManager;
import org.apache.cxf.service.model.BindingOperationInfo;

/**
 * Sets up the outgoing chain if the operation has an output message.
 * @author Dan Diephouse
 */
public class OutgoingChainSetupInterceptor extends AbstractPhaseInterceptor<Message> {

    public OutgoingChainSetupInterceptor() {
        super();
        setPhase(Phase.PRE_LOGICAL);
    }

    public void handleMessage(Message message) {
        Exchange ex = message.getExchange();
        BindingOperationInfo bop = ex.get(BindingOperationInfo.class);
        
        if (bop.getOperationInfo().isOneWay()) {
            return;
        }
            
        Bus bus = ex.get(Bus.class);
        PhaseManager pm = bus.getExtension(PhaseManager.class);
        PhaseInterceptorChain chain = new PhaseInterceptorChain(pm.getOutPhases());
        
        Endpoint ep = ex.get(Endpoint.class);
        chain.add(ep.getOutInterceptors());
        chain.add(ep.getService().getOutInterceptors());
        chain.add(bus.getOutInterceptors());        
        if (ep.getBinding() != null) {
            chain.add(ep.getBinding().getOutInterceptors());
        }
        
        Message outMessage = message.getExchange().getOutMessage();
        if (outMessage == null) {
            outMessage = ep.getBinding().createMessage();
            ex.setOutMessage(outMessage);
        }
        
        if (outMessage.getConduit() == null
            && ex.getConduit() == null
            && ex.getDestination() != null) {
            try {
                ex.setConduit(ex.getDestination().getBackChannel(message, null, null));
            } catch (WSDLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        outMessage.setInterceptorChain(chain);
    }
}
