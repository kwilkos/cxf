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

package org.apache.cxf.systest.ws.policy;

import java.util.Collection;
import java.util.logging.Logger;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.transport.http.policy.PolicyUtils;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.policy.AssertionInfo;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.cxf.ws.policy.builder.jaxb.JaxbAssertion;

public class PolicyLoggingInterceptor extends AbstractPhaseInterceptor {

    private static final Logger LOG = Logger.getLogger(PolicyLoggingInterceptor.class.getName());
    
    private boolean outbound;
    
    PolicyLoggingInterceptor(boolean o) {
        outbound = o;
        setPhase(Phase.PRE_LOGICAL);
        /*
        if (outbound) {
            setPhase(Phase.POST_STREAM);
            addBefore(PolicyVerificationOutInterceptor.class.getName());
        } else {
            setPhase(Phase.PRE_INVOKE);
            addBefore(PolicyVerificationInInterceptor.class.getName());
        }
        */
    }
    
    public void handleMessage(Message message) throws Fault {
        StringBuffer buf = new StringBuffer();
        String nl = System.getProperty("line.separator");
        buf.append(outbound ? "Outbound " : "Inbound ");
        buf.append("message for operation: " + message.getExchange().get(OperationInfo.class).getName());
        buf.append(nl);
        buf.append("Policies:");
        buf.append(nl);
        AssertionInfoMap aim = message.get(AssertionInfoMap.class);
        if (null != aim) {
            for (Collection<AssertionInfo> ais : aim.values()) {
                for (AssertionInfo ai : ais) {
                    JaxbAssertion<HTTPClientPolicy> cp = JaxbAssertion.cast((JaxbAssertion)ai.getAssertion(),
                                                                            HTTPClientPolicy.class);
                    buf.append(cp);
                    buf.append(nl);
                    buf.append("    data: ");
                    buf.append(PolicyUtils.toString(cp.getData()));
                    buf.append(nl);
                    buf.append("    asserted: ");
                    buf.append(ai.isAsserted());
                    buf.append(nl);
                }
            }
        }
        LOG.fine(buf.toString());
        
    }

}
