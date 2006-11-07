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

package org.apache.cxf.systest.ws.rm;

import java.util.Collections;
import java.util.ListIterator;
import java.util.Set;


import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.jaxws.handler.LogicalHandlerInterceptor;
import org.apache.cxf.jaxws.handler.StreamHandlerInterceptor;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptor;


/**
 * 
 */
public class JaxwsInterceptorRemover extends AbstractPhaseInterceptor {
    
    private Set<String> before = Collections.singleton(LogicalHandlerInterceptor.class.getName());
    
    public JaxwsInterceptorRemover() {
        setPhase(Phase.PRE_LOGICAL);
    }
    
    public void handleMessage(Message message) throws Fault {

        // remove the JAXWS handler interceptors
        InterceptorChain chain = message.getInterceptorChain();
        ListIterator it = chain.getIterator();
        while (it.hasNext()) {
            PhaseInterceptor pi = (PhaseInterceptor)it.next();
            if (LogicalHandlerInterceptor.class.getName().equals(pi.getId())) {
                chain.remove(pi);
                break;
            }
        }
        it = chain.getIterator();
        while (it.hasNext()) {
            PhaseInterceptor pi = (PhaseInterceptor)it.next();
            if (SOAPHandlerInterceptor.class.getName().equals(pi.getId())) {
                chain.remove(pi);
                break;
            }
        }
        it = chain.getIterator();
        while (it.hasNext()) {
            PhaseInterceptor pi = (PhaseInterceptor)it.next();
            if (StreamHandlerInterceptor.class.getName().equals(pi.getId())) {
                chain.remove(pi);
                break;
            }
        }
    }
   
}
