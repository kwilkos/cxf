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

package org.apache.cxf.jaxws.handler.soap;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.jaxws.handler.AbstractProtocolHandlerInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

public class SOAPHandlerInterceptor extends
        AbstractProtocolHandlerInterceptor<SoapMessage> implements
        SoapInterceptor {
    private static final SAAJOutInterceptor SAAJ_OUT = new SAAJOutInterceptor();
    
    public SOAPHandlerInterceptor(Binding binding) {
        super(binding);
        setPhase(Phase.PRE_PROTOCOL);
        addAfter(StaxOutInterceptor.class.getName());
        addAfter(SAAJOutInterceptor.class.getName());
    }

    public Set<URI> getRoles() {
        Set<URI> roles = new HashSet<URI>();
        // TODO
        return roles;
    }

    public Set<QName> getUnderstoodHeaders() {
        Set<QName> understood = new HashSet<QName>();
        for (Handler h : getBinding().getHandlerChain()) {
            if (h instanceof SOAPHandler) {
                Set<QName> headers = CastUtils.cast(((SOAPHandler) h).getHeaders());
                if (headers != null) {
                    understood.addAll(headers);
                }
            }
        }
        return understood;
    }

    public void handleMessage(SoapMessage message) {
        if (getInvoker(message).getProtocolHandlers().isEmpty()) {
            return;
        }
        
        if (getInvoker(message).isOutbound()) {
            SOAPMessage saaj = message.getContent(SOAPMessage.class); 
            if (saaj == null) {
                SAAJ_OUT.handleMessage(message);
            }
            
            final SOAPHandlerInterceptor handlerInterceptor = this;
            message.getInterceptorChain().add(new AbstractSoapInterceptor() {

                @Override
                public String getPhase() {
                    return Phase.USER_PROTOCOL;
                }

                public void handleMessage(SoapMessage message) throws Fault {
                    handlerInterceptor.invokeSOAPHandlerInterceptors(message);
                }
            });
        } else {
            super.handleMessage(message);
        }
    }

    protected void invokeSOAPHandlerInterceptors(SoapMessage message) {
        super.handleMessage(message);
    }
    
    @Override
    protected MessageContext createProtocolMessageContext(Message message) {
        return new SOAPMessageContextImpl(message);
    }

    public void handleFault(SoapMessage message) {
    }
}
