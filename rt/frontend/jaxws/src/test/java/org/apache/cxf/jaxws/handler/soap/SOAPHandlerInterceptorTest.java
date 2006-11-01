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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import junit.framework.TestCase;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.jaxws.handler.HandlerChainInvoker;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;

import org.easymock.classextension.IMocksControl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.notNull;
import static org.easymock.classextension.EasyMock.createNiceControl;

public class SOAPHandlerInterceptorTest extends TestCase {

    public void setUp() {
    }

    public void tearDown() {
    }

    // SAAJ tree is created on demand. SAAJ wont be created without
    // the calling of SOAPMessageContext.getMessage().
    public void testNoCallToGetMessageOutBound() throws Exception {
        List<Handler> list = new ArrayList<Handler>();
        list.add(new SOAPHandler<SOAPMessageContext>() {
            public boolean handleMessage(SOAPMessageContext smc) {
                return true;
            }

            public boolean handleFault(SOAPMessageContext smc) {
                return true;
            }

            public Set<QName> getHeaders() {
                return null;
            }

            public void close(MessageContext messageContext) {
            }
        });
        HandlerChainInvoker invoker = new HandlerChainInvoker(list);

        IMocksControl control = createNiceControl();
        Binding binding = control.createMock(Binding.class);
        SoapMessage message = control.createMock(SoapMessage.class);
        Exchange exchange = control.createMock(Exchange.class);
        InterceptorChain chain = control.createMock(InterceptorChain.class);

        expect(chain.doInterceptInSubChain(isA(Message.class))).andReturn(true);
        expect(message.getExchange()).andReturn(exchange).anyTimes();
        expect(message.getInterceptorChain()).andReturn(chain);
        expect(exchange.get(HandlerChainInvoker.class)).andReturn(invoker);
        // This is to set direction to outbound
        expect(exchange.getOutMessage()).andReturn(message);

        CachedStream originalEmptyOs = new CachedStream();
        SOAPMessage soapMessage = preparemSOAPMessage("resources/greetMeRpcLitResp.xml");
        CachedStream os = prepareOutputStreamFromSOAPMessage(soapMessage);
        expect(message.getContent(OutputStream.class)).andReturn(
                originalEmptyOs);
        expect(message.getContent(OutputStream.class)).andReturn(os);
        control.replay();

        SOAPHandlerInterceptor li = new SOAPHandlerInterceptor(binding);
        li.handleMessage(message);
        control.verify();

        // the content of outputStream should remain unchanged.
        CachedStream expectedOs = prepareOutputStreamFromResource("resources/greetMeRpcLitResp.xml");
        assertTrue("the content of os should remain unchanged",
                compareInputStream(expectedOs.getInputStream(), originalEmptyOs
                        .getInputStream()));
    }

    // SAAJ tree is created on if SOAPMessageContext.getMessage() is
    // called. Any changes to SOAPMessage should be streamed back to
    // outputStream
    public void testSOAPBodyChangedOutBound() throws Exception {
        List<Handler> list = new ArrayList<Handler>();
        list.add(new SOAPHandler<SOAPMessageContext>() {
            public boolean handleMessage(SOAPMessageContext smc) {
                Boolean outboundProperty = (Boolean) smc
                        .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
                if (outboundProperty.booleanValue()) {
                    try {
                        SOAPMessage message = smc.getMessage();
                        message = preparemSOAPMessage("resources/greetMeRpcLitRespChanged.xml");
                    } catch (Exception e) {
                        throw new Fault(e);
                    }
                }
                return true;
            }

            public boolean handleFault(SOAPMessageContext smc) {
                return true;
            }

            public Set<QName> getHeaders() {
                return null;
            }

            public void close(MessageContext messageContext) {
            }
        });
        HandlerChainInvoker invoker = new HandlerChainInvoker(list);

        IMocksControl control = createNiceControl();
        Binding binding = control.createMock(Binding.class);
        SoapMessage message = control.createMock(SoapMessage.class);
        Exchange exchange = control.createMock(Exchange.class);
        InterceptorChain chain = control.createMock(InterceptorChain.class);

        expect(chain.doInterceptInSubChain(isA(Message.class))).andReturn(true);
        expect(message.get(notNull())).andReturn(true).anyTimes();
        expect(message.keySet()).andReturn(new HashSet()).anyTimes();
        expect(message.getExchange()).andReturn(exchange).anyTimes();
        expect(message.getInterceptorChain()).andReturn(chain);
        expect(exchange.get(HandlerChainInvoker.class)).andReturn(invoker)
                .anyTimes();
        // message.getExchange().get(HandlerChainInvoker.class);

        // This is to set direction to outbound
        expect(exchange.getOutMessage()).andReturn(message);

        CachedStream originalEmptyOs = new CachedStream();
        SOAPMessage soapMessage = preparemSOAPMessage("resources/greetMeRpcLitResp.xml");
        expect(message.getContent(SOAPMessage.class)).andReturn(soapMessage);
        SOAPMessage soapMessageChanged = preparemSOAPMessage("resources/greetMeRpcLitRespChanged.xml");
        expect(message.getContent(SOAPMessage.class)).andReturn(
                soapMessageChanged);
        expect(message.getContent(OutputStream.class)).andReturn(
                originalEmptyOs);
        control.replay();

        SOAPHandlerInterceptor li = new SOAPHandlerInterceptor(binding);
        li.handleMessage(message);
        control.verify();

        // the content of outputStream should remain unchanged.
        CachedStream expectedOs = prepareOutputStreamFromResource("resources/greetMeRpcLitRespChanged.xml");
        assertTrue("the content of os should have changed", compareInputStream(
                expectedOs.getInputStream(), originalEmptyOs.getInputStream()));
    }

