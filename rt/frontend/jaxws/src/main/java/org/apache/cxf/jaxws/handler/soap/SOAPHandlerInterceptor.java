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
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.MustUnderstandInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapActionInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.interceptor.OutgoingChainInterceptor;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.jaxws.handler.AbstractProtocolHandlerInterceptor;
import org.apache.cxf.jaxws.handler.HandlerChainInvoker;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.transport.MessageObserver;

public class SOAPHandlerInterceptor extends
        AbstractProtocolHandlerInterceptor<SoapMessage> implements
        SoapInterceptor {
    private static final SAAJOutInterceptor SAAJ_OUT = new SAAJOutInterceptor();
    
    public SOAPHandlerInterceptor(Binding binding) {
        super(binding);
        setPhase(Phase.PRE_PROTOCOL);
        addAfter(MustUnderstandInterceptor.class.getName());
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

            SAAJ_OUT.handleMessage(message);

            message.getInterceptorChain().add(new AbstractSoapInterceptor() {
                @Override
                public String getPhase() {
                    return Phase.USER_PROTOCOL;
                }
                @Override
                public String getId() {
                    return SOAPHandlerInterceptor.class.getName() + ".ENDING";
                }

                public void handleMessage(SoapMessage message) throws Fault {
                    handleMessageInternal(message);
                }
            });
        } else {
            handleMessageInternal(message);
            SOAPMessage msg = message.getContent(SOAPMessage.class);
            if (msg != null) {
                XMLStreamReader xmlReader = createXMLStreamReaderFromSOAPMessage(msg);
                message.setContent(XMLStreamReader.class, xmlReader);
            }
        }
    }
    
    private void handleMessageInternal(SoapMessage message) {
        MessageContext context = createProtocolMessageContext(message);
        HandlerChainInvoker invoker = getInvoker(message);
        invoker.setProtocolMessageContext(context);

        if (!invoker.invokeProtocolHandlers(isRequestor(message), context)) {
            message.getInterceptorChain().abort();
            Endpoint e = message.getExchange().get(Endpoint.class);
            Message responseMsg = e.getBinding().createMessage();            
 
            MessageObserver observer = (MessageObserver)message.getExchange().get(MessageObserver.class);
            if (observer != null) {
                //client side outbound, the request message becomes the response message
                message.getExchange().setInMessage(responseMsg);
                SOAPMessage soapMessage = ((SOAPMessageContext)context).getMessage();

                if (soapMessage != null) {
                    responseMsg.setContent(SOAPMessage.class, soapMessage);
                    XMLStreamReader xmlReader = createXMLStreamReaderFromSOAPMessage(soapMessage);
                    responseMsg.setContent(XMLStreamReader.class, xmlReader);
                }
                responseMsg.put(PhaseInterceptorChain.STARTING_AFTER_INTERCEPTOR_ID,
                                SOAPHandlerInterceptor.class.getName());
                observer.onMessage(responseMsg);
            }  else if (!message.getExchange().isOneWay()) {
                //server side inbound
                message.getExchange().setOutMessage(responseMsg);
                SOAPMessage soapMessage = ((SOAPMessageContext)context).getMessage();

                responseMsg.setContent(SOAPMessage.class, soapMessage);
                
                InterceptorChain chain = OutgoingChainInterceptor.getOutInterceptorChain(message
                    .getExchange());
                responseMsg.setInterceptorChain(chain);
                //so the idea of starting interceptor chain from any specified point does not work
                //well for outbound case, as many outbound interceptors have their ending interceptors.
                //For example, we can not skip MessageSenderInterceptor.               
                chain.doInterceptStartingAfter(responseMsg, SoapActionInterceptor.class.getName());
            } 
        }  
        onCompletion(message);
    }
    
    @Override
    protected MessageContext createProtocolMessageContext(Message message) {
        return new SOAPMessageContextImpl(message);
    }
    
    private XMLStreamReader createXMLStreamReaderFromSOAPMessage(SOAPMessage soapMessage) {
        // responseMsg.setContent(SOAPMessage.class, soapMessage);
        XMLStreamReader xmlReader = null;
        try {
            DOMSource bodySource = new DOMSource(soapMessage.getSOAPPart().getEnvelope().getBody());
            xmlReader = StaxUtils.createXMLStreamReader(bodySource);
            xmlReader.nextTag();
            xmlReader.nextTag(); // move past body tag
        } catch (SOAPException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return xmlReader;
    }

    public void handleFault(SoapMessage message) {
    }
}
