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

import org.apache.cxf.binding.soap.interceptor.MustUnderstandInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.interceptor.OutgoingChainInterceptor;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.jaxws.handler.AbstractJAXWSHandlerInterceptor;
import org.apache.cxf.jaxws.handler.HandlerChainInvoker;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.staxutils.W3CDOMStreamWriter;

public class LogicalHandlerInInterceptor<T extends Message> 
    extends AbstractJAXWSHandlerInterceptor<T> {

    public LogicalHandlerInInterceptor(Binding binding) {
        super(binding);
        setPhase(Phase.PRE_PROTOCOL);
        addAfter(MustUnderstandInterceptor.class.getName());
        addAfter(StaxOutInterceptor.class.getName());
        addAfter(SAAJOutInterceptor.class.getName());
        addAfter(SOAPHandlerInterceptor.class.getName());
    }

    public void handleMessage(T message) {
        HandlerChainInvoker invoker = getInvoker(message);
        if (invoker.getLogicalHandlers().isEmpty()) {
            return;
        }
        
        LogicalMessageContextImpl lctx = new LogicalMessageContextImpl(message);
        invoker.setLogicalMessageContext(lctx);
        boolean requestor = isRequestor(message);
        if (!invoker.invokeLogicalHandlers(requestor, lctx)) {
            if (!requestor) {
                handleAbort(message, null);
            } else {
                //Client side inbound, thus no response expected, do nothing, the close will  
                //be handled by MEPComplete later
            }
        }
 
        //If this is the inbound and end of MEP, call MEP completion
        if (!isOutbound(message) && isMEPComlete(message)) {
            onCompletion(message);
        }
    }

    private void handleAbort(T message, W3CDOMStreamWriter writer) {
        message.getInterceptorChain().abort();

        if (!message.getExchange().isOneWay()) {
            Endpoint e = message.getExchange().get(Endpoint.class);
            Message responseMsg = e.getBinding().createMessage();            

            //server side inbound

            message.getExchange().setOutMessage(responseMsg);
            XMLStreamReader reader = message.getContent(XMLStreamReader.class);
            if (reader == null && writer != null) {
                reader = StaxUtils.createXMLStreamReader(writer.getDocument());
            }

            InterceptorChain chain = OutgoingChainInterceptor
                .getOutInterceptorChain(message.getExchange());
            responseMsg.setInterceptorChain(chain);
            responseMsg.put("LogicalHandlerInterceptor.INREADER", reader);
            //so the idea of starting interceptor chain from any specified point does not work
            //well for outbound case, as many outbound interceptors have their ending interceptors.
            //For example, we can not skip MessageSenderInterceptor.               
            chain.doIntercept(responseMsg);
        }        
    }
    
    public void handleFault(T message) {
        // TODO
    }
}