    public void xtestInterceptSuccessInBound() throws Exception {
        List<Handler> list = new ArrayList<Handler>();
        list.add(new SOAPHandler<SOAPMessageContext>() {
            public boolean handleMessage(SOAPMessageContext smc) {
                return true;
            }

            public boolean handleFault(SOAPMessageContext smc) {
                return true;
            }

            public Set<QName> getHeaders() {
                return null;
            }

            public void close(MessageContext messageContext) {
            }
        });
        HandlerChainInvoker invoker = new HandlerChainInvoker(list);

        IMocksControl control = createNiceControl();
        Binding binding = control.createMock(Binding.class);
        SoapMessage message = control.createMock(SoapMessage.class);
        Exchange exchange = control.createMock(Exchange.class);
        InterceptorChain chain = control.createMock(InterceptorChain.class);
        InputStream is = this.getClass().getResourceAsStream(
                "resources/greetMeRpcLitReq.xml");
        SOAPMessage soapMessage = null;
        try {
            MessageFactory factory = MessageFactory.newInstance();
            MimeHeaders mhs = null;
            soapMessage = factory.createMessage(mhs, is);
        } catch (Exception e) {
            throw e;
        }

        expect(message.getExchange()).andReturn(exchange).anyTimes();
        expect(exchange.get(HandlerChainInvoker.class)).andReturn(invoker);
        // This is to set direction to inbound
        expect(exchange.getOutMessage()).andReturn(null);
        expect(message.getContent(SOAPMessage.class)).andReturn(soapMessage);
        control.replay();

        SOAPHandlerInterceptor li = new SOAPHandlerInterceptor(binding);
        li.handleMessage(message);
        control.verify();
    }

    public void testgetUnderstoodHeadersReturnsNull() {
        List<Handler> list = new ArrayList<Handler>();
        list.add(new SOAPHandler<SOAPMessageContext>() {
            public boolean handleMessage(SOAPMessageContext smc) {
                return true;
            }

            public boolean handleFault(SOAPMessageContext smc) {
                return true;
            }

            public Set<QName> getHeaders() {
                return null;
            }

            public void close(MessageContext messageContext) {
            }
        });
        HandlerChainInvoker invoker = new HandlerChainInvoker(list);

        IMocksControl control = createNiceControl();
        Binding binding = control.createMock(Binding.class);
        SoapMessage message = control.createMock(SoapMessage.class);
        Exchange exchange = control.createMock(Exchange.class);
        expect(binding.getHandlerChain()).andReturn(list);
        expect(message.getExchange()).andReturn(exchange);
        expect(message.keySet()).andReturn(new HashSet<String>());
        expect(exchange.get(HandlerChainInvoker.class)).andReturn(invoker);
        control.replay();

        SOAPHandlerInterceptor li = new SOAPHandlerInterceptor(binding);
        Set<QName> understood = li.getUnderstoodHeaders();
        assertNotNull(understood);
        assertTrue(understood.isEmpty());
    }

    private boolean compareInputStream(InputStream os1, InputStream os2) throws Exception {
        if (os1.available() != os2.available()) {
            return false;
        }
        for (int i = 0; i < os1.available(); i++) {
            if (os1.read() != os2.read()) {
                return false;
            }
        }
        return true;
    }

    private SOAPMessage preparemSOAPMessage(String resouceName) throws Exception {
        InputStream is = this.getClass().getResourceAsStream(resouceName);
        SOAPMessage soapMessage = null;
        MessageFactory factory = MessageFactory.newInstance();
        MimeHeaders mhs = null;
        soapMessage = factory.createMessage(mhs, is);
        return soapMessage;
    }

    private CachedStream prepareOutputStreamFromResource(String resouceName) throws Exception {
        SOAPMessage soapMessage = preparemSOAPMessage(resouceName);
        CachedStream os = new CachedStream();
        soapMessage.writeTo(os);
        return os;
    }

    private CachedStream prepareOutputStreamFromSOAPMessage(
            SOAPMessage soapMessage) throws Exception {
        CachedStream os = new CachedStream();
        soapMessage.writeTo(os);
        return os;
    }

    private class CachedStream extends AbstractCachedOutputStream {
        protected void doFlush() throws IOException {
            currentStream.flush();
        }

        protected void doClose() throws IOException {
        }

        protected void onWrite() throws IOException {
        }
    }

}
