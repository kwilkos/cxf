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

package org.apache.cxf.jaxws.handler;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.easymock.classextension.IMocksControl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createNiceControl;

public class StreamHandlerInterceptorTest extends TestCase {
    
    private IMocksControl control;
    private Binding binding;
    private HandlerChainInvoker invoker;
    private Message message;
    private Exchange exchange;
    
    public void setUp() {
        control = createNiceControl();
        binding = control.createMock(Binding.class);
        invoker = control.createMock(HandlerChainInvoker.class);
        message = control.createMock(Message.class);
        exchange = control.createMock(Exchange.class);
        List<StreamHandler> list = new ArrayList<StreamHandler>();
        list.add(new StreamHandler() {
            public void close(MessageContext arg0) {
            }
            public boolean handleFault(StreamMessageContext arg0) {
                return true;
            }
            public boolean handleMessage(StreamMessageContext arg0) {
                return true;
            }
        });
        expect(invoker.getStreamHandlers()).andReturn(list);        
    }
    
    public void tearDown() {
        control.verify();
    }

    public void testInterceptSuccess() {
        expect(message.getExchange()).andReturn(exchange);
        expect(exchange.get(HandlerChainInvoker.class)).andReturn(invoker);
        expect(invoker.invokeStreamHandlers(isA(StreamMessageContext.class))).andReturn(true);
        control.replay();
        StreamHandlerInterceptor si = new StreamHandlerInterceptor(binding);
        assertEquals("unexpected phase", "user-stream", si.getPhase());
        si.handleMessage(message);
    }
    
    public void testInterceptFailure() {
        expect(message.getExchange()).andReturn(exchange);
        expect(exchange.get(HandlerChainInvoker.class)).andReturn(invoker);
        expect(invoker.invokeStreamHandlers(isA(StreamMessageContext.class))).andReturn(false);
        control.replay();
        StreamHandlerInterceptor si = new StreamHandlerInterceptor(binding);
        si.handleMessage(message); 
    }
}
