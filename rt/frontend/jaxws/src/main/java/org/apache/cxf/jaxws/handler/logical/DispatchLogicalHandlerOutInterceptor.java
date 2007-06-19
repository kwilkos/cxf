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

package org.apache.cxf.jaxws.handler.logical;

import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.Binding;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.handler.AbstractJAXWSHandlerInterceptor;
import org.apache.cxf.jaxws.handler.HandlerChainInvoker;
import org.apache.cxf.jaxws.support.ContextPropertiesMapping;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.MessageObserver;


public class DispatchLogicalHandlerOutInterceptor<T extends Message> 
    extends AbstractJAXWSHandlerInterceptor<T> {
    
    public static final String ORIGINAL_WRITER = "original_writer";
     
    public DispatchLogicalHandlerOutInterceptor(Binding binding) {
        super(binding, Phase.PRE_MARSHAL);
    }
    
    public void handleMessage(T message) throws Fault {
        HandlerChainInvoker invoker = getInvoker(message);
        if (invoker.getLogicalHandlers().isEmpty()) {
            return;
        }            

        LogicalMessageContextImpl lctx = new LogicalMessageContextImpl(message);
        invoker.setLogicalMessageContext(lctx);
        boolean requestor = isRequestor(message);
        
        ContextPropertiesMapping.mapCxf2Jaxws(message.getExchange(), lctx, requestor);          
        
        if (!invoker.invokeLogicalHandlers(requestor, lctx)) {
            if (requestor) {
                // client side - abort
                message.getInterceptorChain().abort();
                Endpoint e = message.getExchange().get(Endpoint.class);
                Message responseMsg = e.getBinding().createMessage();            

                MessageObserver observer = (MessageObserver)message.getExchange()
                            .get(MessageObserver.class);
                if (observer != null) {
                    //client side outbound, the request message becomes the response message
                    responseMsg.setContent(XMLStreamReader.class, message
                        .getContent(XMLStreamReader.class));                        
                    
                    message.getExchange().setInMessage(responseMsg);
                    responseMsg.put(PhaseInterceptorChain.STARTING_AT_INTERCEPTOR_ID,
                                    LogicalHandlerInInterceptor.class.getName());
                    observer.onMessage(responseMsg);
                }
            } else {
                // server side - abort
                //System.out.println("Logical handler server side aborting");
            }
        }
    }
}
